import cassis
import os
from typing import List, Set, Dict, Tuple, Optional
from tqdm import tqdm
from multiprocessing import Pool
from functools import partial
from typing import NamedTuple


class Cas(NamedTuple):
    text: str
    title: str
    id: str
    author: str
    subtitle: str
    day : int
    month: int
    year: int
    timestamp: int


def cassis_cas_to_namedTuple_cas(cas:cassis.Cas) -> Cas:
    """
    Function converts a cassis.Cas-Object to a namedTuple-->Cas-Object.
    After that Conversion the Object can be pickled.
    :param cas:
    :return:
    """
    document_meta_data = cas.select("de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData")[0]
    document_annotation = cas.select("org.texttechnologylab.annotation.DocumentAnnotation")[0]
    namedTuple_cas = Cas(text=cas.sofa_string,
                         title=document_meta_data.get("documentTitle"),
                         id=document_meta_data.get("documentId"),
                         author=document_annotation.get("author"),
                         subtitle=document_annotation.get("subtitle"),
                         day=document_annotation.get("dateDay"),
                         month=document_annotation.get("dateMonth"),
                         year=document_annotation.get("dateYear"),
                         timestamp=document_annotation.get("timestamp"))
    return namedTuple_cas


def load_cas_from_xmi(filepath:str,
                      typesystem:cassis.TypeSystem) -> cassis.Cas:
    """
    Function loads a uima-xmi file in and returns it as a (cassis)cas-object
    :param filepath:
    :param typesystem:
    :return:
    """
    with open(filepath, 'rb') as f:
        cas = cassis.load_cas_from_xmi(f, typesystem=typesystem)
    return cas


def load_all_cas_from_dir(dirpath:str,
                          typesystem:cassis.TypeSystem,
                          verbose:bool=True) -> [cassis.Cas]:
    """
    Function collects all xmi files in a directory and returns a list of cas-objects which were
    constructed by going through the xmi_files.
    :param dirpath:
    :param typesystem:
    :return:
    """
    xmi_files = [os.path.join(dirpath, file) for file in os.listdir(dirpath)]
    if verbose:
        cas_list = []
        for i in tqdm(range(0, len(xmi_files)), desc=f"Loading XMI-Files from: {dirpath}"):
            cas_list.append(load_cas_from_xmi(xmi_files[i], typesystem=typesystem))
    else:
        cas_list = [load_cas_from_xmi(xmi_file, typesystem=typesystem) for xmi_file in xmi_files]
    return cas_list


def load_all_cas_from_dir_mp(dirpath:str,
                             typesystem:str,
                             verbose:bool=True) -> [Cas]:
    """
    Function collects all xmi files in a directory and returns a list of cas-objects which were
    constructed by going through the xmi_files.
    :param dirpath:
    :param typesystem:
    :return:
    """
    typesystem = load_typesystem(typesystem)
    xmi_files = [os.path.join(dirpath, file) for file in os.listdir(dirpath)]
    if verbose:
        cas_list = []
        for i in tqdm(range(0, len(xmi_files)), desc=f"Loading XMI-Files from: {dirpath}"):
            cas_list.append(cassis_cas_to_namedTuple_cas(load_cas_from_xmi(xmi_files[i], typesystem=typesystem)))
    else:
        cas_list = [cassis_cas_to_namedTuple_cas(load_cas_from_xmi(xmi_file, typesystem=typesystem)) for xmi_file in xmi_files]
    return cas_list


def load_all_cas_from_dir_of_dirs(dirpath:str,
                                  typesystem:cassis.TypeSystem) -> Dict[str, List[cassis.Cas]]:
    """
    Function does same as load_all_cas_from_dir, just one dir-order higher. Returns a dict with the name of a directory
    as a key and a list of cas-objects of its contained xmi-files.
    :param dirpath:
    :param typesystem:
    :return:
    """
    cas_dict = dict()
    dirs = [os.path.join(dirpath, dir) for dir in os.listdir(dirpath)]
    for dir in dirs:
        cas_dict[dir] = load_all_cas_from_dir(dir, typesystem=typesystem)
    return cas_dict


def load_all_cas_from_dir_of_dirs_mp(dirpath:str,
                                     typesystem:str) -> [[Cas]]:
    """
    Function does same as load_all_cas_from_dir, just one dir-order higher. Returns a list of lists of cas-objects. (
    using Multiprocessing)
    :param dirpath:
    :param typesystem:
    :return:
    """
    dirs = [os.path.join(dirpath, dir) for dir in os.listdir(dirpath)]
    pool = Pool(len(dirs))
    part_func = partial(load_all_cas_from_dir_mp, typesystem=typesystem)
    result = pool.map(part_func, dirs)
    pool.close()
    pool.join()
    return result


def load_typesystem(path:str='/home/s5935481/work4/parliament_crawler/src/convert_and_clean/TypeSystem.xml') -> cassis.TypeSystem:
    """
    Function returns a typesystem given by the path to its xml file. This typesystem is later used to
    construct cas-objects.
    :param path:
    :return:
    """
    with open(path, 'rb') as f:
        typesystem = cassis.load_typesystem(f)
    return typesystem


if __name__ == "__main__":
    typesystem = load_typesystem()
    typesystem_path = '/home/s5935481/work4/parliament_crawler/src/convert_and_clean/TypeSystem.xml'
    dir_path = "/vol/s5935481/parlamentary_xmi_corpus/hamburg"
    dicto = load_all_cas_from_dir_of_dirs_mp(dirpath=dir_path, typesystem=typesystem_path)
    for i in dicto:
        print("==================================================================================================0")
        print(i[0].title)
