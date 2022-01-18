from datetime import datetime
import shutil
import os
from tqdm import tqdm
import pathlib
import pickle


def wahlperiode_oesterreich(file_path:str) -> int:
    year_dic = {
                "17.10.1920":1,
                "21.10.1923":2,
                "24.04.1927":3,
                "01.11.1930":4,
                "25.11.1945":5,
                "09.10.1949":6,
                "22.02.1953":7,
                "13.05.1956":8,
                "10.05.1959":9,
                "18.11.1962":10,
                "06.03.1966":11,
                "01.03.1970":12,
                "10.10.1971":13,
                "05.10.1975":14,
                "06.05.1979":15,
                "24.04.1983":16,
                "23.11.1986":17,
                "07.10.1990":18,
                "09.10.1994":19,
                "17.12.1995":20,
                "03.10.1999":21,
                "24.11.2002":22,
                "01.10.2006":23,
                "28.09.2008":24,
                "29.09.2013":25,
                "15.10.2017":26,
                "29.09.2019":27
               }
    file_date = file_path.split("/")[-1].split("_")[0]
    current = datetime.strptime(file_date, '%d.%m.%Y')
    current_wp = 0
    for example_date in year_dic:
        reference = datetime.strptime(example_date, '%d.%m.%Y')
        if current >= reference:
            current_wp = year_dic[example_date]
        else:
            return current_wp
    return current_wp


def convert_dir_to_normal_datastructure_pdf(dir_path:str, fraktur:bool=False) -> None:
    filenames = [os.path.join(dir_path, filename) for filename in os.listdir(dir_path)]
    count_all_files = len(filenames)
    filenames = [filename for filename in filenames if filename[-4:] == ".pdf"]
    count_pdf_files = len(filenames)

    if fraktur:
        with open("/resources/corpora/parlamentary_germany/Oesterreich/list_of_fraktur_files.txt", "w") as f:
            for filename in filenames:
                f.write(filename + "\n")

    for filename in tqdm(filenames, desc="Converting Structure"):
        wp = wahlperiode_oesterreich(filename)
        dir = "/".join(filename.split("/")[:-2]) + f"/pdf/{str(wp)}"
        pathlib.Path(dir).mkdir(parents=True, exist_ok=True)
        new_filename = dir + "/" + filename.split("/")[-1]
        shutil.copy2(filename, new_filename)
    print(f"|all-files|:{count_all_files}; |pdf-files|:{count_pdf_files}")
    return None


def convert_dir_to_normal_datastructure_txt(dir_path:str) -> None:
    filenames = [os.path.join(dir_path, filename) for filename in os.listdir(dir_path)]
    count_all_files = len(filenames)
    filenames = [filename for filename in filenames if filename[-4:] == ".txt"]
    count_pdf_files = len(filenames)

    for filename in tqdm(filenames, desc="Converting Structure"):
        wp = wahlperiode_oesterreich(filename)
        dir = "/".join(filename.split("/")[:-2]) + f"/txt/{str(wp)}"
        pathlib.Path(dir).mkdir(parents=True, exist_ok=True)
        new_filename = dir + "/" + filename.split("/")[-1]
        shutil.copy2(filename, new_filename)
    print(f"|all-files|:{count_all_files}; |txt-files|:{count_pdf_files}")
    return None


def get_list_of_all_Fraktur_pdf(dir_path:str, fraktur:bool=True) -> None:
    filenames = [os.path.join(dir_path, filename) for filename in os.listdir(dir_path)]
    count_all_files = len(filenames)
    filenames = [filename for filename in filenames if filename[-4:] == ".pdf"]
    count_pdf_files = len(filenames)

    if fraktur:
        with open("/resources/corpora/parlamentary_germany/Oesterreich/list_of_fraktur_files.txt", "w") as f:
            for filename in tqdm(filenames):
                f.write(filename + "\n")
    print(f"|all-files|:{count_all_files}; |txt-files|:{count_pdf_files}")

if __name__ == "__main__":
    convert_dir_to_normal_datastructure_txt("/resources/corpora/parlamentary_germany/Oesterreich/Fraktur")