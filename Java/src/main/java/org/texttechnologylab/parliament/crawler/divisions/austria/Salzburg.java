package org.texttechnologylab.parliament.crawler.divisions.austria;

import com.goebl.david.Request;
import com.goebl.david.Webb;
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

public class Salzburg {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();

        String sDownladBasePath = "https://service.salzburg.gv.at";

        for(int iPeriode = 11; iPeriode<20; iPeriode++) {

            new File(sOutpath+""+iPeriode).mkdir();

            Document pDocument = Jsoup.connect("https://service.salzburg.gv.at/lpi/searchExtern?datumVon=&datumBis=&artId=4&fraktionId=&periode="+iPeriode+"&session=&beilage=&titel=&text=&search=").sslSocketFactory(socketFactory()).get();

            HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

            Elements pElements = pDocument.select("table#mySearchTable tbody tr");

            int finalIPeriode = iPeriode;
            pElements.stream().forEach(el1 -> {

                String sTitle = el1.select("div.col-md").text();
                sTitle = sTitle.replace("Meldung vom ", "");
                System.out.println(sTitle);

                String finalSTitle1 = sTitle;
                el1.select("a").stream().forEach(a->{
                    if(a.attr("href").endsWith(".pdf")){
                        File dFile = new File(sOutpath + finalIPeriode + "/" + finalSTitle1 + ".pdf");

                        if(!dFile.exists()){
                            try {
                                FileUtils.downloadFile(dFile, "https://"+a.attr("href").replace("http://", ""));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }

                    }
                });

            });

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
