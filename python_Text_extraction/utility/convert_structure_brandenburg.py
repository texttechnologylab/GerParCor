import os
import pathlib
import shutil

def convert_dir_structure(dir_path:str):
    dir_stack = [os.path.join(dir_path, file) for file in os.listdir(dir_path)]
    dir_stack = [file_path for file_path in dir_stack if os.path.isdir(file_path)]
    dir_with_txt = set()
    while len(dir_stack) != 0:
        dir_path = dir_stack[0]
        sub_elems = [os.path.join(dir_path, file) for file in os.listdir(dir_path)]
        sub_files, sub_dirs = [], []
        for elem in sub_elems:
            if os.path.isdir(elem):
                sub_dirs.append(elem)
            elif os.path.isfile(elem):
                sub_files.append(elem)
            else:
                pass
        dir_stack.extend(sub_dirs)
        for sub_file in sub_files:
            if ".txt" in sub_file:
                dir_with_txt.add(dir_path)
        dir_stack = dir_stack[1:]
    dir_with_txt = list(dir_with_txt)
    for text_dir in dir_with_txt:
        # /vol/s5935481/parlamentary/brandenburg/1. Wahlperiode (26.10.1990 - 11.10.1994)/txt
        new_text_dir = os.path.join("/".join(text_dir.split("/")[:5]), "txt", text_dir.split("/")[-2])
        pathlib.Path(new_text_dir).mkdir(parents=True, exist_ok=True)
        txt_sub_files = [os.path.join(text_dir, file) for file in os.listdir(text_dir)]
        for txt_sub_file in txt_sub_files:
            shutil.copy2(txt_sub_file, os.path.join(new_text_dir, txt_sub_file.split("/")[-1]))

if __name__ == "__main__":
    convert_dir_structure("/vol/s5935481/parlamentary/brandenburg")