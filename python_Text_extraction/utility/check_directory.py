import os
import pickle

def check_hamburg(path="/vol/s5935481/parlamentary/hamburg") -> None:
    pdf = path + "/pdf"
    txt = path + "/txt"
    per_pdf = os.listdir(pdf)
    all_dir, split_dir = [0, 0], [0, 0]
    with open(path + "/all_links.pickle", "rb") as f:
        links = pickle.load(f)
    print("=============================================================================")
    for per in per_pdf:
        files1 = os.listdir(pdf + "/" + per)
        files2 = os.listdir(txt + "/" + per)
        print("Downloaded   : " + per + ": number_pdf's: {}; number_txt's: {}".format(len(files1), len(files2)))
        print("Crawled Links: " + per + ": number_pdf's: {}".format(len(list(set(links[per])))))
        if per == "-- Alle --":
            all_dir = [len(files1), len(files2)]
        else:
            split_dir[0] += len(files1)
            split_dir[1] += len(files2)
        print("=============================================================================")
    print("Total_all  : number_pdf's: {}; number_txt's: {}".format(all_dir[0], all_dir[1]))
    print("Total_split: number_pdf's: {}; number_txt's: {}".format(split_dir[0], split_dir[1]))
    print("=============================================================================")


check_hamburg()