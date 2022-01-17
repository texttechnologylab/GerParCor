package org.texttechnologylab.parliament;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Class for Parsing Minutes of Bundesrat
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Bundesrat {
    static String sBase = "https://www.bundesrat.de/";

    public static void main(String[] args) throws IOException {

        String sStart = "https://www.bundesrat.de/DE/service/archiv/pl-protokoll-archiv/pl-protokoll-archiv-node.html";
        String sOut = args[0];

        Document pDocument = Jsoup.connect(String.valueOf(new URL(sStart))).get();

        Elements tdTop = pDocument.select("div .body-text tbody td");

        tdTop.stream().forEach(element -> {
            if(element.text().length()>0){

                String sLegislatur = element.text();

                new File(sOut+sLegislatur).mkdir();

                String sLink = element.getElementsByTag("a").get(0).attr("href");

                try {
                    parse(sBase+sLink, sOut+sLegislatur+"/");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });

    }

    public static void parse(String sURI, String outPath) throws IOException {

        // DE/service/archiv/pl-protokoll-archiv/_functions/plpr2021-25/plpr2021-25-node.html;jsessionid=01A55AE8C5268731DC22623596C0445C.2_cid365


        Document pDocument = Jsoup.connect(String.valueOf(new URL(sURI))).get();

        Elements links = pDocument.select("tbody tr td a");

        links.forEach(l->{
           String sLink = l.attr("href");
            String sTitle = l.attr("title");

            String sFileName = sTitle.substring(sTitle.indexOf("Plenar"), sTitle.indexOf("(")-1);

            try {
                FileUtils.downloadFile(new File(outPath+sFileName+".pdf"), sBase+sLink);
            } catch (IOException e) {
                e.printStackTrace();
            }


        });



    }

}
