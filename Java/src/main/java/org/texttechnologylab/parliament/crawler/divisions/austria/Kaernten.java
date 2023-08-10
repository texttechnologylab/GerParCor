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

public class Kaernten {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();

        String sDownladBasePath = "https://www.ktn.gv.at/";

        Document pDocument = Jsoup.connect("https://www.ktn.gv.at/Politik/Landtag/Stenographische-Protokolle").sslSocketFactory(socketFactory()).get();

        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        Elements pElements = pDocument.select(".main article");

        pElements.stream().forEach(el1->{

            Element h2 = el1.getElementsByTag("h2").get(0);

            String sTitle = h2.text();
            sTitle = sTitle.substring(0, sTitle.indexOf("."));
            System.out.println(sTitle);

            new File(sOutpath+""+sTitle).mkdir();

            Elements links = el1.select("a");



            String finalSTitle = sTitle;
            links.forEach(l->{

                File dFile = new File(sOutpath+""+ finalSTitle +"/"+l.text()+".pdf");
                if(!dFile.exists()) {
                    try {
//                        Document pPdf = Jsoup.connect(sDownladBasePath+l.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0").get();
//                        System.out.println(pPdf);
                        FileUtils.downloadFile(dFile, sDownladBasePath+l.attr("href"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        });



    }

    static private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
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
