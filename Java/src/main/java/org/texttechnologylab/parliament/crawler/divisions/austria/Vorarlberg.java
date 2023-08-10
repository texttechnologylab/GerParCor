package org.texttechnologylab.parliament.crawler.divisions.austria;

import it.unimi.dsi.fastutil.Hash;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Vorarlberg {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        String sDownladBasePath = "https://suche.vorarlberg.at/";

        int iCount = 5;
        String searchURL = "https://suche.vorarlberg.at/VLR/vlr_gov.nsf/alldocs?SearchView&SearchMax=0&Count="+iCount+"&Start=1&SearchWV=FALSE&SearchFuzzy=FALSE&SearchOrder=1&Query=FIELD%20%20fd_TypeOfDocumentTX%20Contains%20%22Protokoll%22%20AND";
        Document pDocument = Jsoup.connect(searchURL).sslSocketFactory(socketFactory()).get();

        pDocument.select("table#tab tr").forEach(el1->{
            Elements tdElements = el1.select("td");
            if(tdElements.size()>0){
                String sYear = tdElements.get(5).text();
                new File(sOutpath+""+sYear+"/").mkdir();

                try {
                    Map<String, String> cookies = new HashMap<>();
                    cookies.put("lbpersistence", "!zTewQ61NB2TIpAtuTYmKCJtHdcwozNhtM6IpqvOdULAGENGGs36XZuaOjQeQs9/7cLzlPRS7MRDCONZrjbDniI4XkEAM+ELoBXU40Vzr");
                    cookies.put("rowNavigate", "-1");
                    cookies.put("rowStart", "1");
                    cookies.put("searchMode", "yes");
                    cookies.put("searchURL", searchURL);
                    cookies.put("vlrgov", "L1=0##L2=0##L3=0##L5=0##L6=8##L8=0##L9=0##L10=0##L7=##");
                    Document subDocument = Jsoup.connect(sDownladBasePath+tdElements.get(2).select("a").get(0).attr("href")).cookies(cookies).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").followRedirects(true).get();

                    subDocument.select("body a").stream().forEach(el2->{
                        System.out.println(el2.text());
                        if(el2.text().contains("PDF Voll")){
                            String sLink = el2.attr("href");
                            sLink = sLink.substring(sLink.indexOf("http"), sLink.lastIndexOf(".pdf"));
                            System.out.println(sLink);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

//        pDocument.select("#c_listContent_j_id_4g_menu select option").forEach(option->{
//            if(option.text().length()>6){
//                System.out.println(option.text());
//            }
//
//        });



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
