package org.texttechnologylab.parliament.crawler.divisions.austria;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class Niederoestereich {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();

        String sDownladBasePath = "https://noe-landtag.gv.at";

        Document pDocument = Jsoup.connect("https://noe-landtag.gv.at/sitzungen").sslSocketFactory(socketFactory()).get();

        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        Elements pElements = pDocument.select(".list-unstyled li a");

        pElements.stream().forEach(el1->{


            String sTitle = el1.text();
            sTitle = sTitle.replace("Sitzungen der ", "");
            System.out.println(sTitle);
            new File(sOutpath+sTitle).mkdir();

            try {
                Document subPage = Jsoup.connect(sDownladBasePath+el1.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).get();

                Elements pSubElements = subPage.select("div.col-12 div");

                String finalSTitle = sTitle;
                pSubElements.stream().forEach(el2->{
                    System.out.println(el2.text());

                    try {
                        Document subSubPage = Jsoup.connect(sDownladBasePath+el2.select("a").get(0).attr("href")).get();

                        Elements subSubElements = subSubPage.select("#attachements li");
                        subSubElements.stream().forEach(el3->{

                            if(el3.text().contains("Sitzungsbericht")){
                                el3.select("a").stream().forEach(el4->{
                                    if(el4.attr("href").contains(".pdf")){
                                        File dFile = new File(sOutpath+ finalSTitle +"/"+el2.text()+".pdf");

                                        if(!dFile.exists()) {
                                            try {
                                                FileUtils.downloadFile(dFile, sDownladBasePath + el4.attr("href"));
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                });
                            }

                        });

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }





                        });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//            String finalSTitle = sTitle;
//            links.forEach(l->{
//
//                File dFile = new File(sOutpath+""+ finalSTitle +"/"+l.text()+".pdf");
//                if(!dFile.exists()) {
//                    try {
////                        Document pPdf = Jsoup.connect(sDownladBasePath+l.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0").get();
////                        System.out.println(pPdf);
//                        FileUtils.downloadFile(dFile, sDownladBasePath+l.attr("href"));
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });

        });



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
