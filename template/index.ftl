<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8'>
    <meta name='author' content='Giuseppe Abrami'>
    <meta name='author' content='TTLab'>

    <title>GerParCor</title>
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
            padding: 15px;
        }

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

    </style>

</head>

<body>

<div class="grid-container">
    <img src="gerparcor.png" style="height:14vh;">
    <a href="https://www.texttechnologylab.org" target="_blank"><img src="LogoTTLab.png" style="height:14vh;"></a>
    <a href="#cite"><i class="fa-solid fa-quote-right" style="font-size:24pt;"> Cite</i></a>
    <a href="#license"><i class="fa-solid fa-scale-balanced" style="font-size:24pt;"> License</i></a>
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
        <option value="all" selected>all</option>
        <#list factory.listCountries() as c>
            <option value="${c}">${c}</option>
        </#list>
    </select>


    <select name="devision">
        <option value="all" selected>all</option>

        <#list factory.listDevisions() as c>
            <option value="${c}">${c}</option>
        </#list>
    </select>

    <select name="parliament">
        <option value="all" selected>all</option>

        <#list factory.listParliaments() as c>
            <option value="${c}">${c}</option>
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
    <#list factory.listProtocols(parliament, devision, country) as p>
        <tr>
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
                <span class="token">${p.getToken()}</span>
                <span class="sentence">${p.getSentence()}</span>
                <span class="lemma">${p.getLemma()}</span>
                <span class="dependency">${p.getDependency()}</span>


            </td>
            <td>
                ${p.getSentiment()}
            </td>
            <td>
                <a href="/download/${p.getID()}" target="_blank"><i class="fa-solid fa-download"></i></a>
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
