import argparse
import pickle
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




def get_proxies(proxy_path: str = "/media/leon/GameSSD/proxy.txt") -> [str]:
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
    print(proxy_ip_port)
    proxy = Proxy()
    proxy.proxy_type = ProxyType.MANUAL
    proxy.http_proxy = proxy_ip_port
    proxy.ssl_proxy = proxy_ip_port
    #proxy.socks_proxy = proxy_ip_port

    capabilities = webdriver.DesiredCapabilities.CHROME
    proxy.add_to_capabilities(capabilities)

    driver = webdriver.Chrome(driver_path, options=chrome_options, desired_capabilities=capabilities, keep_alive=True)
    return driver


def unlock_pages_for_proxy(proxy, driver_path: str = "/usr/local/bin/chromedriver"):
    chrome_options = Options()
    #chrome_options.add_argument('--headless')
    chrome_options.add_argument("--enable-javascript")
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    driver = get_proxy_driver(proxy, chrome_options, driver_path)
    #driver= webdriver.Chrome(driver_path, options=chrome_options)
    url = "https://www.buergerschaft-hh.de/parldok/dokumentennummer"
    url = "https://httpbin.org/ip"
    driver.get(url)
    time.sleep(2)

    #driver.execute_script('''window.open("https://www.buergerschaft-hh.de/parldok/dokumentennummer","_blank");''')
    driver.close()


proxies = get_proxies()

for i in proxies:
    unlock_pages_for_proxy(i)
