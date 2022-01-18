import pathlib
from tqdm import tqdm
from PIL import Image
import pytesseract
import sys
from pdf2image import convert_from_path
import os
from multiprocessing import Pool
from functools import partial

def pdf_to_image(pdf_path:str, dpi:int) -> (int, str):
    """
    Function takes pdf scan as input and converts it to single images (one per page).
    Returns amount of pages
    :param pdf_path:
    :param output_path:
    :param dpi:
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
        # Increment the counter to update filename
        image_counter += 1
    return image_counter, output_path


def image_to_text(output_path:str, pdf_path:str, filelimit:int, lang:str) -> None:
    """
    Function converts images to text using OCR.
    :param output_path:
    :param output_name:
    :param image_counter:
    :param lang:
    :return:
    """
    # Variable to get count of total number of pages


    # Creating a text file to write the output


    # Open the file in append mode so that
    # All contents of all images are added to the same file
    with open(pdf_path.replace("pdf", "txt"), "w") as f:
        # Iterate from 1 to total number of pages
        for i in range(1, filelimit):
            # Set filename to recognize text from
            # Again, these files will be:
            # page_1.jpg
            # page_2.jpg
            # ....
            # page_n.jpg
            filename = f"page_{i}.jpg"

            # Recognize the text as string in image using pytesserct
            text = str(((pytesseract.image_to_string(Image.open(f"{output_path}/{filename}"), lang=lang))))

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
    for i in range(1, filelimit):
        filename = f"page_{i}.jpg"
        os.remove(f"{output_path}/{filename}")

    return

def scanned_pdf_to_text(pdf_path:str, dpi:int=200, lang:str="deu") -> bool:
    """
     Quelle:                        https://www.geeksforgeeks.org/python-reading-contents-of-pdf-using-ocr-optical-character-recognition/ +
                                    https://gitlab.texttechnologylab.org/Bagci/scannedpdftotext
     Overview possible languages:   https://tesseract-ocr.github.io/tessdoc/Data-Files-in-different-versions.html
    :param pdf_path:
    :param output_path:
    :param dpi:
    :param lang:
    :return:
    """
    success = False
    try:
        counter, output_path = pdf_to_image(pdf_path, dpi)
        image_to_text(output_path, pdf_path, counter, lang)
        success = True
    except Exception as e:
        print(e)
        success = False
    return success

def scan_dir_to_text(dir_path:str, dpi:int=200, lang:str="deu") -> None:
    """
    Function to convert whole direcotry.
    :param dir_path:
    :param dpi:
    :param lang:
    :return:
    """
    part_func = partial(scanned_pdf_to_text, dpi=dpi, lang=lang)
    pool = Pool(6)
    files = [os.path.join(dir_path, file) for file in os.listdir(dir_path)]
    result = list(tqdm(pool.imap_unordered(part_func, files),
                       desc=f"Converting files from: {dir_path.split('/')[-1]}"))
    pool.close()
    pool.join()
    successes, fails = 0, 0
    for i in result:
        if i:
            successes += 1
        else:
            fails += 1
    print(f"successes: {successes}, fails: {fails}")



def clean_directory(dir_path:str) -> None:
    """
    Function to clean directory from image_safe subdirectories, if
    programm fails and leaves them behind...
    :param dir_path:
    :return:
    """
    import os
    import shutil
    files = [os.path.join(dir_path, file) for file in os.listdir(dir_path)]
    for file in files:
        if "image_safe" in file:
            shutil.rmtree(file)


if __name__ == "__main__":
    # define Parameter
    input_dir = "/resources/corpora/parlamentary_germany/Bremen/pdf/14"
    scan_dir_to_text(input_dir)