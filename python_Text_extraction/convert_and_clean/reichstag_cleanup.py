import os
import pathlib
import re
from typing import List, Tuple, Union

import xmi
from tqdm import tqdm
import xml.etree.ElementTree as ET
from multiprocessing import Pool
from functools import partial


class Reichtags_Handle:

    def __init__(self):
        self.directory_path = "/resources/corpora/parlamentary"

    @staticmethod
    def list_sub_directories_and_files(directory_path: str) -> Tuple[List[str], List[str]]:
        sub_elements = os.listdir(directory_path)
        files, dir = [], []
        for elem in sub_elements:
            elem = os.path.join(directory_path, elem)
            if os.path.isfile(elem):
                files.append(elem)
            else:
                dir.append(elem)
        return files, dir

    @staticmethod
    def process_whole_directory(directory_path: str) -> Tuple[List[str], List[str], List[str]]:
        files, directoies = Reichtags_Handle.list_sub_directories_and_files(directory_path)
        dir_stack = directoies[:]
        empty_dirs = []
        pbar = tqdm(desc="Going through directory")
        while len(dir_stack) != 0:
            cur_files, cur_dirs = Reichtags_Handle.list_sub_directories_and_files(dir_stack[0])
            if cur_files == [] and cur_dirs == []:
                empty_dirs.append(dir_stack[0])
            dir_stack.extend(cur_dirs)
            dir_stack = dir_stack[1:]
            files.extend(cur_files)
            directoies.extend(cur_dirs)
            pbar.update(1)
        return files, directoies, empty_dirs

    @staticmethod
    def parse_ocr_xml(file_path: str) -> str:
        text = ""
        tree = ET.parse(file_path)
        root = tree.getroot()
        for ocr_page in root[1]:
            page = ""
            block_count = 0
            max_block_count = len([1 for var_x in ocr_page])
            for ocrx_block in ocr_page:
                block = ""
                line_count = 0
                for ocr_par in ocrx_block:
                    para = ""
                    for ocr_line in ocr_par:
                        line_count += 1
                        line = ""
                        for ocr_word in ocr_line:
                            word_text = ocr_word.text
                            if word_text != None:
                                line += word_text + " "
                        para += line.strip() + "\n"
                    block += para.strip() + "\n\n"
                if (block_count in [0, 1]) and (line_count < 2):
                    pass
                elif (block_count == max_block_count - 1) and (line_count < 2):
                    pass
                else:
                    page += block.strip() + "\n\n\n"
            text += page.strip() + "\n\n\n\n"

        return text.strip()

    @staticmethod
    def mp_parse_job(directory: str, save_path: str) -> List[Union[bytes, str]]:
        fails = []
        year, ep, year2 = directory.split("/")[-3:]
        try:
            pathlib.Path(os.path.join(save_path, year, ep, year2)).mkdir(parents=True, exist_ok=False)
        except:
            pass
        file_names = os.listdir(directory)
        protocol_names = []
        pattern_02 = re.compile(
            '([0-9]|([1-9][0-9])|([1-9][0-9][0-9])|1000)\\. Sitzung [0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9][0-9].*')
        for file_name in file_names:
            z = re.match(pattern_02, file_name)
            if z:
                protocol_names.append(file_name)
        meetings = dict()
        for file_name in protocol_names:
            number = file_name.split(".")[0]
            if number in meetings:
                meetings[number].append(file_name)
            else:
                meetings[number] = [file_name]

        for key in meetings:
            index_array = [0 for i in range(0, 10000)]  # assuming no protocol has more than 10000 pages
            all_pages = meetings[key]
            for page in all_pages:
                page_number = page.split("_")[-1].strip(".xml")
                index_array[int(page_number)] = page
            index_array = [indx for indx in index_array if indx != 0]
            text = ""
            for file_path in index_array:
                file_path = os.path.join(directory, file_path)
                try:
                    text += Reichtags_Handle.parse_ocr_xml(file_path) + "\n\n\n"
                except:
                    print("{} failed".format(file_path))
                    fails.append(file_path)
            text = text.strip()
            with open(os.path.join(save_path, year, ep, year2, all_pages[0].split("_")[0] + ".txt"), "w") as f:
                f.write(text)
        return fails


    @staticmethod
    def parse_directory(directory_path: str, save_path: str) -> None:
        pattern_01 = re.compile("[0-9][0-9][0-9][0-9] - [0-9][0-9][0-9][0-9]")
        main_paths = []
        for sub_dir in os.listdir(directory_path):
            z = re.match(pattern_01, sub_dir)
            if z:
                whole_path = os.path.join(directory_path, sub_dir)
                if os.path.isdir(whole_path):
                    main_paths.append(whole_path)
        part_func = partial(Reichtags_Handle.mp_parse_job, save_path=save_path)
        for main_path in main_paths:
            main_path_name = main_path.split("/")[-1]  # first_path_part
            try:
                pathlib.Path(os.path.join(save_path, main_path_name)).mkdir(parents=True, exist_ok=False)
            except:
                pass
            sub_directories = [os.path.join(main_path, sub_directory) for sub_directory in os.listdir(main_path) if sub_directory != "Sonstige"]
            sub_directories = [sub_directory for sub_directory in sub_directories if os.path.isdir(sub_directory)]
            # for xxx in sub_directories: print(xxx)
            sub_sub_directories = []
            for sub_directory in sub_directories:
                try:
                    pathlib.Path(os.path.join(save_path, main_path_name, sub_directory.split("/")[-1])).mkdir(parents=True, exist_ok=False)
                except:
                    pass
                sub_sub_directories.extend([os.path.join(sub_directory, sub_sub_directory) for sub_sub_directory in os.listdir(sub_directory)
                                            if os.path.isdir(os.path.join(sub_directory, sub_sub_directory))])
            # print(sub_sub_directories)
            pool = Pool(28)
            result = list(tqdm(pool.imap_unordered(part_func, sub_sub_directories), desc="Parsing_Converting Dir: {}".format(main_path_name)))
            pool.close()
            pool.join()
            # pool.terminate()
            fails = []
            for fail_list in result:
                for fail in fail_list:
                    fails.append(fail)
            with open(save_path + "/fails.txt", "w") as f:
                for fail in fails:
                    f.write(fail + "\n")

        return


if __name__ == "__main__":
    test = Reichtags_Handle()
    """result = test.process_whole_directory(test.directory_path)
    for i in result[-1]:
        print(i)
    print("Dirs: {}; Files: {}".format(len(result[1]), len(result[0])))"""

