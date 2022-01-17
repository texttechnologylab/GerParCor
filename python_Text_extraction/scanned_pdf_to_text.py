import multiprocessing
import argparse
from PIL import Image
import pytesseract
from pdf2image import convert_from_path
import os
from tqdm import tqdm
from os import makedirs
import numpy as np
import cv2
import pathlib
from multiprocessing import Pool
from functools import partial

set_files = set()


def pdf_to_image(pdf_path: str, dpi: int, lang: str, bad_quali: bool = False) -> (int, str):
    """
    Function takes pdf scan as input and converts it to single images (one per page).
    Returns amount of pages
    :param pdf_path: path of the pdf-file
    :param dpi: Dpi number of the converted pictures from the pdf pages
    :param lang: Language of Tesseract OCR
    :param bad_quali: True if the scanned pdfs have a bad quality otherwise False
    :return:
    """
    output_path = pdf_path.strip(".pdf") + "_image_safe"
    pathlib.Path(output_path).mkdir(parents=True, exist_ok=True)
    # Store all the pages of the PDF in a variable
    pages = convert_from_path(pdf_path, dpi)
    # Counter to store images of each page of PDF to image
    image_counter = 1
    # Iterate through all the pages stored above
    for page in pages:
        # Declaring filename for each page of PDF as JPG
        # For each page, filename will be:
        # PDF page 1 -> page_1.jpg
        # PDF page 2 -> page_2.jpg
        # PDF page 3 -> page_3.jpg
        # ....
        # PDF page n -> page_n.jpg
        filename = f"page_{image_counter}.jpg"
        # Save the image of the page in system
        page.save(f"{output_path}/{filename}", 'JPEG')
        if lang == "frk" or bad_quali:
            preprocess_bad_quality_text(f"{output_path}/{filename}")
        # Increment the counter to update filename
        image_counter += 1
    return image_counter, output_path


def preprocess_bad_quality_text(img_path: str):
    """
    :param img_path: path to image for rescale, convert the color from RGB to Gray, erode, dilate and remove/reduce the noises with a filter
    """
    # https://tesseract-ocr.github.io/tessdoc/ImproveQuality.html
    # https://nanonets.com/blog/ocr-with-tesseract/
    # base on: https://towardsdatascience.com/getting-started-with-tesseract-part-ii-f7f9a0899b3f & https://towardsdatascience.com/getting-started-with-tesseract-part-i-2a6a6b1cf75e
    img = cv2.imread(f"{img_path}", cv2.IMREAD_UNCHANGED)
    img_resize = cv2.resize(img, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)
    img_gray = cv2.cvtColor(img_resize, cv2.COLOR_BGR2GRAY)
    kernel = np.ones((1, 1), np.uint8)
    img_dilate = cv2.dilate(img_gray, kernel, iterations=1)
    img_erode = cv2.erode(img_dilate, kernel, iterations=1)
    img_bilateral = cv2.bilateralFilter(img_erode, 5, 75, 75)
    img_filter = cv2.threshold(img_bilateral, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1]
    img_threshold = cv2.adaptiveThreshold(cv2.bilateralFilter(img_filter, 9, 75, 75), 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 2)
    cv2.imwrite(img_path, img_threshold)


def image_to_text(image_path: str, pdf_path: str, file_limit: int, lang: str, out_dir: str) -> None:
    """
        Function converts images to text using OCR.
        :param pdf_path: path to the pdf-file
        :param file_limit:Number of converted Pages
        :param out_dir: output directory for the extraction
        :param image_path: path for the converted pages of the pdf-file
        :param lang: Language for Tesseract OCR extraction
    """
    # Variable to get count of total number of pages

    # Creating a text file to write the output

    # Open the file in append mode so that
    # All contents of all images are added to the same file
    txt_data_name = str(os.path.basename(pdf_path)).replace(".pdf", ".txt")
    txt_path = f"{out_dir}/{txt_data_name}"
    makedirs(os.path.dirname(txt_path), exist_ok=True)
    with open(txt_path, "w", encoding="UTF-8") as f:
        # Iterate from 1 to total number of pages
        for i in range(1, file_limit):
            # Set filename to recognize text from
            # Again, these files will be:
            # page_1.jpg
            # page_2.jpg
            # ....
            # page_n.jpg
            filename = f"page_{i}.jpg"

            # Recognize the text as string in image using pytesserct
            text = str((pytesseract.image_to_string(Image.open(f"{image_path}/{filename}"), lang=lang)))

            # The recognized text is stored in variable text
            # Any string processing may be applied on text
            # Here, basic formatting has been done:
            # In many PDFs, at line ending, if a word can't
            # be written fully, a 'hyphen' is added.
            # The rest of the word is written in the next line
            # Eg: This is a sample text this word here GeeksF-
            # orGeeks is half on first line, remaining on next.
            # To remove this, we replace every '-\n' to ''.
            text = text.replace('-\n', '')

            # Finally, write the processed text to the file.
            f.write(text)

        # Close the file after writing all the text.

    # Delete saved images
    for i in range(1, file_limit):
        filename = f"page_{i}.jpg"
        os.remove(f"{image_path}/{filename}")
    if len(os.listdir(image_path)) == 0:
        # removing the file using the os.remove() method
        os.rmdir(image_path)
    else:
        # messaging saying folder not empty
        print("Folder is not empty")
    return


def get_all_path_pdf(path_dir: str):
    """
    :param path_dir: Directory of the pdf-files
    :return: set Path of the pdf-Files
    """
    for file in os.scandir(path_dir):
        if file.is_dir():
            get_all_path_pdf(file)
        elif (str(file.path)).endswith(".pdf"):
            set_files.add(str(file.path))
            if len(set_files) % 100 == 0:
                print(len(set_files))


def scanned_pdf_to_text(pdf_path: str, out_name_dir: str, bad_quali: bool, dpi=200, lang="eng"):
    """
         Base-on:                        https://www.geeksforgeeks.org/python-reading-contents-of-pdf-using-ocr-optical-character-recognition/ +
                                        https://gitlab.texttechnologylab.org/Bagci/scannedpdftotext
         Overview possible languages:   https://tesseract-ocr.github.io/tessdoc/Data-Files-in-different-versions.html
        :param bad_quali: True if the scanned pdfs have a bad quality otherwise False
        :param out_name_dir: Directory for the output
        :param pdf_path: Directory of the pdf
        :param dpi: Dpi number of the converted pictures from the pdf pages
        :param lang: Language of Tesseract OCR
        :return: if the extraction was successfull
    """
    success = False
    try:
        counter, output_path = pdf_to_image(pdf_path, dpi, lang, bad_quali)
        image_to_text(output_path, pdf_path, counter, lang, out_name_dir)
        success = True
    except Exception as e:
        print(e)
        success = False
    return success


def scan_dir_to_text(dir_path: str, out_name_dir: str, bad_quali: bool, dpi: int = 200, lang: str = "deu"):
    """
    Function to convert whole direcotry.
    :param out_name_dir: Directory for the output
    :param bad_quali: True if the scanned pdfs have a bad quality otherwise False
    :param dir_path: Directory of all pdf-files
    :param dpi: Dpi number of the converted pictures from the pdf pages
    :param lang: Language of Tesseract OCR
    """
    part_func = partial(scanned_pdf_to_text, dpi=dpi, lang=lang, out_name_dir=out_name_dir, bad_quali=bad_quali)
    number_core = int(multiprocessing.cpu_count()/4)
    pool = Pool(number_core)
    # files = [os.path.join(dir_path, file) for file in os.listdir(dir_path)]
    global set_files
    set_files = set()
    get_all_path_pdf(dir_path)
    # print(set_files)
    files = list(set_files)
    result = list(tqdm(pool.imap_unordered(part_func, files),
                       desc=f"Converting files from: {dir_path.split('/')[-1]}", total=len(files)))
    pool.close()
    pool.join()
    successes, fails = 0, 0
    for i in result:
        if i:
            successes += 1
        else:
            fails += 1
    print(f"successes: {successes}, fails: {fails}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-p", "--path_directory", help="path to the directory with the .pdf files")
    parser.add_argument("-o", "--out", default="pdf_to_txt_out", help="directory path for the outputs of the Tessseract OCR extraction")
    parser.add_argument("-q", "--quali", default=False, help="Boolean True if scanned pdf documents have a bad quality otherwise False")
    parser.add_argument("-d", "--dpi", default=200, help="The dpi for converting ever page to a picture")
    parser.add_argument("-l", "--lang", default="deu", help="The language for the Tesseract OCR extraction")
    args = parser.parse_args()
    scan_dir_to_text(dir_path=args.path_directory, out_name_dir=args.out, bad_quali=args.quali, dpi=args.dpi, lang=args.lang)