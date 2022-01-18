a = """/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_81.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_65.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_76.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_97.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_105.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_89.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_87.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_63.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/04/04_40.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_102.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_59.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_93.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_67.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_52.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_100.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_104.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_61.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_68.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_58.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_106.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_84.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_71.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_79.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_95.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_62.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_51.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_69.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_49.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/04/04_39.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_66.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_56.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_70.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_50.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_64.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_90.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_57.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_60.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_86.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_74.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_88.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_98.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_77.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_75.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_103.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_82.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_94.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_85.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_101.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/04/04_41.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_72.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_96.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_99.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_53.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_78.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_80.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_83.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_55.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_91.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_92.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/04/04_42.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_54.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_73.txt

/resources/corpora/parlamentary_germany/Thueringen/txt/03/03_107.txt

"""


a = a.split("\n")
a = [i for i in a if i != ""]
for i in a:
    with open(i, "r") as f:
        lines = f.readlines()
        if len(lines) > 0:
            print(i)
