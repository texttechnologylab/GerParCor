"""
https://github.com/jhnwr/rotatingproxies/blob/master/scrapeproxies.py#L13
"""
from typing import List

import requests
from bs4 import BeautifulSoup
import random
import concurrent.futures

#get the list of free proxies
def getProxies() -> List[str]:
    r = requests.get('https://free-proxy-list.net/')
    soup = BeautifulSoup(r.content, 'html.parser')
    table = soup.find('tbody')
    proxies = []
    for row in table:
        if row.find_all('td')[4].text =='elite proxy':
            proxy = ':'.join([row.find_all('td')[0].text, row.find_all('td')[1].text])
            proxies.append(proxy)
        else:
            pass
    return proxies

def get_proxies2(path="/vol/s5935481/parlamentary/BIN/proxy_list2.txt") -> List[str]:
    proxies = []
    with open(path, "r") as f:
        c = 0
        for i in f:
            if c == 500: break
            if c % 2 == 0:
                proxies.append(str(i).split(" ")[0].strip().split("\t")[0])
            c += 1
    return proxies

def extract(proxy: str) -> [bool, str]:
    #this was for when we took a list into the function, without conc futures.
    #proxy = random.choice(proxylist)
    working = False
    try:
        #change the url to https://httpbin.org/ip that doesnt block anything
        r = requests.get("https://www.buergerschaft-hh.de/parldok/dokumentennummer", proxies={'http' : proxy,'https': proxy}, timeout=2)
        working = True
        print("Working: {}".format(proxy))
    except:
        working = False
    return [working, proxy]

proxylist = getProxies()
proxylist.extend(get_proxies2())
print("Number of proxys found: {}".format(str(len(proxylist))))

#check them all with futures super quick
with concurrent.futures.ThreadPoolExecutor() as executor:
    proxies = executor.map(extract, proxylist)
    count = 0
    with open("/vol/s5935481/parlamentary/BIN/proxy.txt", "w") as f:
        for proxy in proxies:
            if proxy[0] == True:
                count += 1
                f.write(proxy[1] + "\n")
    print("Found {} working proxys.".format(count))
