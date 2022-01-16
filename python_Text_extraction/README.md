Needed packages:

    python version == 3.8
    sudo apt-get install tesseract-ocr
    sudo apt-get install tesseract-ocr-deu (for language German) 
    https://ocrmypdf.readthedocs.io/en/latest/languages.html
    https://tesseract-ocr.github.io/tessdoc/Data-Files-in-different-versions.html
    pip3 install tqdm
    pip3 install numpy
    pip3 install -U symspellpy
    pip3 install Pillow
    pip3 install pytesseract
    pip3 install pdf2image

Scanned PDF documents to text with Tesseract OCR extraction:

    1. Create python environment (python Version=3.8) (Example with Conda: conda create -n myenv python=3.8)
    2. Activate python environment (Example conda: conda activate myenv)
    3. Install all needed packages pip install -r requirements.txt (recommendation: install the needed libtaries via pip install library, because sometimes pip install -r requirements.txt, does not work)
    4. Download all languages (explanation: https://ocrmypdf.readthedocs.io/en/latest/languages.html) (language shortcuts: https://tesseract-ocr.github.io/tessdoc/Data-Files-in-different-versions.html)
    5.  start scanned_pdf_to_text.py: python scanned_pdf_to_text.py -p directory_path_to_the_pdf_files -o out_path_for_extracted_text -q boolean_if_bad_quality_pdfs -d dpi_for_converting_the_pages_of_pdfs_to_pictures -l language_shortcut_of_OCR_Tesseract

Spellchecker:

    1. install the python library sysmspellpy: pip install -U symspellpy
    2. Download your needed language word lexicon from https://github.com/wolfgarbe/SymSpell
        2.1. Put the downloaded word lexicon into your python-environment in the folder envs/python-environment_name/lib/python(Version_umber)/site-packages/symspellpy
        2.2 Example with miniconda3, environment=symspeller and python version3.8 ~/miniconda3/envs/symspeller/lib/python3.8/site-packages/symspellpy
    3. start Programm with python spellchecker.py -p path_to_txt_files_directory -s Symspeller_word_lexicon_name -o output_path_to_directory -m Output_name_modifier