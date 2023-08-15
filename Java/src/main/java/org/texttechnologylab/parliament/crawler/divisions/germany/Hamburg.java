package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for Parsing Minutes of Hamburg
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Hamburg {

    public static void main(String[] args) throws IOException {

        String sBaseURL = "https://www.buergerschaft-hh.de";

        String outPath = args[0];
        new File(outPath).mkdir();
        AtomicBoolean hasError = new AtomicBoolean(false);
        int a=37;
        int iPeriode = 22;
        while(!hasError.get()) {

            new File(outPath+""+iPeriode).mkdir();

            Map<String, String> params = new HashMap<>();
            params.put("DokumentenArtId", "2");
            params.put("LegislaturPeriodenNummer", ""+iPeriode);
            params.put("DokumentenNummer", ""+a);
            params.put("AFHTOKE", "W2fLQQ4EhRSHTX4Ioe+zAH2V28HvsSrNOXRrKpZUSk/K7ozL19h/0z95qmklTMSqr30Yr74sGBfrNDjHxK0aD89y6jz46jSWjtOAw4Up09iUS3rO25DIcBLbBgyIv9t8oGO1cM6233OLBIQ7Twg1R0+wm/X2Vwp41ETg7Vc8tK9XUwex1iu3j5V77PKd/L8VcUtaMFu7xGPyGb376HftthcD2nJY6hQubvTbIdmC5j2ebE8cQ35PQe9XkAPYvT0vaeaN38gBBdMeFI980NGbtb1twrWEORaomOzI+zHEQMJMwqiVgz7b/j5UxGsZVDiTApD1nk9k1To=");

            Map<String, String> cookies = new HashMap<>(0);
            cookies.put("ASP.NET_SessionId", "0u05ocgjqa4h2kt1drpzlanl");
            cookies.put("cookieokay", "true");
            cookies.put("HASH_ASP.NET_SessionId", "6B54B4C4762559FB936CC1645AAE92CBEB63812E");

            try {
                Document pDocument = Jsoup.connect("https://www.buergerschaft-hh.de/parldok/dokumentennummer").followRedirects(true).cookies(cookies).data(params).post();

                Document dataDocument = Jsoup.connect("https://www.buergerschaft-hh.de/parldok/dokumentennummer/1").cookies(cookies).data(params).followRedirects(true).get();

                try {
                    Thread.sleep(1000l);
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }

                AtomicReference<String> sURI = new AtomicReference<>("");
                AtomicReference<String> sName = new AtomicReference<>("");
                AtomicReference<String> sDatum = new AtomicReference<>("");
                dataDocument.select("table#parldokresult tr").stream().forEach(d -> {
                    System.out.println(d.text());

                    d.select("td").stream().forEach(d1 -> {

                        d1.select("a").stream().forEach(href->{
                            if(href.attr("href").endsWith(".pdf")){
                                sURI.set(href.attr("href"));
                            }
                        });

                        if (d1.attr("headers").equals("result-nummer")) {
                            sName.set(d1.text());
                        }
                        if (d1.attr("headers").equals("result-datum")) {
                            sDatum.set(d1.text());
                        }

                        if(sDatum.get().length()>0 && sName.get().length()>0 && sURI.get().length()>0){
                            String tName = sName.get();
                            tName = tName.substring(tName.indexOf("/")+1);
                            File dFile = new File(outPath+""+iPeriode+"/"+tName+"_"+sDatum.get()+".pdf");

                                try {
                                    if(!dFile.exists()) {
                                        FileUtils.downloadFile(dFile, sBaseURL + "" + sURI.get());
                                        hasError.set(false);
                                    }
                                } catch (IOException e) {
                                    hasError.set(true);
                                    throw new RuntimeException(e);

                                }
                                finally {
                                    sDatum.set("");
                                    sName.set("");
                                    sURI.set("");
                                }
                            }

                    });


                });
                a++;
//            System.out.println(dataDocument);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                hasError.set(true);
            }
        }


    }

}
