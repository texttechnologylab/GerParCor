package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Class for Parsing Minutes of Schleswig-Holstein
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Schleswig_Holstein {

    public static void main(String[] args) throws IOException {

        String sOut = args[0];
        new File(sOut).mkdir();

        String sURL = "https://www.landtag.ltsh.de/infothek/wahl{WP}/plenum/plenprot_seite/";


        for(int c=19; c<=20; c++) {

            String sUrlNew = sURL.replace("{WP}", c+"");


                new File(sOut + c).mkdir();

                Document pDocument = Jsoup.connect(sUrlNew).get();

                Elements pElements = pDocument.select(".presseticker ul li.presserow");
            int finalC = c;
            pElements.stream().forEach(el->{
//                    System.out.println(el);
                    if(el.select("a").size()>0) {
                        String sID = el.select("div").get(0).text();
                        String sDatum = el.select("div").get(1).text();
                        String sDownload = el.select("div").get(2).select("a").get(0).attr("href");

                        File dFile = new File(sOut + finalC + "/"+ sDatum + ".pdf");
                        if (!dFile.exists()) {
                            try {
                                FileUtils.downloadFile(dFile, "https://www.landtag.ltsh.de" + sDownload);
                                Thread.sleep(1000);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                });


//                for (int a = 1; a < 200; a++) {
//                    params.put("dokumentennummer", c+"/"+a);
//
//                    Document pDocument = Jsoup.connect(sURL).data(params).sslSocketFactory(socketFactory()).timeout(10000).followRedirects(true).ignoreContentType(true).post();
//                    System.out.println(pDocument);
//
//
////                    File dFile = new File(sOut + sPeriode + "/" + sPeriode + "_" + sID + ".pdf");
////                    if (!dFile.exists()) {
////                        try {
////                            FileUtils.downloadFile(dFile, sDownload);
////                        } catch (IOException e) {
////                            System.out.println(e.getMessage());
////                            try {
////                                Thread.sleep(1000l);
////                            } catch (InterruptedException ex) {
////                                ex.printStackTrace();
////                            }
////                        }
////                    }
//
//                }




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


    static private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory result = sslContext.getSocketFactory();

            return result;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }

}
