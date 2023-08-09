package org.texttechnologylab.parliament.crawler.divisions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of NordrheinWestfalen
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class NordrheinWestfalen {

    public static void main(String[] args){

//        String sURL = "https://www.landtag.nrw.de/portal/WWW/dokumentenarchiv/Dokument?pl=MM&pnr=WP/NR&part=P&quelle=parla&ref=dok_verw";
        String sURL = "https://www.landtag.nrw.de/portal/WWW/dokumentenarchiv/Dokument/MMP{WP}-{NR}.pdf";
        String sOut = args[0];
        new File(sOut).mkdir();

        boolean isRunning = true;

        for(int a=17; a<=18; a++){
            isRunning = true;
            new File(sOut+a).mkdir();

            int b=1;

            while(isRunning){


                File dFile = new File(sOut+a+"/"+b+".pdf");

                if(!dFile.exists()){
                    try {
                        FileUtils.downloadFile(dFile, sURL.replace("{WP}", ""+a).replace("{NR}", ""+b));
                        try {
                            Thread.sleep(1500l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        isRunning=false;
                    }
                }

                b++;

            }

        }

    }

    @Test
    public void from4thPeriod(){

        String sURL = "https://edas.landtag.sachsen.de/viewer.aspx?dok_nr=[NR]&dok_art=PlPr&leg_per=[PER]&pos_dok=201&dok_id=";

        String sOut = "/tmp/mypath";

        new File(sOut).mkdir();
        boolean isRunning = true;

        for(int a=4; a<8; a++){
            isRunning = true;
            new File(sOut+a).mkdir();

            int b=1;

            while(isRunning){


                File dFile = new File(sOut+a+"/"+b+".pdf");

                if(!dFile.exists()){

                    try {

                        Document doc = Jsoup.connect(sURL.replace("[NR]", ""+b).replace("[PER]", ""+a)).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").execute().parse();

                        doc.select("frame[name=anzeige]").forEach(i->{

                            String sSRC = i.attr("src");

                            try {
                                Document doc2 = Jsoup.connect("https://edas.landtag.sachsen.de/"+sSRC).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").followRedirects(true).execute().parse();

                                doc2.getElementsByTag("iframe").forEach(t->{

                                    System.out.println(t.attr("src"));

                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        });

                        System.out.println(doc);

                        try {
                            Thread.sleep(25000l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String sDownload = "https://ws.landtag.sachsen.de/images/[PER]_PlPr_[NR]_201_1_1_.pdf";

                        try {
                            FileUtils.downloadFile(dFile, sDownload.replace("[PER]", ""+a).replace("[NR]", ""+b));
                        } catch (IOException e) {
                            e.printStackTrace();
                            isRunning=false;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }

                b++;

            }

        }

    }

}
