package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Berlin
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Brandenburg {

    public static void main(String[] args){

        String sOut = args[0];
        new File(sOut).mkdir();
        String sURI = "https://www.parlamentsdokumentation.brandenburg.de/starweb/LBB/ELVIS/parladoku/w7/plpr/{ID}.pdf";

        new File(sOut+7).mkdir();

        for(int a=1; a<100; a++){

            File dFile = new File(sOut+"7/"+a+".pdf");

            try {
                FileUtils.downloadFile(dFile, sURI.replace("{ID}", a+""));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


    }


}
