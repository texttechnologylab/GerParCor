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
            params.put("AFHTOKE", "BNLIzgFs/M+5BUjbESyxaflUlbyqPBUoV27NxMgTou3XFOrW6b9aV94ZoptRN5FJ/j961Y7p1zDsMN134hGaqiF8iIY9JMVSLIBiONex9JxORFmS41YEfDMAzQCvF5Dz4TsBCqbK7e8+ppCZ0TWLgf4BbTnmAetKuvIxmiKWI8XpjqslRR2fZQ0GP9ERsVYOKYLwPNsAS/itfJM4MGJwyJlvDrh0NSBMNz+2ToykT0DT4CljL+goyk+fcIRT3dQPCHxMKYvVBqI4/3hTIyuL2uhkqGNmKJrwa25uUkn2bSpSo4dMEofEdg==");

            Map<String, String> cookies = new HashMap<>(0);
            cookies.put("ASP.NET_SessionId", "p4egvdbmgwbppy3cabw1ylzk");
            cookies.put("cookieokay", "true");
            cookies.put("HASH_ASP.NET_SessionId", "DCB1B59FFDDEB108D515BF6FE02C17A2A1CBB5AA");

            try {
                Document pDocument = Jsoup.connect("https://www.buergerschaft-hh.de/parldok/dokumentennummer").followRedirects(true).cookies(cookies).data(params).post();

                Document dataDocument = Jsoup.connect("https://www.buergerschaft-hh.de/parldok/dokumentennummer/1").cookies(cookies).data(params).followRedirects(true).get();


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
