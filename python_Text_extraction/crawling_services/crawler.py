import argparse
import pickle
from typing import Union, List, Tuple, Any

import urllib3
from selenium.webdriver.common.by import By
from selenium.webdriver.support.select import Select
from selenium.webdriver.support.wait import WebDriverWait
from tqdm import tqdm
import pathlib
import re
import requests
from bs4 import BeautifulSoup
import selenium
import os
from multiprocessing import Pool
from multiprocessing.pool import ThreadPool
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
import textract
import codecs
import time
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.proxy import Proxy, ProxyType
from selenium.webdriver.common.action_chains import ActionChains




def get_links(page_url: Union[str, bytes]) -> set:
    """
    get all links on webpage.
    :param page_url:
    :return:
    """
    pages = set()
    pattern = re.compile("^(/)")
    html = requests.get(page_url).text  # fstrings require Python 3.6+
    soup = BeautifulSoup(html, "html.parser")
    for link in soup.find_all("a", href=pattern):
        if "href" in link.attrs:
            if link.attrs["href"] not in pages:
                new_page = link.attrs["href"]
                print(new_page)
                pages.add(new_page)
                #get_links(new_page)

    return pages

def test() -> set:
    pages = set()
    from selenium import webdriver
    import time
    from bs4 import BeautifulSoup
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument("--enable-javascript")
    chrome_options.add_argument('--disable-dev-shm-usage')
    browser = webdriver.Chrome("/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                              options=chrome_options)


    url = "https://www.buergerschaft-hh.de/parldok/neuedokumente/1"
    browser.get(url)
    time.sleep(3)
    source = browser.page_source
    soup = BeautifulSoup(source, 'html.parser')
    pattern = re.compile("pdf")
    for link in soup.find_all("a", href=pattern):
        if "href" in link.attrs:
            if link.attrs["href"] not in pages:
                new_page = link.attrs["href"]
                print(new_page)
                pages.add(new_page)
    return pages


def get_proxies(proxy_path: str = "/vol/s5935481/parlamentary/BIN/proxy.txt") -> [str]:
    """
    Function for getting a list of working proxies.
    :param proxy_path:
    :return:
    """
    proxy_strings = []
    with open(proxy_path, "r") as f:
        for i in f:
            proxy_strings.append(i.strip())

    return proxy_strings

def get_proxy_driver(proxy_ip_port: str, chrome_options: Options,
                     driver_path: str = "/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver") -> webdriver.Chrome:
    """
    Function to create a webdriver instance with a proxy.
    :param proxy_ip_port:
    :param chrome_options:
    :param driver_path:
    :return:
    """
    proxy = Proxy()
    proxy.proxy_type = ProxyType.MANUAL
    proxy.http_proxy = proxy_ip_port
    proxy.ssl_proxy = proxy_ip_port

    capabilities = webdriver.DesiredCapabilities.CHROME
    proxy.add_to_capabilities(capabilities)

    driver = webdriver.Chrome(driver_path, options=chrome_options, desired_capabilities=capabilities)
    return driver

def brandenburg_proc_mp(config: list, make_directories: bool = True) -> List[List[Tuple[Any, Any]]]:
    """
    Function for downloading and saving (as pdf and txt) all "Plenumprotokolle" for brandenburg.
    But depricated--multiprocessing doesnt work, but single process version works fine and takes not long...
    :param config:
    :param make_directories:
    :return:
    """
    period, document_type, save_path = config
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument("--start-maximized")
    driver = webdriver.Chrome("/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                              options=chrome_options)

    driver.get("https://www.parlamentsdokumentation.brandenburg.de/ELVIS/index.html")
    time.sleep(10)
    select_item_electionperiods = Select(driver.find_element_by_id("LISSH_WP"))
    select_document_types = Select(driver.find_element_by_id("LISSH_DART"))
    select_item_electionperiods.select_by_visible_text(period)
    select_document_types.select_by_visible_text(document_type)
    button_items = driver.find_elements_by_class_name("button")
    for i in reversed(range(0, len(button_items))):
        if button_items[i].get_property("type") != "submit":
            del button_items[i]
    button_item = button_items[0]
    button_item.click()
    time.sleep(10)
    document_types_dropdown_menu = Select(driver.find_element_by_name("LISSH_Browse_ReportFormatList"))
    document_types_dropdown_menu.select_by_visible_text("Dokumente")
    time.sleep(10)
    items_per_page_dropdown = Select(driver.find_element_by_name("NumPerSegment"))
    items_per_page_dropdown.select_by_visible_text("alle")
    time.sleep(10)
    all_links = driver.find_elements_by_css_selector("[title^='Gesamtdokument']")
    all_links = list(set([item.get_property("href") for item in all_links]))
    driver.quit()
    if make_directories:
        try:
            pathlib.Path(save_path + "/pdf").mkdir(parents=True, exist_ok=False)
            pathlib.Path(save_path + "/txt").mkdir(parents=True, exist_ok=False)
        except:
            print("one of the directories already exists")
    file_names = []
    file_names_failed = []
    for link in tqdm(all_links, desc="Process: {}".format(period)):
        response = requests.get(link)
        file_name = save_path + "/pdf" + "/" + "_".join(link.split("/")[6:])

        try:
            with open(file_name, 'wb') as f:
                f.write(response.content)
            file_names.append((link, file_name))
        except:
            file_names_failed.append((link, file_name))

    file_names_failed_2 = []
    for file_name in file_names:
        try:
            text = textract.process(file_name[-1])
            text = text.decode("utf-8").split("\n")
            with codecs.open(save_path + "/txt/" + file_name[-1].split("/")[-1], "w", "utf-8") as f:
                for line in text:
                    text.write(line + "\n")
        except:
            file_names_failed_2.append(file_name)

    for failed in file_names_failed_2:
        file_names.remove(failed)
    file_names_failed = file_names_failed + file_names_failed_2
    return [file_names, file_names_failed]

def brandenburg_crawler_mp(make_directories: bool = True,
                        save_path: str = "/vol/s5935481/parlamentary/brandenburg") -> None:
    """
    Function for downloading and saving (as pdf and txt) all "Plenumprotokolle" for brandenburg.
    But depricated--multiprocessing doesnt work, but single process version works fine and takes not long...
    :param make_directories:
    :param save_path:
    :return:
    """

    if make_directories:
        try:
            pathlib.Path(save_path).mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    driver = webdriver.Chrome("/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                              options=chrome_options)
    driver.get("https://www.parlamentsdokumentation.brandenburg.de/ELVIS/index.html")
    select_item_electionperiods = Select(driver.find_element_by_id("LISSH_WP"))
    electionperiods = [i.text for i in select_item_electionperiods.options][:-1]
    select_document_types = Select(driver.find_element_by_id("LISSH_DART"))
    document_types = [i.text for i in select_document_types.options][1:]
    configs = []
    for period in tqdm(electionperiods):
        if make_directories:
            try:
                pathlib.Path(save_path + "/" + period).mkdir(parents=True, exist_ok=False)
            except:
                print(save_path + "/" + period + " already exists...")

        configs.append([period, "Plenarprotokoll", save_path + "/" + period])

    driver.quit()
    del driver

    print("Start Downloading")
    pool = Pool(len(electionperiods))
    results = pool.map(brandenburg_proc_mp, configs)
    pool.close()
    pool.join()

    with open(save_path + "/stats.txt", "w") as f:
        correct = []
        failed = []
        for result in results:
            correct.extend([res[0] for res in result[0]])
            failed.extend([res[0] for res in result[1]])
        f.write("completed requests + conversions : {}".format(len(correct)) + "\n")
        f.write("failed requests + conversions    : {}".format(len(failed)) + "\n")
        f.write("total requests                   : {}".format(len(correct) + len(failed)) + "\n")
        f.write("==========completed requests==========" + "\n")
        for com in correct:
            f.write(com + "\n")
        f.write("==========failed requests==========" + "\n")
        for fail in failed:
            f.write(fail + "\n")
    return


def brandenburg_proc_sp(config: list, make_directories: bool = True) -> List[List[Tuple[Any, Any]]]:
    """
     Function for downloading and saving (as pdf and txt) all "Plenumprotokolle" for brandenburg.

    :param config:
    :param make_directories:
    :return:
    """
    period, document_type, save_path = config
    print(period, document_type)
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument("--start-maximized")
    driver = webdriver.Chrome("/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                              options=chrome_options)
    driver.get("https://www.parlamentsdokumentation.brandenburg.de/ELVIS/index.html")
    time.sleep(5)
    select_item_electionperiods = Select(driver.find_element_by_id("LISSH_WP"))
    select_document_types = Select(driver.find_element_by_id("LISSH_DART"))
    select_item_electionperiods.select_by_visible_text(period)
    select_document_types.select_by_visible_text(document_type)
    button_items = driver.find_elements_by_class_name("button")
    for i in reversed(range(0, len(button_items))):
        if button_items[i].get_property("type") != "submit":
            del button_items[i]
    button_item = button_items[0]
    button_item.click()
    time.sleep(5)
    document_types_dropdown_menu = Select(driver.find_element_by_name("LISSH_Browse_ReportFormatList"))
    document_types_dropdown_menu.select_by_visible_text("Dokumente")
    time.sleep(5)
    items_per_page_dropdown = Select(driver.find_element_by_name("NumPerSegment"))
    items_per_page_dropdown.select_by_visible_text("alle")
    time.sleep(5)
    all_links = driver.find_elements_by_css_selector("[title^='Gesamtdokument']")
    all_links = list(set([item.get_property("href") for item in all_links]))
    if make_directories:
        try:
            pathlib.Path(save_path + "/pdf").mkdir(parents=True, exist_ok=False)
            pathlib.Path(save_path + "/txt").mkdir(parents=True, exist_ok=False)
        except:
            print("one of the directories already exists")
    driver.quit()
    file_names = []
    file_names_failed = []
    good, bad = 0, 0
    exceptions = []
    for link in tqdm(all_links, desc="Downloading and Saving: {}".format(period)):
        response = requests.get(link)
        file_name = save_path + "/pdf" + "/" + ".".join(("_".join(link.split("/")[6:])).split(".")[0:-1]) + ".pdf"
        try:
            with open(file_name, 'wb') as f:
                f.write(response.content)
            file_names.append((link, file_name))
            good += 1
        except Exception as e:
            file_names_failed.append((link, file_name))
            exceptions.append(str(e))
            bad += 1
    print("completed requests: {}; failed requests: {}\n".format(good, bad))
    print("Exceptions occured: {}\n".format(",".join(list(set(exceptions)))))
    file_names_failed_2 = []
    good, bad = 0, 0
    exceptions = []
    for file_name in tqdm(file_names, desc="Converting to txt: {}".format(period)):
        try:
            text = textract.process(file_name[-1])
            text = text.decode("utf-8").split("\n")
            with codecs.open(save_path + "/txt/" + ".".join(file_name[-1].split("/")[-1].split(".")[0:-1]) + ".txt", "w", "utf-8") as f:
                for line in text:
                    f.write(line + "\n")
            good += 1
        except Exception as e:
            exceptions.append(str(e))
            file_names_failed_2.append(file_name)
            bad += 1
    print("completed conversions: {}; failed conversions: {}\n".format(good, bad))
    print("Exceptions occured: {}\n".format(",".join(list(set(exceptions)))))

    for failed in file_names_failed_2:
        file_names.remove(failed)
    file_names_failed = file_names_failed + file_names_failed_2

    return [file_names, file_names_failed]

def brandenburg_crawler_sp(make_directories: bool = True,
                        save_path: str = "/vol/s5935481/parlamentary/brandenburg") -> None:
    """
    Function for downloading and saving (as pdf and txt) all "Plenumprotokolle" for brandenburg.
    :param make_directories:
    :param save_path:
    :return:
    """
    if make_directories:
        try:
            pathlib.Path(save_path).mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    driver = webdriver.Chrome("/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                              options=chrome_options)
    driver.get("https://www.parlamentsdokumentation.brandenburg.de/ELVIS/index.html")
    select_item_electionperiods = Select(driver.find_element_by_id("LISSH_WP"))
    electionperiods = [i.text for i in select_item_electionperiods.options][:-1]
    select_document_types = Select(driver.find_element_by_id("LISSH_DART"))
    document_types = [i.text for i in select_document_types.options][1:]
    configs = []
    for period in tqdm(electionperiods):
        if make_directories:
            try:
                pathlib.Path(save_path + "/" + period).mkdir(parents=True, exist_ok=False)
            except:
                print(save_path + "/" + period + " already exists...")

        configs.append([period, "Plenarprotokoll", save_path + "/" + period])
    driver.quit()
    del driver
    print("Start Downloading")

    results = []
    for config in configs:
        results.append(brandenburg_proc_sp(config))

    with open(save_path + "/stats.txt", "w") as f:
        correct = []
        failed = []
        for result in results:
            correct.extend([res[0] for res in result[0]])
            failed.extend([res[0] for res in result[1]])
        f.write("completed requests + conversions : {}".format(len(correct)) + "\n")
        f.write("failed requests + conversions    : {}".format(len(failed)) + "\n")
        f.write("total requests                   : {}".format(len(correct) + len(failed)) + "\n")
        f.write("==========completed requests==========" + "\n")
        for com in correct:
            f.write(com + "\n")
        f.write("==========failed requests==========" + "\n")
        for fail in failed:
            f.write(fail + "\n")
    return



def hamburg_crawler_depricated(make_directories:bool = True,
                    save_path: str = "/vol/s5935481/parlamentary/hamburg") -> None:
    """
    Function for crawling plenary minutes from parliament of hamburg
    :param make_directories:
    :param save_path:
    :return:
    """

    if make_directories:
        try:
            pathlib.Path(save_path).mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument("--enable-javascript")
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    driver = webdriver.Chrome("/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                              options=chrome_options)

    url = "https://www.buergerschaft-hh.de/parldok/neuedokumente"
    driver.get(url)
    time.sleep(5)
    checkbox = driver.find_element_by_xpath("/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[1]/td[2]/input[2]")
    driver.execute_script("arguments[0].setAttribute('checked', 'true')", checkbox)
    button = driver.find_element_by_xpath("/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[2]/td[2]/input")
    button.submit()
    pages = set()
    for i in tqdm(range(2, 12), desc="Crawling links"):
        source = driver.page_source
        soup = BeautifulSoup(source, 'html.parser')
        pattern = re.compile("pdf")
        for link in soup.find_all("a", href=pattern):
            if "href" in link.attrs:
                if link.attrs["href"] not in pages:
                    new_page = link.attrs["href"]
                    pages.add(new_page)
        if i < 11:
            driver.get(url + "/" + str(i))
            time.sleep(20)
    print("{} Links were collected...".format(len(pages)))
    pages = list(pages)
    pages = ["https://www.buergerschaft-hh.de" + link for link in pages]
    if make_directories:
        try:
            pathlib.Path(save_path + "/pdf").mkdir(parents=True, exist_ok=False)
            pathlib.Path(save_path + "/txt").mkdir(parents=True, exist_ok=False)
        except:
            print("one of the directories already exists")
    file_names = []
    good, bad = 0,0
    file_names_failed = []
    exceptions = []
    for link in tqdm(pages, desc="Downloading all files: "):
        electoral_term = "electoral_term_" + link.split("_")[1]
        try:
            pathlib.Path(save_path + "/pdf/" + electoral_term).mkdir(parents=True, exist_ok=False)
            pathlib.Path(save_path + "/txt/" + electoral_term).mkdir(parents=True, exist_ok=False)
        except:
            pass
        response = requests.get(link)
        file_name = save_path + "/pdf/" + electoral_term + "/" + link.split("/")[-1]
        try:
            with open(file_name, 'wb') as f:
                f.write(response.content)
            file_names.append((link, file_name))
            good += 1
        except Exception as e:
            file_names_failed.append((link, file_name))
            exceptions.append(str(e))
            bad += 1
    print("completed requests: {}; failed requests: {}\n".format(good, bad))
    print("Exceptions occured: {}\n".format(",".join(list(set(exceptions)))))

    file_names_failed_2 = []
    good, bad = 0, 0
    exceptions = []
    for file_name in tqdm(file_names, desc="Converting to txt"):
        try:
            text = textract.process(file_name[-1])
            text = text.decode("utf-8").split("\n")
            with codecs.open(file_name[-1].replace("pdf", "txt"),
                             "w", "utf-8") as f:
                for line in text:
                    f.write(line + "\n")
            good += 1
        except Exception as e:
            exceptions.append(str(e))
            file_names_failed_2.append(file_name)
            bad += 1
    print("completed conversions: {}; failed conversions: {}\n".format(good, bad))
    print("Exceptions occured: {}\n".format(",".join(list(set(exceptions)))))
    driver.quit()
    return


def hamburg_crawler(make_directories:bool = True,
                    save_path: str = "/vol/s5935481/parlamentary/hamburg",
                    driver_path: str = "/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                    proxy_path : str = "/vol/s5935481/parlamentary/BIN/proxy.txt") -> None:
    """
    Function for crawling plenary minutes from parliament of hamburg
    :param make_directories:
    :param save_path:
    :return:
    """
    proxies = get_proxies(proxy_path=proxy_path)

    if make_directories:
        try:
            pathlib.Path(save_path).mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
    chrome_options = Options()
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)
    chrome_options.add_argument("--disable-extensions")
    chrome_options.add_argument("window-size=1920,1080")
    chrome_options.add_argument("--dns-prefetch-disable")

    chrome_options.headless = True
    chrome_options.add_argument("--enable-javascript")
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    url = "https://www.buergerschaft-hh.de/parldok/dokumentennummer"
    for prox in proxies:
        try:
            driver = get_proxy_driver(prox, chrome_options, driver_path)
            driver.set_page_load_timeout(5)
            driver.get(url)
            time.sleep(2)
            break
        except:
            pass

    """                                      /html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[1]/td[2]/input[2]                       
    checkbox = driver.find_element_by_xpath("/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[1]/td[2]/input[2]")
    driver.execute_script("arguments[0].setAttribute('checked', 'true')", checkbox)
    """
    try:
        cooki_button = driver.find_element_by_xpath("/html/body/div[1]/div/div/a").click()
        time.sleep(1)
    except:
        pass

    select_item = Select(driver.find_element_by_xpath("/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[2]/td[2]/select"))
    election_periods = select_item.options
    election_periods = [i.text for i in election_periods]
    election_periods = list(reversed(election_periods))
    link_dict = dict()

    driver.quit()
    if make_directories:
        try:
            pathlib.Path(save_path + "/pdf").mkdir(parents=True, exist_ok=False)
            pathlib.Path(save_path + "/txt").mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
    avail_prox = proxies[:]
    avail_count = 0
    for ep in tqdm(range(0, len(election_periods)), desc="Extracting Download Links: "):
        period = election_periods[ep]
        if make_directories:
            try:
                pathlib.Path(save_path + "/pdf/" + period).mkdir(parents=True, exist_ok=False)
                pathlib.Path(save_path + "/txt/" + period).mkdir(parents=True, exist_ok=False)
            except:
                print(save_path + " already exists...")


        for pid in range(avail_count, len(avail_prox) + avail_count):
            try:
                driver = get_proxy_driver(avail_prox[pid % len(avail_prox)], chrome_options, driver_path)
                driver.set_page_load_timeout(5)
                driver.get(url)
                time.sleep(2)
                checkbox = driver.find_element_by_xpath(
                    "/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[1]/td[2]/input[2]")
                driver.execute_script("arguments[0].setAttribute('checked', 'true')", checkbox)
                select_item = Select(
                    driver.find_element_by_xpath(
                        "/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[2]/td[2]/select"))
                select_item.select_by_visible_text(period)
                print("Using: {}".format(avail_prox[pid % len(avail_prox)]))
                avail_count += 1
                break
            except:
                avail_count += 1

        try:
            cooki_button = driver.find_element_by_xpath("/html/body/div[1]/div/div/a").click()
            time.sleep(1)
        except:
            pass

        doc_number = 1
        pages = set()
        running = True
        pbar = tqdm(total=100, leave=False)
        while running:
            try:
                c = 0
                query = "arguments[0].setAttribute('value', '{}')".format(str(doc_number))
                doc_selector = driver.find_element_by_xpath(
                    "/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[3]/td[2]/div[1]/input")
                driver.execute_script(query, doc_selector)
                button = driver.find_element_by_xpath("/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[4]/td[2]/input")
                button.submit()
                time.sleep(3)
                source = driver.page_source
                soup = BeautifulSoup(source, 'html.parser')
                pattern = re.compile("pdf")
                for link in soup.find_all("a", href=pattern):
                    if "href" in link.attrs:
                        if link.attrs["href"] not in pages:
                            new_page = link.attrs["href"]
                            pages.add(new_page)
                            c += 1
                if c == 0:
                    running = False
                else:
                    doc_number += 1
                                                          # /html/body/div[4]/div[2]/div/input
                back_button = driver.find_element_by_xpath("/html/body/div[4]/div[2]/div/input")
                driver.execute_script("arguments[0].click();", back_button)
                time.sleep(3)
                pbar.update(1)
            except:
                for pid in range(avail_count, len(avail_prox) + avail_count):
                    try:
                        driver = get_proxy_driver(avail_prox[pid % len(avail_prox)], chrome_options, driver_path)
                        driver.set_page_load_timeout(5)
                        driver.get(url)
                        time.sleep(2)
                        checkbox = driver.find_element_by_xpath(
                            "/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[1]/td[2]/input[2]")
                        driver.execute_script("arguments[0].setAttribute('checked', 'true')", checkbox)
                        select_item = Select(
                            driver.find_element_by_xpath(
                                "/html/body/div[4]/div[2]/div/form/fieldset/table/tbody/tr[2]/td[2]/select"))
                        select_item.select_by_visible_text(period)
                        print("Using: {}".format(avail_prox[pid % len(avail_prox)]))
                        avail_count += 1
                        break
                    except:
                        avail_count += 1
                    try:
                        cooki_button = driver.find_element_by_xpath("/html/body/div[1]/div/div/a").click()
                        time.sleep(1)
                    except:
                        pass





        pbar.close()
        link_dict[period] = ["https://www.buergerschaft-hh.de" + link for link in list(pages)]
        driver.quit()

    with open(save_path + "/all_links.pickle", "wb") as handle:
        pickle.dump(link_dict, handle)

    good, bad = 0, 0

    exceptions = []
    name_dict = dict()
    indx = 0
    for key in tqdm(link_dict, desc="Downloading files: "):
        file_names = []
        for link in link_dict[key]:
            prox_id = 0
            try:
                while_count = 0
                while True:
                    if while_count > len(proxies):
                        raise ValueError('No proxy works for link')
                    try:
                        response = requests.get(link, proxies={'http': proxies[prox_id % len(proxies)], 'https': proxies[prox_id % len(proxies)]}, timeout=2)
                        break
                    except:
                        prox_id += 1
                file_name = save_path + "/pdf/" + key + "/" + link.split("/")[-1]
                with open(file_name, 'wb') as f:
                    f.write(response.content)
                file_names.append(file_name)
                good += 1
            except Exception as e:
                exceptions.append(str(e))
                bad += 1
            prox_id += 1
        name_dict[key] = file_names
        indx += 1

    print("completed requests: {}; failed requests: {}\n".format(good, bad))
    print("Exceptions occured: {}\n".format(",".join(list(set(exceptions)))))

    good, bad = 0, 0
    exceptions = []
    for key in tqdm(name_dict, desc="Converting from PDF to TXT: "):
        for file_name in name_dict[key]:
            try:
                text = textract.process(file_name)
                text = text.decode("utf-8").split("\n")
                with codecs.open(file_name.replace("pdf", "txt"),
                                 "w", "utf-8") as f:
                    for line in text:
                        f.write(line + "\n")
                good += 1
            except Exception as e:
                exceptions.append(str(e))
                bad += 1
        print("completed conversions: {}; failed conversions: {}\n".format(good, bad))
        print("Exceptions occured: {}\n".format(",".join(list(set(exceptions)))))
    driver.quit()
    return





def bayern_crawler(make_directories:bool = True,
                   save_path: str = "/vol/s5935481/parlamentary/bayern",
                   chrome_driver: str = "/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver") -> None:
    """

    :param make_directories:
    :param save_path:
    :param chrome_driver:
    :return:
    """

    if make_directories:
        try:
            pathlib.Path(save_path).mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + "/pdf" + " already exists...")
        try:
            pathlib.Path(save_path + "/pdf").mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
        try:
            pathlib.Path(save_path + "/txt").mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + "/txt" + " already exists...")



    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--window-size=1920x1080")
    chrome_options.add_argument("--disable-notifications")
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--verbose')
    chrome_options.add_experimental_option("prefs", {
        #"download.default_directory": "/home/leon/Downloads",
        "download.prompt_for_download": False,
        "download.directory_upgrade": True,
        "safebrowsing_for_trusted_sources_enabled": False,
        "safebrowsing.enabled": False
    })
    chrome_options.add_argument('--disable-gpu')
    chrome_options.add_argument('--disable-software-rasterizer')

    # initialize driver object and change the <path_to_chrome_driver> depending on your directory where your chromedriver should be
    driver = webdriver.Chrome(options=chrome_options, executable_path=chrome_driver)

    # change the <path_to_place_downloaded_file> to your directory where you would like to place the downloaded file
    download_dir = save_path

    driver.command_executor._commands["send_command"] = ("POST", '/session/$sessionId/chromium/send_command')
    params = {'cmd': 'Page.setDownloadBehavior', 'params': {'behavior': 'allow', 'downloadPath': download_dir}}
    driver.execute("send_command", params)


    driver.get("https://www.bayern.landtag.de/webangebot2/webangebot/protokolle;jsessionid=BCBE10883F579A92447E7C06D2DF0E90?execution=e1s1#")

    election_periods_select_item = Select(driver.find_element_by_xpath("/html/body/div[1]/main/div[2]/div[2]/div[2]/div[1]/div/div/div[1]/div/div[1]/div[4]/form/div/div[2]/div[2]/span/select[1]"))
    election_periods = [option.text for option in election_periods_select_item.options]
    election_periods = list(reversed(election_periods))
    driver.quit()
    for period in tqdm(election_periods):
        if os.path.isdir(save_path + "/pdf/" + period):
            pass
        else:
            if make_directories:
                try:
                    pathlib.Path(save_path + "/pdf/" + period).mkdir(parents=True, exist_ok=False)
                    pathlib.Path(save_path + "/txt/" + period).mkdir(parents=True, exist_ok=False)
                except:
                    print(save_path + " already exists...")

            driver = webdriver.Chrome(options=chrome_options, executable_path=chrome_driver)
            download_dir = save_path + "/pdf/" + period
            driver.command_executor._commands["send_command"] = ("POST", '/session/$sessionId/chromium/send_command')
            params = {'cmd': 'Page.setDownloadBehavior', 'params': {'behavior': 'allow', 'downloadPath': download_dir}}
            driver.execute("send_command", params)
            driver.get(
                "https://www.bayern.landtag.de/webangebot2/webangebot/protokolle;jsessionid=BCBE10883F579A92447E7C06D2DF0E90?execution=e1s1#")

            election_periods_select_item = Select(driver.find_element_by_xpath(
                "/html/body/div[1]/main/div[2]/div[2]/div[2]/div[1]/div/div/div[1]/div/div[1]/div[4]/form/div/div[2]/div[2]/span/select[1]"))
            election_periods_select_item.select_by_visible_text(period)
            confirm = driver.find_element_by_xpath("/html/body/div[1]/main/div[2]/div[2]/div[2]/div[1]/div/div/div[1]/div/div[1]/div[4]/form/div/div[2]/div[2]/input[1]")
            confirm.click()
            time.sleep(3)
            body = driver.find_element_by_xpath("/html/body/div[1]/main/div[2]/div[2]/div[2]/div[1]/div/div/div[1]/div/div[1]/div[4]/form/div/div[3]/div/table/tbody")
            len_idx = len(body.find_elements_by_xpath("//a[@title='Protokoll als PDF-Datei']"))
            for idx in tqdm(range(0, len_idx), desc="Downloading {}".format(period)):
                body = driver.find_element_by_xpath(
                    "/html/body/div[1]/main/div[2]/div[2]/div[2]/div[1]/div/div/div[1]/div/div[1]/div[4]/form/div/div[3]/div/table/tbody")
                button = body.find_elements_by_xpath("//a[@title='Protokoll als PDF-Datei']")[idx]
                button.click()
                button.submit()
            driver.quit()

    return




def sachsen_anhalt_crawler(make_directories:bool = True,
                   save_path: str = "/tmp/sachsen_anhalt",
                   chrome_driver: str = "/home/stud_homes/s5935481/work4/parliament_crawler/src/crawling_services/chromedriver",
                   load_checkpoint : bool = True) -> None:

    if make_directories:
        try:
            pathlib.Path(save_path).mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + "/pdf" + " already exists...")
        try:
            pathlib.Path(save_path + "/pdf").mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + " already exists...")
        try:
            pathlib.Path(save_path + "/txt").mkdir(parents=True, exist_ok=False)
        except:
            print(save_path + "/txt" + " already exists...")

    chrome_options = Options()
    #chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    #chrome_options.add_experimental_option('useAutomationExtension', False)
    #chrome_options.add_argument("--disable-extensions")
    chrome_options.add_argument("window-size=1920,1080")
    chrome_options.add_argument("--dns-prefetch-disable")

    chrome_options.headless = True
    chrome_options.add_argument("--enable-javascript")
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')

    url = "https://padoka.landtag.sachsen-anhalt.de/portala/browse.tt.html"
    driver = webdriver.Chrome(chrome_driver, options=chrome_options)
    driver.get(url)
    wait = WebDriverWait(driver, 10)
    document_select = driver.find_element_by_xpath("/html/body/main/div[1]/div/div/div/div/div/div[1]/div/div/div/nav/a[2]")
    document_select.click()
    time.sleep(1)
    more_option_button = driver.find_element_by_xpath("/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[2]/div/div/div[2]/button")
    more_option_button.click()
    time.sleep(1)
    document_type = Select(driver.find_element_by_xpath("/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[4]/div/div/div[1]/div/div[1]/div/div/select"))
    document_type.select_by_visible_text("Plenarprotokoll")
    time.sleep(1)
    select_election_period = Select(driver.find_element_by_xpath("/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[5]/div[2]/div/div[1]/div/div[2]/span/select"))
    select_election_period_options = [i.text for i in list(reversed(select_election_period.options))]
    driver.quit()
    for period in select_election_period_options:

        if os.path.isdir(save_path + "/pdf/" + period) and load_checkpoint:
            print("{} already downloaded".format(period))
        else:
            if make_directories:
                try:
                    pathlib.Path(save_path + "/pdf/" + period).mkdir(parents=True, exist_ok=False)
                    pathlib.Path(save_path + "/txt/" + period).mkdir(parents=True, exist_ok=False)
                except:
                    print(save_path + " already exists...")

            driver = webdriver.Chrome(chrome_driver, options=chrome_options)
            driver.get(url)
            wait = WebDriverWait(driver, 10)
            document_select = driver.find_element_by_xpath(
                "/html/body/main/div[1]/div/div/div/div/div/div[1]/div/div/div/nav/a[2]")
            document_select.click()
            time.sleep(1)
            more_option_button = driver.find_element_by_xpath(
                "/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[2]/div/div/div[2]/button")
            more_option_button.click()
            time.sleep(1)
            document_type = Select(driver.find_element_by_xpath(
                "/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[4]/div/div/div[1]/div/div[1]/div/div/select"))
            document_type.select_by_visible_text("Plenarprotokoll")
            time.sleep(1)
            select_election_period = Select(driver.find_element_by_xpath(
                "/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[5]/div[2]/div/div[1]/div/div[2]/span/select"))


            select_election_period.deselect_all()
            select_election_period.select_by_visible_text(period)
            time.sleep(1)
            confirm = driver.find_element_by_xpath("/html/body/main/div[1]/div/div/div/div/div/div[2]/div[2]/form/fieldset/div/div[2]/div/div/div[3]/button")
            confirm.click()
            time.sleep(20)
            #driver.find_element_by_xpath("/html/body/main/div[2]/div[2]/div/div/div[1]/div[2]/div[2]/div/div/div/div[4]/div/div[2]/span/div/button").submit()
            #documents_per_page = Select(driver.find_element_by_xpath("/html/body/main/div[2]/div[2]/div/div/div[1]/div[2]/div[2]/div/div/div/div[4]/div/div[2]/span/select"))
            #documents_per_page.select_by_visible_text("100 Treffer pro Seite")
            Select(WebDriverWait(driver, 20).until(
                EC.element_to_be_clickable((By.XPATH, "/html/body/main/div[2]/div[2]/div/div/div[1]/div[2]/div[2]/div/div/div/div[4]/div/div[2]/span/select")))).select_by_value(
                "0")
            time.sleep(10)
            #result_list = driver.find_element_by_id("results-container")
            #result_elements = result_list.find_elements_by_xpath("//a[@class='btn btn-white btn-sm btn-spacing']")
            all_links = set()
            result_elements = driver.find_elements_by_xpath("//a[@class='btn btn-white btn-sm btn-spacing']")
            for res_elem in result_elements:
                link = res_elem.get_attribute("href")
                if "/plenum/" in link:
                    all_links.add(link)

            driver.quit()
            all_links = list(all_links)
            print("Found {} plenumprotokoll-links".format(len(all_links)))
            fails = 0
            for download_link in tqdm(all_links, desc="Downloading {}".format(period)):
                response = requests.get(download_link)
                file_name = save_path + "/pdf/" + period + "/" + download_link.split("/")[-1]
                try:
                    with open(file_name, 'wb') as f:
                        f.write(response.content)
                except Exception as e:
                    fails += 1
            print("Downloaded {} PDF's, failed with {} PDF's".format(len(all_links) - fails, fails))







def main(args):
    if args.brandenburg:
        brandenburg_crawler_sp()
    if args.hamburg:
        hamburg_crawler()

if __name__ == "__main__":
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('--brandenburg', action='store_true',
                        help='Do you want to crawl documents for brandenburg?')
    parser.add_argument('--hamburg', action='store_true',
                        help='Do you want to crawl documents for hamburg?')
    args = parser.parse_args()
    
    main(args)
    """
    path = "/vol/team/hammerla/parlamentary/hamburg"
    path2= "/media/leon/GameSSD/parlamentary/hamburg"
    drver_path = "/usr/bin/chromedriver"
    sachsen_anhalt_crawler(save_path="/tmp/sachsenanhalt", chrome_driver=drver_path)
    #sachsen_anhalt_crawler(save_path="/media/leon/GameSSD/parlamentary/sachsen_anhalt", chrome_driver=drver_path)
    #sachsen_anhalt_crawler()