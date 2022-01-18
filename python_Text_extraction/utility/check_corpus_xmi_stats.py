import os
import sys
from datetime import datetime

from tqdm import tqdm

sys.path.append("/home/stud_homes/s5935481/uima_cassis/src")
sys.path.append(os.path.realpath(os.path.join(os.path.dirname(__file__), '..', '..')))
from cassis_utility.loading_utility import load_cas_from_dir, load_cas_from_xmi_dir, find_paths_in_xmi_dir, \
    load_typesystem, load_cas_from_xmi_file
import cassis
ROOT_DIR = os.path.realpath(os.path.join(os.path.dirname(__file__), '..', '..'))


def main(dir_path: str, safe: bool) -> None:
    corpus_name = dir_path.split("/")[-2]
    paths = find_paths_in_xmi_dir(dir_path)
    paths = [path for path in paths if ".xmi.gz" not in path]
    typesystem = load_typesystem(path=os.path.join(ROOT_DIR, "TypeSystem.xml"))

    year_list = []
    bar = tqdm(bar_format="year", leave=True, position=0)
    if safe:
        for i in tqdm(range(0, len(paths)), leave=True, position=1):
            try:
                cas = load_cas_from_xmi_file(filepath=paths[i], typesystem=typesystem)
                doc_anno = cas.select("org.texttechnologylab.annotation.DocumentAnnotation")[0]
                year = doc_anno["dateYear"]
                year_list.append((year, paths[i]))
                bar.bar_format = str(year)
                bar.refresh()
            except:
                print(f"ERROR: {paths[i]}")
    else:
        for i in tqdm(range(0, len(paths)), leave=True, position=1):
            cas = load_cas_from_xmi_file(filepath=paths[i], typesystem=typesystem)
            doc_anno = cas.select("org.texttechnologylab.annotation.DocumentAnnotation")[0]
            year = doc_anno["dateYear"]
            year_list.append((year, paths[i]))
            bar.bar_format = str(year)
            bar.refresh()
    year_list.sort(key= lambda x: int(x[0]))
    with open(os.path.join(ROOT_DIR, "src", "data", corpus_name + ".txt"), "w") as f:
        for i in year_list:
            f.write(str(i[0]) + " " + i[1] + "\n")
            print(i)


def date_niedersachsen(file_path:str) -> None:
    """
    Function to get the date for a document from brandenburg corpus.
    :param filepath:
    :return:
    """
    months = [
        (1, 'Januar'),
        (2, 'Februar'),
        (3, 'März'),
        (4, 'April'),
        (5, 'Mai'),
        (6, 'Juni'),
        (7, 'Juli'),
        (8, 'August'),
        (9, 'September'),
        (10, 'Oktober'),
        (11, 'November'),
        (12, 'Dezember'),
    ]
    with open(file_path, "r") as f:
        for line in f:
            if "Ausgegeben" not in line:
                for month in months:
                    if month[-1] in line:
                        try:
                            day, year = line.split(month[-1])
                            day, year = day.strip(), year.strip()
                            day = day.split(" ")[-1].strip(".")[-2:].strip()
                            final_date = day + "." + str(month[0]) + "." + year[:4]
                            date_time_obj = datetime.strptime(final_date, '%d.%m.%Y')
                            return final_date
                        except:
                            pass

def timestamp_maker(date: str) -> int:
    date_time_obj = datetime.strptime(date, '%d.%m.%Y')
    timestamp = int(datetime.timestamp(date_time_obj) * 1000)
    return timestamp

if __name__ == "__main__":
    #print(ROOT_DIR)
    main("/resources/corpora/parlamentary_germany/Saarland/xmi", True)
    #main("/resources/corpora/parlamentary_germany/OldGermany/xmi", True)
    """while True:
        date = str(input())
        print(date)
        print(timestamp_maker(date))"""