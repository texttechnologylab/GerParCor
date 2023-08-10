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

public class Tirol {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        String sDownladBasePath = "https://service.salzburg.gv.at";

        Document pDocument = Jsoup.connect("https://portal.tirol.gv.at/LteWeb/public/sitzung/landtag/landtagsSitzungList.xhtml?cid=2").sslSocketFactory(socketFactory()).get();

        pDocument.select("#c_listContent_j_id_4g_menu select option").forEach(option->{
            if(option.text().length()>6){
                System.out.println(option.text());
            }

        });



//            Elements pElements = pDocument.select("table#mySearchTable tbody tr");

//            pElements.stream().forEach(el1 -> {
//
//                String sTitle = el1.select("div.col-md").text();
//                sTitle = sTitle.replace("Meldung vom ", "");
//                System.out.println(sTitle);
//
//                String finalSTitle1 = sTitle;
//                el1.select("a").stream().forEach(a->{
//                    if(a.attr("href").endsWith(".pdf")){
//                        File dFile = new File(sOutpath + finalIPeriode + "/" + finalSTitle1 + ".pdf");
//
//                        if(!dFile.exists()){
//                            try {
//                                FileUtils.downloadFile(dFile, "https://"+a.attr("href").replace("http://", ""));
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//
//                        }
//
//                    }
//                });
//
//            });

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
