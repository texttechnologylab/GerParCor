package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for Parsing Minutes of Thüringen
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Thueringen {

    public static void main(String[] args) {

        // set output path
        String sOut = args[0];
        new File(sOut).mkdir();

        String sURL = "https://www.landtag.nrw.de/portal/WWW/dokumentenarchiv/Dokument/RRP{PERIODE}-{NR}.pdf";

        for (int c = 20; c <= 21; c++) {

            String sPeriode = "";

            if (c < 10) {
                sPeriode = "0" + c;
            } else {
                sPeriode = "" + c;
            }

            new File(sOut + sPeriode).mkdir();


            for (int a = 1; a < 200; a++) {
                String sID = "" + a;

                String sDownload = sURL.replace("{PERIODE}", sPeriode);
                sDownload = sDownload.replace("{NR}", "" + sID);

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
    public void from5Th(){

        String sOut = "/storage/projects/abrami/GerParCor/pdf/Thueringen/";
        new File(sOut).mkdir();

        String sSession = "z3wneg1twsedxlcmqojm2x2s";

        for(int iPeriode=7; iPeriode<=7; iPeriode++){
            int a = 1;
            AtomicBoolean isRunning = new AtomicBoolean(true);
            while (isRunning.get()) {

                String sOutPath = sOut + iPeriode + "/";
                new File(sOutPath).mkdir();

                Map<String, String> cookies = new HashMap<>();
                cookies.put("acceptgrt", "1");

                cookies.put("ASP.NET_SessionId", sSession);
                try {

                    try {
                        Thread.sleep(1000l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Document pElement = Jsoup.connect("https://parldok.thueringer-landtag.de/ParlDok/dokumentennummer?LegislaturPeriodenNummer=" + iPeriode + "&DokumentenArtId=3&PDFSelect=1&DokumentenNummer=" + a).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").cookies(cookies).followRedirects(true).post();

                    int finalA = a;
                    if(pElement.select("div.col-12 a").size()==0){
                        isRunning.set(false);
                    }
                    pElement.select("div.col-12 a").forEach(el -> {

                        String sURL = el.attr("href");

                        try {

                            if (!el.text().contains("Vorgang")) {
                                File dFile = new File(sOutPath + finalA+"_"+pElement.select(".row .resultinfo .row").get(0).text().replace("Dokumentdatum: ", "") + ".pdf");
                                if(!dFile.exists()) {
                                    FileUtils.downloadFile(dFile, "https://parldok.thueringer-landtag.de" + sURL);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            isRunning.set(false);
                        }

                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(10000l);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                a++;
            }
        }


    }


}
