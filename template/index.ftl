<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8'>
    <meta name='author' content='Giuseppe Abrami'>
    <meta name='author' content='TTLab'>

    <title>GerParCor Reloaded</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.1/jquery.min.js"></script>
    <script src="https://d3js.org/d3.v7.min.js"></script>
    <script src='https://cdnjs.cloudflare.com/ajax/libs/spin.js/2.0.1/spin.min.js'></script>
    <script src="https://kit.fontawesome.com/a01f8fee95.js" crossorigin="anonymous"></script>
    <#--    Some CSS-Styles...-->
    <style>
        @import url(http://fonts.googleapis.com/css?family=Source+Code+Pro:400,600);
        body {
            font-family: "Source Code Pro", Consolas, monaco, monospace;
            font-size: 18px;
            line-height: 1.5;
            font-weight: 400;
            background-color: rgb(0, 97, 143);
            color: white;
        }

        .topBox{
            display:block;
            border:2pt solid rgb(72, 169, 218);
            margin:5pt;
            background-color:rgb(72, 169, 218);
            /*height:100%;*/
        }

        .topBox span{
            width:100%;
            text-align: center;
            font-size:20pt;
        }

        .textBlock{
            padding-top: 1vh;
        }

        .textContent{
            background: #ddd;
            display: block;
            word-break: break-all;
            word-wrap: break-word;
            font-size:12pt;
            color:black;
            border: #333 solid 5pt;
        }

        a{
            color:#fff;
        }

        .topBox h2{
            width:100%;
            background-color: rgb(228, 227, 221);
            text-align:center;
            color:black;
            font-weight: bolder;
            font-variant: all-petite-caps;
            font-size:30pt;
            margin: 0px;
        }

        .topBox .content{
            padding:1vw;
        }

        .grid-container {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr 1fr;
            grid-template-rows: 100% 100% 100% 100%;
            grid-column-gap: 5px;
            grid-row-gap: 5px;
            align-items: start;
            background-color: rgb(0, 97, 143);
            margin-left: 2em;
            margin-right: 2em;
        }
        .grid-container-menu {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            grid-template-rows: 100% 100% 100%;
            grid-column-gap: 5px;
            grid-row-gap: 5px;
            align-items: start;
            background-color: rgb(0, 97, 143);
            margin-left: 2em;
            margin-right: 2em;
        }

        .vis {
            margin:2em;
        }

        th, td {
            padding: 10px;
        }

        tr.content:hover {background-color: #48a9da;}


        span{
            color:black;
            width:50px;
            min-width:50px!important;
        }
        span.token{
            background: #ccc;
        }
        span.sentence{
            background: #cbd;
        }
        span.lemma{
            background: #abc;
        }
        span.dependency{
            background: #cde;
        }

        .small{
            font-size: 14pt;
        }

    </style>

</head>

<body>

<div class="grid-container">
    <img src="gerparcor.png" style="height:14vh;">
    <a href="https://www.texttechnologylab.org" target="_blank"><img src="LogoTTLab.png" style="height:14vh;"></a>
    <a href="#cite"><i class="fa-solid fa-quote-right" style="font-size:24pt; padding-top: 3vw;"> Cite</i></a>
    <a href="#license"><i class="fa-solid fa-scale-balanced" style="font-size:24pt; padding-top: 3vw;"> License</i></a>
</div>

<h2>Welcome @ GerParCor. If you are looking for GerParCor of 2022, please click <a href="http://lrec2022.gerparcor.texttechnologylab.org/" target="_blank">here</a>.</h2>
<div class="textContent small" style="padding: 1vw; margin-bottom: 3vh;">
    In 2022, the largest German-speaking corpus of parliamentary protocols from three different centuries, on a national and federal level
    from the countries of Germany, Austria, Switzerland and Liechtenstein, was collected and published - GerParCor. Through GerParCor, it
    became possible to provide for the first time various parliamentary protocols which were not available digitally and, moreover, could
    not be retrieved and processed in a uniform manner. Furthermore, GerParCor was additionally preprocessed using NLP methods and
    made available in XMI format. In this paper, GerParCor is significantly updated by including all new parliamentary protocols in the corpus,
    as well as adding and preprocessing further parliamentary protocols previously not covered, so that a period up to 1797 is now covered.
    Besides the integration of a new, state-of-the-art and appropriate NLP preprocessing for the handling of large text corpora, this
    update also provides an overview of the further reuse of GerParCor by presenting various provisioning capabilities such as APIs,
    among others.
</div>


<#--Definition of the individual containers for the respective contents.-->
<#--Is self-explanatory as far as it shows.-->

<#--<#list factory.listProtocols("Germany", "National") as p>-->
<#--    ${p}<br>-->
<#--</#list>-->
<form method="post">
<div class="grid-container-menu">

    <label for="country">Country</label>
    <label for="division">National / Regional</label>
    <label for="parliament">Parliament</label>


    <select name="country" onchange="this.form.submit()">
        <option value="all" <#if country=="all">selected</#if> >all</option>
        <#list factory.listCountries() as c>
            <option value="${c}" <#if country==c>selected</#if> >${c}</option>
        </#list>
    </select>


    <select name="devision"  onchange="this.form.submit()">
        <option value="all" <#if devision=="all">selected</#if> >all</option>

        <#list factory.listDevisions() as c>
            <option value="${c}" <#if devision==c>selected</#if> >${c}</option>
        </#list>
    </select>

    <select name="parliament" onchange="this.form.submit()">
        <option value="all" <#if parliament=="all">selected</#if> >all</option>

        <#list factory.listParliaments(country) as c>
            <option value="${c}" <#if parliament==c>selected</#if> >${c}</option>
        </#list>
    </select>
    

    

</div>
<div class="grid-container-menu"> 
    <span></span>
    <label for="page">Page</label>
    <span></span>

    <#assign documents=factory.countProtocols(parliament, devision, country)>
    <b>Total documents: ${documents?c}</b>
    
    <#assign x=(documents/limit)?floor>
	
    <select name="page" onchange="this.form.submit()">
	<#list 1..x as i>
	  <option value="${i?c}" <#if page==i>selected</#if> >${i?c} / ${x?c}</option>
	</#list>
    
    </select>
</div>
</form>

<div id="data">

    <table padding="10px" align="center">
        <tr style="font-weight:bold; background:#ccc;">
            <td>Country</td>
            <td>Date</td>
            <td>Name</td>
            <td>Parliament</td>
            <td>Annotations</td>
            <td>Sentiment</td>
            <td>Options</td>
        </tr>
    <#list factory.listProtocols(parliament, devision, country, page, limit) as p>
        <tr class="content">
            <td>
                ${p.getCountry()}
            </td>

            <td>
                ${p.getDate()}
            </td>

            <td>
                ${p.getSubtitle()}
            </td>

            <td>
                ${p.getParliament()}
            </td>
            <td>
                <span class="token" title="Tokens">${p.getToken()}</span>
                <span class="sentence" title="Sentences">${p.getSentence()}</span>
                <span class="lemma"  title="Lemmas">${p.getLemma()}</span>
                <span class="dependency" title="Dependencies">${p.getDependency()}</span>
            </td>
            <td>
                ${p.getSentiment()}
            </td>
            <td>
                <a href="/download/${p.getID()}" target="_blank" title="Download as XMI"><i class="fa-solid fa-download"></i></a>
            </td>
        </tr>

    </#list>
   
 
    </table>

</div>

<div id="cite" class="textBlock">
<h2><i class="fa-solid fa-quote-right" style="font-size:24pt;"> Cite</i></h2>

    <div class="textContent"><pre>
    @inproceedings{Abrami:et:al:2024,
        address   = {Torino, Italy},
        author    = {Abrami, Giuseppe and Bagci, Mevl{\"u}t and Mehler, Alexander},
        booktitle = {Proceedings of the 2024 Joint International Conference on Computational Linguistics, Language Resources and Evaluation (LREC-COLING 2024)},
        editor    = {Calzolari, Nicoletta and Kan, Min-Yen and Hoste, Veronique and Lenci, Alessandro and Sakti, Sakriani and Xue, Nianwen},
        month     = {may},
        pages     = {7707--7716},
        publisher = {ELRA and ICCL},
        title     = {{G}erman Parliamentary Corpus ({G}er{P}ar{C}or) Reloaded},
        url       = {https://aclanthology.org/2024.lrec-main.681},
        year      = {2024}
    }
    </pre>
    </div>

</div>

<div id="license" class="textBlock">
<h2><i class="fa-solid fa-scale-balanced" style="font-size:24pt;"> License</i></h2>
    <div class="textContent"><pre>
    GerParCor
    Copyright (C) 2024 Text Technology Lab

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see https://www.gnu.org/licenses/.
    </pre>
    </div>
</div>

</body>

</html>
