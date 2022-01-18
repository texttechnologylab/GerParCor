import os

path = "/resources/corpora/parlamentary_germany/Saarland/pdf_clear"

dirs = [os.path.join(path, sub_dir) for sub_dir in os.listdir(path)]

for dir in dirs:
    files = [os.path.join(dir, file) for file in os.listdir(dir)]
    for file in files:
        new_name = os.path.join("/".join(file.split("/")[:-1]), file.split("/")[-1].replace(".PDF", ".pdf"))
        os.rename(file, new_name)
