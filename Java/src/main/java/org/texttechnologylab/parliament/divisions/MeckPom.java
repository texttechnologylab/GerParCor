package org.texttechnologylab.parliament.divisions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for Parsing Minutes of Mecklenburg-Vorpommern
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class MeckPom {

    @Test
    public void current2() throws MalformedURLException {

        // https://www.dokumentation.landtag-mv.de/parldok/neuedokumente/1

        String sOutPath = "/storage/projects/abrami/GerParCor/pdf/MeckPom/";

        new File(sOutPath).mkdir();

        int iPeriode = 7;
        int a = 0;
        AtomicBoolean isRunning = new AtomicBoolean(true);
        while (isRunning.get()) {
            new File(sOutPath+""+iPeriode).mkdir();
            Map<String, String> cookies = new HashMap<>();
            cookies.put("ASP.NET_SessionId", "bvudp5snyi50asidthnspck3");
            try {

                Document pElement = Jsoup.connect("https://www.dokumentation.landtag-mv.de/parldok/dokumentennummer?LegislaturPeriodenNummer="+iPeriode+"&DokumentenArtId=2&PDFSelect=0&DokumentenNummer="+a).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").cookies(cookies).followRedirects(true).post();

                Elements pElements = pElement.select("table#parldokresult tr");

                if(pElements.size()>0){


                Element download = pElements.get(5);
                Element metaInfos = pElements.get(7);

                Elements metaInfos_content = metaInfos.getElementsByTag("td");

                String sURL = download.select("a").get(0).attr("href");

                File pDownload = new File(sOutPath + ""+ iPeriode + metaInfos_content.get(0).text().replace("/", "_")+"_"+metaInfos_content.get(2).text()+"_".replace("/", "_") + ".pdf");

                if(!pDownload.exists()) {
                    FileUtils.downloadFile(pDownload, "https://www.dokumentation.landtag-mv.de/" + sURL);
                }



                }

//                pElement.select("td.title a").forEach(el -> {
//
//                    String sURL = el.attr("href");
//
//                    try {
//
//                        FileUtils.downloadFile(new File(sOutPath + el.text().replaceAll(" ", "_").replaceAll("/", "_") + ".pdf"), "https://www.dokumentation.landtag-mv.de/" + sURL);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        isRunning.set(false);
//                    }
//
//                });


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


    @Test
    public void current() throws MalformedURLException {

        // https://www.dokumentation.landtag-mv.de/parldok/neuedokumente/1

        String sOutPath = "/tmp/mypath";

        new File(sOutPath).mkdir();

        String sSession = "avzlw4x0u02pp2uh4vx2nq4e";

        int a = 10;
        AtomicBoolean isRunning = new AtomicBoolean(true);
        while (isRunning.get()) {


            Map<String, String> cookies = new HashMap<>();
            cookies.put("ASP.NET_SessionId", sSession);
            try {
                Document pElement = Jsoup.connect("https://www.dokumentation.landtag-mv.de/parldok/neuedokumente/" + a + "?DokumentenArtId=2&PDFSelect=1").userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").cookies(cookies).followRedirects(true).get();

                pElement.select("td.title a").forEach(el -> {

                    String sURL = el.attr("href");

                    try {

                        FileUtils.downloadFile(new File(sOutPath + el.text().replaceAll(" ", "_").replaceAll("/", "_") + ".pdf"), "https://www.dokumentation.landtag-mv.de/" + sURL);
                    } catch (IOException e) {
                        e.printStackTrace();
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

    public static void main(String[] args) {

        String sOutPath = args[0];

        String sURL = "https://www.landtag.nrw.de/portal/WWW/dokumentenarchiv/Dokument/KVP0[WP]-[NR].pdf";

        new File(sOutPath).mkdir();

        for (int a = 1; a < 8; a++) {


            boolean isRunning = true;

            new File(sOutPath + a).mkdir();

            int b = 1;
            while (isRunning) {

                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                File dFile = new File(sOutPath + a + "/" + b + ".pdf");

                if (!dFile.exists()) {
                    try {
                        FileUtils.downloadFile(dFile, sURL.replace("[WP]", "" + a).replace("[NR]", "" + b));
                    } catch (IOException e) {
                        e.printStackTrace();
                        isRunning = false;
                    }
                }

                b++;

            }


        }


    }

}
