package org.texttechnologylab.parliament.crawler.divisions.austria;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vorarlberg {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        Map<String, String> cookies = new HashMap<>();

        cookies.put("lbpersistence", "!zTewQ61NB2TIpAtuTYmKCJtHdcwozNhtM6IpqvOdULAGENGGs36XZuaOjQeQs9/7cLzlPRS7MRDCONZrjbDniI4XkEAM+ELoBXU40Vzr");
        cookies.put("rowNavigate", "-1");
        cookies.put("rowStart", "1");
        cookies.put("searchMode", "yes");
        cookies.put("vlrgov", "L1=0##L2=0##L3=0##L5=0##L6=8##L8=0##L9=0##L10=0##L7=##");
        cookies.put("searchURL", "https://suche.vorarlberg.at/VLR/vlr_gov.nsf/alldocs?SearchView&SearchMax=0&Count=10000&Start=1&SearchWV=FALSE&SearchFuzzy=FALSE&SearchOrder=1&Query=FIELD  fd_TypeOfDocumentTX Contains \"Protokoll\" AND ");
        StringBuilder sb = new StringBuilder();

//        Set<String> blackWords = new HashSet<>(0);
//        blackWords.add("top");
//        blackWords.add("vorlage");
//        blackWords.add("genehmigung");

        for(int a=0; a<9; a++) {
            Document pDocument = Jsoup.connect("https://suche.vorarlberg.at/VLR/vlr_gov.nsf/alldocs_byDateDSC?SearchView=&SearchMax=0&Count=100&Start="+a+"&SearchWV=FALSE&SearchFuzzy=FALSE&SearchOrder=4&Query=FIELD fdClassificationHumanTX =Protokoll der kompletten Sitzung").userAgent("Mozilla/5.0").timeout(5000).get();

            Elements pElements = pDocument.select("table.rfont tr");
            for (Element pElement : pElements) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                if (pElement.select("a").size() > 0) {
                    Elements pTD = pElement.select("td");
                    if (pTD.size() > 0) {

                        String sYear = pTD.get(0).text();
                        String sShortDate = pTD.get(3).text();
                        String sName = pTD.get(1).text();

                        sb.append(sName);
                        sb.append("\t");
                        sb.append(sYear);
                        sb.append("\t");
                        sb.append("https://suche.vorarlberg.at" + pTD.get(1).select("a").get(0).attr("href"));

//                    if(!sName.toLowerCase().contains("top") && !sName.toLowerCase().contains("vorlage")){


                        Document tDocument = Jsoup.connect("https://suche.vorarlberg.at" + pTD.get(1).select("a").get(0).attr("href")).timeout(1000).get();

                        if(tDocument.select("iframe").size()>0){
                            String sURI = tDocument.select("iframe").get(0).attr("src");
                            sURI = sURI.substring(0, sURI.indexOf("#"));
                            System.out.println(sURI);
                            sb.append("\t");
                            sb.append(sURI);
//                            tDocument = Jsoup.connect(sURI).ignoreHttpErrors(true).ignoreContentType(true).timeout(1000).get();
//                            try {
//
//                                String sFinalName = sName.replace("/", "__");
////                                if (sFinalName.length() > 20) {
////                                    sFinalName = sShortDate;
////                                }
//                                new File(sOutpath + sYear).mkdir();
//                                File dFile = new File(sOutpath + sYear + "/" + sFinalName + ".pdf");
//                                if (!dFile.exists()) {
//                                    Thread.sleep(1000l);
//                                    System.out.println(dFile.getAbsolutePath());
////                                    FileUtils.downloadFile(dFile, sURI);
//
//                                    org.apache.commons.io.FileUtils.copyURLToFile(
//                                            new URL(sURI),
//                                            dFile,
//                                            1000,
//                                            10000);
//                                }
//
//                            } catch (IOException e) {
//                                System.out.println(e.getMessage());
//                                System.out.println(sURI);
//                            } catch (InterruptedException e) {
//                                System.out.println(e.getMessage());
//                                System.out.println(sURI);
//                            }
                        }
                        else {

                            tDocument.select("a").stream().forEach(link -> {
                                if (link.text().contains("Vollanzeige")) {
//                            System.out.println(link);
                                    String sLink = link.attr("href");
                                    sLink = sLink.replace("javascript:OpenPDF(\"", "");
                                    sLink = sLink.replace("\")", "");
                                    System.out.println(sLink);
                                    try {
                                        Thread.sleep(1000l);

                                        String sFinalName = sName.replace("/", "__");
                                        if (sFinalName.length() > 20) {
                                            sFinalName = sShortDate;
                                        }
                                        new File(sOutpath + sYear).mkdir();
                                        File dFile = new File(sOutpath + sYear + "/" + sFinalName + ".pdf");
                                        if (!dFile.exists()) {
                                            FileUtils.downloadFile(dFile, sLink);
                                        }

                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                            });
                        }
                    }
                }
            }
            }
//        }

        FileUtils.writeContent(sb.toString(), new File("/tmp/vorarlberg.tsv"));



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
