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
import java.util.concurrent.atomic.AtomicBoolean;

public class Wien {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        String sBasePath = "https://www.wien.gv.at";

        Document pDocument = Jsoup.connect("https://www.wien.gv.at/mdb/ltg/").sslSocketFactory(socketFactory()).get();

        Elements years = pDocument.select("div.vie-artikel ol.ul_unmarked li a");

        years.stream().forEach(yearLink->{
            System.out.println(yearLink);
            String sYear = yearLink.text();
            new File(sOutpath+""+sYear).mkdir();

            try {
                Document pageYear = Jsoup.connect(sBasePath+yearLink.attr("href")).sslSocketFactory(socketFactory()).get();

                Elements pElements = pageYear.select("#vie_col2_content > ul > li");

                pElements.stream().forEach(el->{
                    String sDatum = el.select("strong").get(0).text();

                    Elements pUElements = el.select("ul.ul_unmarked ul.vie-lst-horizontal");

                    AtomicBoolean bWortprotokoll = new AtomicBoolean(false);

                    pUElements.stream().forEach(uEl->{
                        if(uEl.text().contains("Wortprotokoll")){
                            bWortprotokoll.set(true);
                            AtomicBoolean bDownload = new AtomicBoolean(false);
                            el.select("a").stream().forEach(al->{
                                if(al.text().contains("PDF")){
                                    bDownload.set(true);
                                    File dFile = new File(sOutpath+""+sYear+"/"+sDatum+".pdf");
                                    if(!dFile.exists()){
                                        try {
                                            FileUtils.downloadFile(dFile, sBasePath+yearLink.attr("href").replace("index.htm", "")+al.attr("href"));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            });
                            if(!bDownload.get()){
                                el.select("a").stream().forEach(al->{
                                    if(al.text().contains("DOC")){
                                        bDownload.set(true);
                                        File dFile = new File(sOutpath+""+sYear+"/"+sDatum+".doc");
                                        if(!dFile.exists()){
                                            try {
                                                FileUtils.downloadFile(dFile, sBasePath+yearLink.attr("href").replace("index.htm", "")+al.attr("href"));
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });

                    if(!bWortprotokoll.get()){
                        pUElements.stream().forEach(uEl->{
                            if(uEl.text().contains("Sitzungsbericht")){

                                AtomicBoolean bDownload = new AtomicBoolean(false);
                                el.select("a").stream().forEach(al->{
                                    if(al.text().contains("DOC")){
                                        bDownload.set(true);
                                        File dFile = new File(sOutpath+""+sYear+"/"+sDatum+".doc");
                                        if(!dFile.exists()){
                                            try {
                                                FileUtils.downloadFile(dFile, sBasePath+yearLink.attr("href").replace("index.htm", "")+al.attr("href"));
                                            } catch (IOException e) {
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                    }
                                });


                            }
                        });
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
