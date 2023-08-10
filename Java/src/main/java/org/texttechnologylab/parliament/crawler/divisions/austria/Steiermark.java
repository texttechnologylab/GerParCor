package org.texttechnologylab.parliament.crawler.divisions.austria;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class Steiermark {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();

        String sDownladBasePath = "https://www.landesarchiv.steiermark.at";

        Document pDocument = Jsoup.connect("https://www.landesarchiv.steiermark.at/cms/ziel/111284715/DE/").sslSocketFactory(socketFactory()).get();

        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        Elements pElements = pDocument.select(".txtblock-wrapper h2 a");

        pElements.stream().forEach(el1->{


            String sTitle = el1.text();
            System.out.println(sTitle);

            try {
                Document subPage = Jsoup.connect(el1.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).get();

                Elements pSubElements = subPage.select(".txtblock-wrapper h2 a");

                pSubElements.stream().forEach(el2->{
                    System.out.println(el2);

                    File dPath = new File(sOutpath+""+el2.text());
                    dPath.mkdir();

                    try {
                        Document subSubPage = Jsoup.connect(el2.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).get();

                        Elements subsubElements = subSubPage.select(".txtblock-wrapper");
                        subsubElements.stream().forEach(el3->{

                            if(el3.select("h2").get(0).text().contains("protokolle")){
                                el3.select(".txtblock-content li a").forEach(el4->{

                                    File dFile = new File(dPath.getAbsolutePath()+"/"+el4.text()+".pdf");
                                    if(!dFile.exists()) {
                                        try {
                                            FileUtils.downloadFile(dFile, sDownladBasePath + el4.attr("href"));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }


                                });
                            }

                        });

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                });

                System.out.println(subPage);

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
