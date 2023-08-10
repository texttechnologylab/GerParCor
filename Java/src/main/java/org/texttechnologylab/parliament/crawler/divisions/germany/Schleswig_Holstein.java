package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Schleswig-Holstein
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Schleswig_Holstein {

    public static void main(String[] args) {

        String sOut = args[0];
        new File(sOut).mkdir();

//        String sURL = "http://lissh.lvn.parlanet.de/shlt/lissh-dok/infothek/wahl{PERIODE}/plenum/plenprot/XQQP{SID}-{TID}.pdf";

        String sURL = "http://www.landtag.ltsh.de/export/sites/ltsh/infothek/wahl19/plenum/plenprot/2017/19-001_06-17.pdf";

        for(int c=19; c<=22; c++) {

            String sPeriode = "";

            if(c<10){
                sPeriode = "0"+c;
            }
            else{
                sPeriode = ""+c;
            }

            new File(sOut + sPeriode).mkdir();


                for (int a = 1; a < 200; a++) {
                    String sID = "" + a;

                    String sDownload = sURL.replace("{PERIODE}", sPeriode);
                    sDownload = sDownload.replace("{SID}", "" + sPeriode);
                    sDownload = sDownload.replace("{TID}", "" + sID);

                    File dFile = new File(sOut + sPeriode + "/" + sPeriode + "_" + sID + ".pdf");
                    if (!dFile.exists()) {
                        try {
                            FileUtils.downloadFile(dFile, sDownload);
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            try {
                                Thread.sleep(1000l);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                }




        }

    }


    @Test
    public void from15Th() throws IOException {

        String sOut = "/tmp/mypath";
        new File(sOut).mkdir();


        for(int i=-2; i<15; i++) {
            new File(sOut+i).mkdir();

            Document pElement = Jsoup.connect("http://lissh.lvn.ltsh.de/cgi-bin/starfinder/0").data("path", "lisshdokfl.txt").data("id", "fastlinkdok").data("format", "WEBKURZFL3").data("search", "P").data("search", ""+i).data("search", "*").data("OK", "Suche").data("pass", "").timeout(1000000).post();
            //.data("path=lisshdokfl.txt&id=fastlinkdok&format=WEBKURZFL3&OK=Suche&pass=&search=P&search=15&search=*").userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").post();

            Elements el = pElement.select("table.tabcol tr td");
            int finalI = i;
            el.forEach(element -> {
                if (element.text().contains("Plenarprotokoll")) {
                    Elements a = element.select("a");
                    System.out.println(a);

                    a.forEach(aElement -> {

                        String sText = element.text();
                        sText = sText.replace("/", "_");
                        sText = sText.replace("Plenarprotokoll ", "");

                        File dFile = new File(sOut+ finalI +"/"+sText+".pdf");
                        String sURL = aElement.attr("href");
                        if(!dFile.exists()){
                            try {
                                FileUtils.downloadFile(dFile, sURL);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }



                    });
                }
            });

            try {
                Thread.sleep(5000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }


}
