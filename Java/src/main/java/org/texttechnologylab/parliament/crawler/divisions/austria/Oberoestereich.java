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

public class Oberoestereich {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();

        String sDownladBasePath = "https://www.land-oberoesterreich.gv.at";

        Document pDocument = Jsoup.connect("https://www.land-oberoesterreich.gv.at/ltgspsuche.htm").sslSocketFactory(socketFactory()).get();

        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        Elements pElements = pDocument.select("ul.liste-extra li a");

        pElements.stream().forEach(el1->{


            String sTitle = el1.text();
            sTitle = sTitle.replace("Sitzungen der ", "");
            System.out.println(sTitle);
            new File(sOutpath+sTitle).mkdir();

            try {
                System.out.println(el1.attr("href"));
                Document subPage = Jsoup.connect(
                        el1.attr("href").startsWith(sDownladBasePath) ? el1.attr("href") : sDownladBasePath+el1.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).get();

                Elements pSubElements = subPage.select("li.link-extern");
                if(pSubElements.size()==0){
                    pSubElements = subPage.select("ul.liste-extra li");
                }

                String finalSTitle = sTitle;
                pSubElements.stream().forEach(el2->{
                    System.out.println(el2.select("a").get(0).text());

                    File dFile = new File(sOutpath+ finalSTitle +"/"+el2.select("a").get(0).text()+".pdf");

                    if(!dFile.exists()) {
                        try {
                            String sDownload = el2.select("a").get(0).attr("href");

                            FileUtils.downloadFile(dFile, sDownload.startsWith(sDownladBasePath) ? sDownload : sDownladBasePath+sDownload);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });



                } catch (IOException e) {
                throw new RuntimeException(e);
            }



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