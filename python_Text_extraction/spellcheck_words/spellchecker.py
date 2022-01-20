import json
import argparse
import multiprocessing
import pkg_resources
from symspellpy import SymSpell, Verbosity
import os
from tqdm import tqdm
from multiprocessing import Pool
from functools import partial

set_files = set()


def spellchecker(input_txt_dir: str, speller: SymSpell, spell_object_name: str):
    """
    :param input_txt_dir: Path of the txt-file
    :param speller: SymSpell for checking the spelling mistakes
    :param spell_object_name: Modifier for the output name
    :return: Dictionary of the qualities
    """
    right = 0
    wrong = 0
    unknown = 0
    number_of_words = 0
    quality_good, quality_good_unknown = 0.0, 0.0
    try:
        with open(input_txt_dir, "r", encoding="UTF-8") as text_file:
            text = text_file.read()
            text = text.replace("\n", " ")
        for word in text.split():
            #  and not "ſ" in word:
            if word.isalnum() and not word.isdigit():
                number_of_words += 1
                suggestions = speller.lookup(word.lower(), Verbosity.CLOSEST, max_edit_distance=2)
                if suggestions:
                    if suggestions[0].term == word.lower():
                        right += 1
                    else:
                        wrong += 1
                else:
                    unknown += 1
        if right != 0 and wrong != 0:
            quality_good = right / (wrong + right)
            quality_good_unknown = right / number_of_words
        elif wrong == 0:
            quality_good, quality_good_unknown = 1.0, 1.0
        elif right:
            quality_good, quality_good_unknown = 0.0, 0.0
        file_base_name = str(os.path.basename(input_txt_dir))
        name_data = f"{spell_object_name}_{file_base_name}"
        dict_spellcheck_text = {"name": name_data,
                                "right_number": right,
                                "wrong_number": wrong,
                                "unknown_number": unknown,
                                "number_of_words": number_of_words,
                                "quality_good": quality_good,
                                "quality_good_unknown": quality_good_unknown}
        return dict_spellcheck_text
    except Exception as ex:
        print(f"{input_txt_dir}, Error: {ex}")


def multiprocessing_spellchecker(in_txt_dir: str, in_dir_speller: str, spell_object_name: str, out_dir: str):
    """
    :param in_txt_dir: Directory of all txt-files
    :param in_dir_speller: Name of the SymSpell word lexicon
    :param spell_object_name: Modifier for the output name
    :param out_dir: Location for the output
    :return:
    """
    # in_dir_speller = f"de-100k.txt"
    try:
        sym_spell = SymSpell(max_dictionary_edit_distance=2, prefix_length=7)
        dictionary_path = pkg_resources.resource_filename(
            "symspellpy", in_dir_speller
        )
        sym_spell.load_dictionary(dictionary_path, term_index=0, count_index=1)
    except Exception as ex:
        print(f"{in_dir_speller}, Error: {ex}")
        exit()
    global set_files
    set_files = set()
    get_all_path_files(in_txt_dir, ".txt")
    files = list(set_files)
    part_func = partial(spellchecker, speller=sym_spell, spell_object_name=spell_object_name)
    pool = Pool(multiprocessing.cpu_count() - 1)
    result = list(tqdm(pool.imap_unordered(part_func, files),
                       desc=f"Spellchecking {spell_object_name}: {in_txt_dir.split('/')[-1]}", total=len(files)))
    pool.close()
    pool.join()
    os.makedirs(out_dir, exist_ok=True)
    out_name_dir = f"{out_dir}/{spell_object_name}_spellchecking.json"
    right_number = 1
    wrong_number = 1
    for i in result:
        right_number += i["right_number"]
        wrong_number += i["wrong_number"]
    all_good_quality = right_number / (wrong_number + right_number)
    out_all = out_name_dir.replace(".json", "_all_together.txt")
    print(f"Output: {out_all}")
    with open(out_name_dir, "w", encoding="UTF-8") as out_name_dir:
        json.dump(list(result), out_name_dir, indent=2)
    with open(out_all, "w", encoding="UTF-8") as text_file:
        text_file.write(f"Good_quality\tright_number\twrong_number\n")
        text_file.write(f"{all_good_quality}\t{right_number}\t{wrong_number}\n")


def get_all_path_files(path_dir: str, end_file: str):
    """
    :param path_dir: directory path for searching file paths
    :param end_file: which files should be search (txt-data => .txt)
    :return:
    """
    global set_files
    for file in os.scandir(path_dir):
        if file.is_dir():
            get_all_path_files(file, end_file)
        elif (str(file.path)).endswith(f"{end_file}"):
            set_files.add(str(file.path))


def summary_result_spellcheck(input_dir_results: str, end_with: str, spell_object_name: str = "SpellChecking"):
    """
    :param input_dir_results: Directory of the spellchecking result
    :param end_with: Data format of the results normally .txt
    :param spell_object_name: Modifier for the output name
    :return:
    """
    global set_files
    set_files = set()
    get_all_path_files(input_dir_results, end_with)
    files = list(set_files)
    quality_sum = 0
    number_right = 0
    number_wrong = 0
    out_first_line = f"Good_quality\tright_number\twrong_number\n"
    for file_dir in tqdm(files, desc=f"Put all qualities together of {spell_object_name}"):
        with open(file_dir, "r", encoding="UTF-8") as txt_file:
            info_out = txt_file.readlines()[1].split()
            quality_sum += float(info_out[0])
            number_right += int(info_out[1])
            number_wrong += int(info_out[2].replace("\n", ""))
    quality_federal_state = quality_sum / len(files)
    out_dir_name = f"{input_dir_results}/{spell_object_name}_all_quality_out.txt"
    with open(out_dir_name, "w", encoding="UTF-8") as out_text_file:
        out_text_file.write(out_first_line)
        out_text_file.write(f"{quality_federal_state}\t{number_right}\t{number_wrong}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-p", "--path_directory", help="Path to the directory with the .txt files")
    parser.add_argument("-s", "--sym_speller", default="de-100k.txt", help="Name of the word lexicon for SymSpellpy")
    parser.add_argument("-o", "--out", default="symspell_spellcheck", help="Directory path for the outputs of the spell checking")
    parser.add_argument("-m", "--name_modifier", default="Symspell", help="Modify the output name")
    args = parser.parse_args()
    multiprocessing_spellchecker(in_txt_dir=args.path_directory, in_dir_speller=args.sym_speller, spell_object_name=args.name_modifier, out_dir=args.out)

