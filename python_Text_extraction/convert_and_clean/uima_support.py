import pathlib
from datetime import datetime
from multiprocessing import Pool
from typing import Union, Optional, Tuple, List, Set

import cassis
import time
import os
from functools import partial
from tqdm import tqdm
import re
import codecs
import traceback

# path of the corpus where all xmi files are stored
XMI_CORPUS_PATH = "/resources/corpora/parlamentary_germany"
# dictionary with identifiers for the different parliaments
MASK = {
        "Reichstag":    {"landtag":"Reichstag",
                        "origin_path":"/resources/corpora/parlamentary",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"BSB-Bayerische Staatsbibliothek",
                        "date_func": (lambda file_path: file_path.strip(".txt").split(" ")[-1]),
                        "subtitle": (lambda file_path: file_path.split("/")[-3].replace(" ", "") + "__" + "".join(file_path.split("/")[-1].split(" ")[0:2])),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "reichstag", "/".join(file_path.split("/")[4:-1])))),
                        "dir_path" : "/vol/s5935481/parlamentary_reichstag_text",
                        "filter": False
                        },
        "Hamburg":      {"landtag":"Hamburgische-Bürgerschaft",
                        "origin_path":"/vol/s5935481/parlamentary/hamburg/pdf",
                        "user1":"hammerla",
                        "user2":"hammerla",
                        "quelle":"hamburgische Bürgerschaftskanzlei",
                        "date_func": (lambda file_path: date_hamburg(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-1].split("_")[1] + ".Wahlperiode__" + file_path.split("/")[-1].strip(".txt").split("_")[2] + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "hamburg", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/vol/s5935481/parlamentary/hamburg/txt",
                        "filter":True
                        },
        "Bayern":       {"landtag":"Bayerische-Landtag",
                        "origin_path":"/vol/s5935481/parlamentary/bayern/pdf",
                        "user1":"hammerla",
                        "user2":"hammerla",
                        "quelle":"Bayerische-Landtag",
                        "date_func": (lambda file_path: file_path.split("/")[-1].replace("Seitenaus", "")[5:7] + "." + file_path.split("/")[-1].replace("Seitenaus", "")[7:9] + ".20" + file_path.split("/")[-1].replace("Seitenaus", "")[9:11] if int(file_path.split("/")[-1].replace("Seitenaus", "")[9:11]) < 30 else file_path.split("/")[-1].replace("Seitenaus", "")[5:7] + "." + file_path.split("/")[-1].replace("Seitenaus", "")[7:9] + ".19" + file_path.split("/")[-1].replace("Seitenaus", "")[9:11]),
                        "subtitle": (lambda file_path: file_path.split("/")[-2].split(".")[0] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].replace("Seitenaus", "")[:3])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "bayern", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/vol/s5935481/parlamentary/bayern/txt",
                        "filter":True
                        },
        "Sachsen_Anht": {"landtag":"Landtag-von-Sachsen-Anhalt",
                        "origin_path":"/vol/s5935481/parlamentary/sachsen_anhalt/pdf",
                        "user1":"hammerla",
                        "user2":"hammerla",
                        "quelle":"Landtag-von-Sachsen-Anhalt",
                        "date_func": (lambda file_path: date_sachsen_anhalt(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2].split(" ")[0] + ".Wahlperiode__" + str(int(file_path.split("/")[-1][:3])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "sachsen_anhalt", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/vol/s5935481/parlamentary/sachsen_anhalt/txt",
                        "filter":True
                        },
        "Brandenburg":  {"landtag":"Landtag-Brandenburg",
                        "origin_path":"/vol/s5935481/parlamentary/brandenburg/3. Wahlperiode (29.09.1999 - 13.10.2004)/pdf",
                        "user1":"hammerla",
                        "user2":"hammerla",
                        "quelle":"Brandenburgischer-IT-Dienstleister",
                        "date_func": (lambda file_path: date_brandenburg(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2].split(".")[0] + ".Wahlperiode__" + file_path.split("/")[-1].strip(".txt").split("_")[-1] + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "brandenburg", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/vol/s5935481/parlamentary/brandenburg/txt",
                        "filter":True
                        },
        "Berlin":       {"landtag":"Abgeordnetenhaus Berlin",
                        "origin_path":"/resources/corpora/parlamentary_germany/Berlin/pdf",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"Abgeordnetenhaus Berlin",
                        "date_func": (lambda file_path: date_berlin(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-1].split("_")[0] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].strip(".txt").split("_")[-1])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Berlin/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Berlin/txt",
                        "filter":True
                        },
        "Bremen":       {"landtag":"Bremische Bürgerschaft",
                        "origin_path":"/resources/corpora/parlamentary_germany/Bremen/pdf",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"Parlamentsdokumentation-Bremische Bürgerschaft",
                        "date_func": (lambda file_path: date_hamburg(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].strip(".txt")[-4:])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Bremen/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Bremen/txt",
                        "filter":True
                        },
        "Meck_Pom":     {"landtag":"Landtag Mecklenburg-Vorpommern",
                        "origin_path":"/resources/corpora/parlamentary_germany/MeckPom/pdf",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"Landtag Mecklenburg-Vorpommern",
                        "date_func": (lambda file_path: date_meckpom(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt").replace("Plenarprotokoll_7_",""))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "MeckPom/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/MeckPom/txt",
                        "filter":True
                        },
        "R-Pfalz":      {"landtag":"Landtag Rheinland-Pfalz",
                        "origin_path":"/resources/corpora/parlamentary_germany/RheinlandPfalz/pdf",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"Landtag Rheinland-Pfalz",
                        "date_func": (lambda file_path: date_pfalz(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt").split("-")[0])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "RheinlandPfalz/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/RheinlandPfalz/txt",
                        "filter":True
                        },
        "S-Holstein":   {"landtag":"Schleswig-holsteinischer Landtag",
                        "origin_path":"/resources/corpora/parlamentary_germany/SchleswigHolstein/pdf",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"Schleswig-holsteinischer Landtag",
                        "date_func": (lambda file_path: date_schleswig_holstein(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].split("_")[1].split(" ")[0])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "SchleswigHolstein/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/SchleswigHolstein/txt",
                        "filter":True
                        },
        "LiechtenSt":   {"landtag":"Landtag des Fürstentums Liechtenstein",
                        "origin_path":"/resources/corpora/parlamentary_germany/Lichtenstein/pdf",
                        "user1":"abrami",
                        "user2":"hammerla",
                        "quelle":"Landtag des Fürstentums Liechtenstein",
                        "date_func": (lambda file_path: date_liechtenstein(file_path)),
                        "subtitle": (lambda file_path: str(wahlperiode_liechtenstein(file_path)) + ".Wahlperiode__" + str(sitzungs_nr_liechtenstein(file_path)) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Lichtenstein/xmi", str(wahlperiode_liechtenstein(file_path))))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Lichtenstein/txt",
                        "filter":True
                        },
        "N-Sachsen":    {"landtag": "Niedersächsischer Landtag",
                        "origin_path": "/resources/corpora/parlamentary_germany/Niedersachsen/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Niedersächsisches-Landtagsdokumentationssystem",
                        "date_func": (lambda file_path: date_niedersachsen(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt"))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Niedersachsen/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Niedersachsen/txt",
                        "filter": True
                        },
        "N-W-Falen":    {"landtag": "Landtag Nordrhein-Westfalen",
                        "origin_path": "/resources/corpora/parlamentary_germany/NordrheinWestfalen/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Parlamentsdatenbank Nordrhein-Westfalen",
                        "date_func": (lambda file_path: date_niedersachsen(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt"))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "NordrheinWestfalen/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/NordrheinWestfalen/txt",
                        "filter": True
                        },
        "Hessen":       {"landtag": "Hessischer Landtag",
                        "origin_path": "/resources/corpora/parlamentary_germany/Hessen/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Hessischer Landtag - Landtagsinformationssystem",
                        "date_func": (lambda file_path: date_niedersachsen(file_path)),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt"))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Hessen/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Hessen/txt",
                        "filter": True
                        },
        "Saarland":     {"landtag": "Landtag des Saarlandes",
                        "origin_path": "/resources/corpora/parlamentary_germany/Saarland/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Landtag des Saarlandes",
                        "date_func": (lambda file_path: date_niedersachsen(file_path)),
                        "subtitle": (lambda file_path: str(int(file_path.split("/")[-2])) + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt").split("_")[-1].split("-")[-1])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Saarland/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Saarland/txt",
                        "filter": True
                        },
        "Baden-W":      {"landtag": "Landtag von Baden-Württemberg",
                        "origin_path": "/resources/corpora/parlamentary_germany/BadenWuertemberg/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Parlamentsinformationssystem von Baden-Württemberg",
                        "date_func": (lambda file_path: file_path.split("/")[-1].split(" ")[2]),
                        "subtitle": (lambda file_path: file_path.split("/")[-2] + ".Wahlperiode__" + str(int(file_path.split("/")[-1].split(" ")[1].split("_")[-1])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "BadenWuertemberg/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/BadenWuertemberg/txt",
                        "filter": True
                        },
        "B-Rat":        {"landtag": "Deutsche Bundesrat",
                        "origin_path": "/resources/corpora/parlamentary_germany/Bundesrat/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Dokumentations- und Informationssystem des Bundesrates",
                        "date_func": (lambda file_path: file_path.split("/")[-1].split(" ")[-1].rstrip(".txt")),
                        "subtitle": (lambda file_path: str(sitzungsnummer_bundesrat(file_path)) + ".Wahlperiode__" + str(int(file_path.split("/")[-1].split(" ")[1].rstrip("."))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Bundesrat/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Bundesrat/txt",
                        "filter": True
                        },
        "Ö":            {"landtag": "Österreichische Nationalrat",
                        "origin_path": "/resources/corpora/parlamentary_germany/Oesterreich/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Open Government Data (OGD)",
                        "date_func": (lambda file_path: file_path.split("/")[-1].split("_")[0]),
                        "subtitle": (lambda file_path: str(wahlperiode_oesterreich(file_path)) + ".Wahlperiode__" + str(int(file_path.split("/")[-1].split("_")[1].split(" ")[0].rstrip("."))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Oesterreich/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Oesterreich/txt",
                        "filter": True
                        },
        "Thuer":        {"landtag": "Thüringer Landtag",
                        "origin_path": "/resources/corpora/parlamentary_germany/Thueringen/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Thüringer Landtag",
                        "date_func": (lambda file_path: date_pfalz(file_path)),
                        "subtitle": (lambda file_path: str(int(file_path.split("/")[-2])) + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt").split("_")[-1])) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Thueringen/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Thueringen/txt",
                        "filter": True
                        },
        "Sachsen":      {"landtag": "Sächsischer Landtag",
                        "origin_path": "/resources/corpora/parlamentary_germany/Sachsen/pdf",
                        "user1": "abrami",
                        "user2": "hammerla",
                        "quelle": "Sächsischer Landtag",
                        "date_func": (lambda file_path: date_pfalz(file_path)),
                        "subtitle": (lambda file_path: str(int(file_path.split("/")[-2])) + ".Wahlperiode__" + str(int(file_path.split("/")[-1].rstrip(".txt"))) + ".Sitzung"),
                        "save_path": (lambda file_path: create_dirs(os.path.join(XMI_CORPUS_PATH, "Sachsen/xmi", "/".join(file_path.split("/")[-2:-1])))),
                        "dir_path": "/resources/corpora/parlamentary_germany/Sachsen/txt",
                        "filter": True
                        }


        }

def valid_xml_char_ordinal(c: Union[str, bytes]) -> bool:
    codepoint = ord(c)
    # conditions ordered by presumed frequency
    return (
        0x20 <= codepoint <= 0xD7FF or
        codepoint in (0x9, 0xA, 0xD) or
        0xE000 <= codepoint <= 0xFFFD or
        0x10000 <= codepoint <= 0x10FFFF
        )

def create_dirs(dir_path: str) -> str:
    """
    Help function to create directories, if they dont exist.
    :param dir_path:
    :return:
    """
    pathlib.Path(dir_path).mkdir(parents=True, exist_ok=True)
    return dir_path


def current_milli_time() -> Optional[int]:
    """
    returns timestamp in milliseconds.
    :return:
    """
    return round(time.time() * 1000)

def date_liechtenstein(filepath:str):
    date = filepath.split("/")[-1].rstrip(".txt").split("_")
    return f"{date[-1]}.{date[-2]}.{date[-3]}"

def wahlperiode_liechtenstein(filepath:str) -> Optional[int]:
    perioden = [("02.02.1997", "07.02.2001"),
                ("08.02.2001", "13.03.2005"),
                ("14.03.2005", "08.02.2009"),
                ("09.02.2009", "03.02.2013"),
                ("04.02.2013", "05.02.2017"),
                ("06.02.2017", "07.02.2021"),
                ("07.02.2021", "07.02.2025")]
    current = datetime.strptime(date_liechtenstein(filepath), '%d.%m.%Y')
    for i in range(0, len(perioden)):
        start = datetime.strptime(perioden[i][0], '%d.%m.%Y')
        end = datetime.strptime(perioden[i][-1], '%d.%m.%Y')
        if start <= current <= end:
            return i + 1
        else:
            pass

def sitzungs_nr_liechtenstein(filepath: str) -> Optional[int]:
    sub_sub_dir = "/".join(filepath.split("/")[:-2])
    sub_dirs = [os.path.join(sub_sub_dir, file) for file in os.listdir(sub_sub_dir)]

    current = datetime.strptime(date_liechtenstein(filepath), '%d.%m.%Y')
    current_wp = wahlperiode_liechtenstein(filepath)

    all_dates = []
    for sub_dir in sub_dirs:
        files = [os.path.join(sub_dir, file) for file in os.listdir(sub_dir)]
        try:
            files.remove(filepath)
        except:
            pass
        files_wp = []
        for file in files:
            if wahlperiode_liechtenstein(file) == current_wp:
                files_wp.append(file)
            else:
                pass
        other_dates = [datetime.strptime(date_liechtenstein(file), '%d.%m.%Y') for file in files_wp]
        all_dates.extend(other_dates)

    all_dates = all_dates + [current]

    for i in range(0, len(all_dates)):
        cur_date = all_dates[i]
        j = i
        while j > 0 and all_dates[j - 1] > cur_date:
            all_dates[j] = all_dates[j - 1]
            j = j - 1
        all_dates[j] = cur_date
    for index, item in enumerate(all_dates):
        if item == current:
            return index + 1


def date_hamburg(filepath:str) -> Optional[str]:
    """
    Function to get the date for a document from hamburg corpus.
    :param filepath:
    :return:
    """
    pattern1 = re.compile("([0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9][0-9]|[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9])")
    pattern2 = re.compile("[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9]")
    with open(filepath, "r") as f:
        for line in f:
            line = line.replace(" ", "")
            line = line.strip()
            z = re.match(pattern1, line)
            if z:
                zz = re.match(pattern2, line)
                if zz:
                    if int(line[-2:]) < 40:
                        line = line[:6] + "20" + line[-2:]
                    else:
                        line = line[:6] + "19" + line[-2:]
                else:
                    pass
                return line

def date_sachsen_anhalt(filepath:str) -> Optional[str]:
    """
    Function to get the date for a document from sachsen_anhalt corpus.
    :param filepath:
    :return:
    """
    pattern = re.compile("[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9][0-9]")
    with open(filepath, "r") as f:
        for line in f:
            line = line.replace(" ", "")
            line = line.strip()
            z = re.match(pattern, line)
            if z:
                res = re.sub('[^\d\.]', '', line)
                try:
                    date_time_obj = datetime.strptime(res, '%d.%m.%Y')
                    return res
                except:
                    pass


def date_brandenburg(file_path:str) -> Optional[str]:
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
            for month in months:
                if month[-1] in line:
                    try:
                        day, year = line.split(month[-1])
                        day, year = day.strip(), year.strip()
                        day = day.split(" ")[-1].strip(".")[-2:]
                        final_date = day + "." + str(month[0]) + "." + year[:4]
                        date_time_obj = datetime.strptime(final_date, '%d.%m.%Y')
                        return final_date
                    except:
                        pass

def date_berlin(file_path:str) -> Optional[str]:
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


def date_schleswig_holstein(filepath:str) -> Optional[str]:
    """
    Function to get the date for a document from sachsen_anhalt corpus.
    :param filepath:
    :return:
    """
    pattern = re.compile("[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9][0-9]")
    elems = filepath.split("/")[-1].split(" ")
    for elem in elems:
        elem = elem.replace(" ", "")
        elem = elem.strip()
        z = re.match(pattern, elem)
        if z:
            res = re.sub('[^\d\.]', '', elem)
            try:
                date_time_obj = datetime.strptime(res, '%d.%m.%Y')
                return res
            except:
                pass


def date_niedersachsen(file_path:str) -> Optional[str]:
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

def date_meckpom(file_path:str) -> Optional[str]:
    res = date_sachsen_anhalt(file_path)
    if res == None:
        return date_niedersachsen(file_path)
    else:
        return res

def date_pfalz(file_path:str) -> Optional[str]:
    res = date_berlin(file_path)
    if res == None:
        pattern = re.compile("[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9][0-9]")
        with open(file_path, "r") as f:
            for line in f:
                line = line.replace(" ", "")
                line = line.strip()
                line = re.sub('[^\d\.]', '', line)
                z = re.match(pattern, line)
                if z:
                    try:
                        date_time_obj = datetime.strptime(line, '%d.%m.%Y')
                        return line
                    except:
                        pass
    else:
        return res

def sitzungsnummer_bundesrat(file_path:str) -> int:
    year = file_path.split("/")[-2]
    year_dic = {
                "1949-1950":1,
                "1951-1955":2,
                "1956-1960":3,
                "1961-1965":4,
                "1966-1970":5,
                "1971-1975":6,
                "1976-1980":7,
                "1981-1985":8,
                "1986-1990":9,
                "1991-1995":10,
                "1996-2000":11,
                "2001-2005":12,
                "2006-2010":13,
                "2011-2015":14,
                "2016-2020":15,
                "2021-2025":16
                }
    return year_dic[year]

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


def save_txt_as_xmi(txt_path:str, landtag:str, datum: str,
                    typesystem:cassis.TypeSystem, user1:str, user2:str,
                    origin_path:str, quelle:str, subtilte_protocol:str,
                    save_path:str, mask_key:str) -> None:
    """
    landtag: parliament of the given protocol
    datum: date of the protocol with style: DD.MM.YYYY
    Function to save a txt file as apache uima xmi.
    :param txt_path:
    :param landtag:
    :param datum:
    :param typesystem:
    :return:
    """
    with codecs.open(txt_path, "r", "utf-8") as f:
        text = f.read()
    if MASK[mask_key]["filter"]:
        text = ''.join(c for c in text if valid_xml_char_ordinal(c))
    cas = cassis.Cas(typesystem=typesystem)

    cas.sofa_string = text
    cas.sofa_mime = "text"


    DocumentMetaData = typesystem.get_type("de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData")
    DocumentAnnotation = typesystem.get_type("org.texttechnologylab.annotation.DocumentAnnotation")
    DocumentModification = typesystem.get_type("org.texttechnologylab.annotation.DocumentModification")
    # DocumentMetaData
    document_title = landtag + "-Plenarprotokoll vom " + datum
    document_id = txt_path.split("/")[-1].replace(" ", "_").replace(".txt", ".xmi")
    # DocumentAnnotation
    date_time_obj = datetime.strptime(datum, '%d.%m.%Y')
    author = quelle
    subtitle = subtilte_protocol
    day = int(datum.split(".")[0])
    month = int(datum.split(".")[1])
    year = int(datum.split(".")[2])
    timestamp = int(datetime.timestamp(date_time_obj) * 1000)
    # DocumentModification
    user1 = user1
    user2 = user2
    comment1 = "Download"
    comment2 = "Transformation/Conversion"
    timestamp2 = int(os.path.getmtime(origin_path) * 1000)
    timestamp3 = int(current_milli_time())

    cas.add_all([
        DocumentMetaData(documentTitle=document_title, documentId=document_id),
        DocumentAnnotation(author=author, dateDay=day, subtitle=subtitle,
                           dateMonth=month, dateYear=year, timestamp=timestamp),
        DocumentModification(user=user1, timestamp=timestamp2, comment=comment1),
        DocumentModification(user=user2, timestamp=timestamp3, comment=comment2)
    ])

    cas.to_xmi(save_path + "/" + document_id)

    return


def save_directory_as_xmi(dir_path:str, mask_key:str, typesystem:str) -> List[Union[List[str], Set[str]]]:
    """
    Function saves a whole directory of txt-files as xmi. it needs directory path, a typesystem for
    apache uima and a mask_key, which is ja identifier for the parliament of the given txt files.
    :param dir_path:
    :param mask_key:
    :param typesystem:
    :return:
    """
    with open('/home/s5935481/work4/parliament_crawler/src/convert_and_clean/TypeSystem.xml', 'rb') as f:
        typesystem = cassis.load_typesystem(f)
    files = [os.path.join(dir_path, file) for file in os.listdir(dir_path)]
    files = [file for file in files if os.path.isfile(file)]
    fails = []
    exceptions = set()
    for file in files:
        save_path = MASK[mask_key]["save_path"](file)

        try:
            save_txt_as_xmi(
                            txt_path=file,
                            landtag=MASK[mask_key]["landtag"],
                            datum=MASK[mask_key]["date_func"](file),
                            typesystem=typesystem,
                            user1=MASK[mask_key]["user1"],
                            user2=MASK[mask_key]["user2"],
                            origin_path=MASK[mask_key]["origin_path"],
                            quelle=MASK[mask_key]["quelle"],
                            subtilte_protocol=MASK[mask_key]["subtitle"](file),
                            save_path=save_path,
                            mask_key = mask_key
                            )
        except Exception as e:
            fails.append(file)
            exceptions.add(traceback.format_exc())
        """
        save_txt_as_xmi(
            txt_path=file,
            landtag=MASK[mask_key]["landtag"],
            datum=MASK[mask_key]["date_func"](file),
            typesystem=typesystem,
            user1=MASK[mask_key]["user1"],
            user2=MASK[mask_key]["user2"],
            origin_path=MASK[mask_key]["origin_path"],
            quelle=MASK[mask_key]["quelle"],
            subtilte_protocol=MASK[mask_key]["subtitle"](file),
            save_path=save_path,
            mask_key=mask_key
        )
        """
    return [fails, exceptions]


def parse_and_save_whole_corpus(mask_key:str, typesystem:str) -> None:
    dir_path = MASK[mask_key]["dir_path"]
    part_func = partial(save_directory_as_xmi, mask_key=mask_key, typesystem=typesystem)
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
    pool = Pool(28)
    result = list(tqdm(pool.imap_unordered(part_func, dir_with_txt),
                       desc="Parsing and Converting to XMI; Corpus: {}".format(mask_key)))
    pool.close()
    pool.join()
    fails = []
    exceptions = []
    for fail_list in result:
        for fail in fail_list[0]:
            fails.append(fail)
        for exception in fail_list[-1]:
            exceptions.append(exception)

    for fail in list(set(fails)):
        print(fail + "\n")

    for exception in list(set(exceptions)):
        print(exception)
        print("\n")
    print(len(fails))
    return


def main():
    typesystem = '/home/s5935481/work4/parliament_crawler/src/convert_and_clean/TypeSystem.xml'
    parse_and_save_whole_corpus("Thuer", typesystem)

if __name__ == "__main__":
    main()