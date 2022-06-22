![GerParCor](https://user-images.githubusercontent.com/32450159/149387119-6d300f31-f743-436b-b1e1-baf2181ff961.png)
# GerParCor
German Parliamentary Corpus (GerParCor)

[![Paper](http://img.shields.io/badge/paper-ACL--anthology-B31B1B.svg)](http://www.lrec-conf.org/proceedings/lrec2022/pdf/2022.lrec-1.202.pdf)
[![Conference](http://img.shields.io/badge/conference-LREC--2022-4b44ce.svg)](https://lrec2022.lrec-conf.org/)

# Abstract
Parliamentary debates represent a large and partly unexploited treasure trove of publicly accessible texts. In the German-speaking area, there is a certain deficit of uniformly accessible and annotated corpora covering all German-speaking parliaments at the national and federal level. To address this gap, we introduce the German Parliament Corpus (GerParCor). GerParCor is a genre-specific corpus of (predominantly historical) German-language parliamentary protocols from three centuries and four countries, including state and federal level data. In addition, GerParCor contains conversions of scanned protocols and, in particular, of protocols in Fraktur converted via an OCR process based on Tesseract. All protocols were preprocessed by means of the NLP pipeline of spaCy3 and automatically annotated with metadata regarding their session date. GerParCor is made available in the XMI format of the UIMA project. In this way, GerParCor can be used as a large corpus of historical texts in the field of political communication for various tasks in NLP.

GerParCor is available via http://gerparcor.texttechnologylab.org
 
| # | Parliament | Sessions | From | Until | Status / Download |
--- | --- | --- | --- | --- | --- |
| 1 | Reichstag (NG + Zoll) | 1990 | 02/25/1867 | 05/24/1895 | [Download](http://gerparcor.texttechnologylab.org/data/Reichstag_NG_Zoll.tar) |
| 2 | Reichstag (Empire) | 2183 | 12/03/1895 | 10/26/1918 | [Download](http://gerparcor.texttechnologylab.org/data/Reichstag_Empire.tar) |
| 3 | Weimar Republic | 1328 | 02/06/1919 | 12/09/1932 | [Download](http://gerparcor.texttechnologylab.org/data/Weimar_Republic.tar) |
| 4 | ThirdReich | 20 | 03/21/1933 | 04/24/1942 | [Download](http://gerparcor.texttechnologylab.org/data/ThirdReich.tar) |
| 5 | Bundesrat | 1008 | 09/07/1949 | 10/08/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Bundesrat.tar) |
| 6 | Bundestag | 4158 | 09/07/1949 | 09/07/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Bundestag.tar) |
| 7 | Baden-Würtemberg | 412 | 06/05/1984 | 09/29/2021 | [Download](http://gerparcor.texttechnologylab.org/data/BadenWuertemberg.tar) |
| 8 | Bayern | 2221 | 12/16/1946 | 10/14/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Bayern.tar) |
| 9 | Berlin | 582 | 04/02/1989 | 09/16/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Berlin.tar) |
| 10 | Brandenburg | 442 | 10/26/1990 | 08/27/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Brandenburg.tar) |
| 11 | Bremen | 1102 | 07/04/1995 | 09/16/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Bremen.tar) |
| 12 | Hamburg | 586 | 10/08/1997 | 11/03/2021 |  [Download](http://gerparcor.texttechnologylab.org/data/Hamburg.tar) |
| 13 | Hessen | 1297 | 02/04/1947 | 09/29/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Hessen.tar) |
| 14 | Mecklenburg-Vorpommern | 659 | 10/26/1990 | 06/11/2021 | [Download](http://gerparcor.texttechnologylab.org/data/MeckPom.tar) |
| 15 | Niedersachsen | 1109 | 06/22/1982 | 09/15/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Niedersachsen.tar) |
| 16 | Nordrhein-Westfalen | 2041 | 05/21/1947 | 10/08/2021 | [Download](http://gerparcor.texttechnologylab.org/data/NRW.tar) |
| 17 | Rheinland-Pfalz | 1562 | 07/24/1947 | 09/22.2021 | [Download](http://gerparcor.texttechnologylab.org/data/RLP.tar) |
| 18 | Saarland | 876 | 07/23/1959 | 09/15/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Saarland.tar) |
| 19 | Sachsen | 690 | 10/27/1990 | 11/18/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Sachsen.tar) |
| 20 | Sachsen-Anhalt | 607 | 10/28/1990 | 09/17/2021 | [Download](http://gerparcor.texttechnologylab.org/data/SachsenAnhalt.tar) |
| 21 | Schleswig-Holstein | 1776 | 02/26/1946 | 02/11/2021 | [Download](http://gerparcor.texttechnologylab.org/data/SchleswigHolstein.tar) |
| 22 | Thüringen | 761 | 10/25/1990 | 11/19/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Thueringen.tar) |
| 23 | Liechtenstein | 504 | 03/13/1997 | 11/06/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Liechtenstein.tar) |
| 24 | Nationalrat (AT) | 4267 | 10/21/1918 | 05/17/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Nationalrat.tar)  |
| 25 | Nationlarat (CH) | 368 | 12/06/1999 | 12/09/2021 | [Download](http://gerparcor.texttechnologylab.org/data/Schweiz.tar) |

# Cite
If you want to use the project or the corpus, please quote this as follows:

G. Abrami, M. Bagci, L. Hammerla, and A. Mehler, “German Parliamentary Corpus (GerParCor),” in Proceedings of the Language Resources and Evaluation Conference, Marseille, France, 2022, pp. 1900-1906. 

# BibTeX
```
@InProceedings{Abrami:Bagci:Hammerla:Mehler:2022,
  author         = {Abrami, Giuseppe and Bagci, Mevl\"{u}t and Hammerla, Leon and Mehler, Alexander},
  title          = {German Parliamentary Corpus (GerParCor)},
  booktitle      = {Proceedings of the Language Resources and Evaluation Conference},
  month          = {June},
  year           = {2022},
  address        = {Marseille, France},
  publisher      = {European Language Resources Association},
  pages          = {1900--1906},
  url            = {https://aclanthology.org/2022.lrec-1.202}
}

```
