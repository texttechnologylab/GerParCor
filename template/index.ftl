<html>
<head>
    <meta charset='utf-8'>
    <meta name='author' content='Giuseppe Abrami'>

    <title>Musterlösung Übung 4 &copy; by Giuseppe Abrami</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.1/jquery.min.js"></script>
    <script src="https://d3js.org/d3.v7.min.js"></script>
    <script src='https://cdnjs.cloudflare.com/ajax/libs/spin.js/2.0.1/spin.min.js'></script>
    <script src='charts.js'></script>
    <script src='functions.js'></script>
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

    </style>

</head>

<body>

<#--Definition of the individual containers for the respective contents.-->
<#--Is self-explanatory as far as it shows.-->
<#--<div class="grid-container">-->

<#--<#list factory.listProtocols("Germany", "National") as p>-->
<#--    ${p}<br>-->
<#--</#list>-->

    <#list factory.listCountries() as c>
        <h1>${c}</h1><br>
        <#list factory.listDevisions() as d>
            <h2>${d}</h2><br>
            <#list factory.listProtocols(c, d) as p>
                ${p} ${p.getParliament()}<br>
            </#list>
        </#list>
    </#list>


<#--</div>-->

</body>
</html>
