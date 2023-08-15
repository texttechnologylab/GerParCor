package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for Parsing Minutes of Sachsen
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class SachsenAnhalt {

    public static void main(String[] args){

        String sURL = "https://padoka.landtag.sachsen-anhalt.de/files/plenum/wp{WP}/{NR}stzg.pdf";

        String sOut = args[0];
        new File(sOut).mkdir();

        boolean isRunning = true;

        for(int a=7; a<=8; a++){
            isRunning = true;
            new File(sOut+a).mkdir();

            int b=1;

            while(isRunning){


                File dFile = new File(sOut+a+"/"+b+".pdf");

                if(!dFile.exists()){
                    try {
                            String sNR = "00";
                            if(b<10){
                                sNR = sNR+b;
                            }
                            else if(b<100){
                                sNR = "0"+b;
                            }
                            else{
                                sNR = b+"";
                            }
                            FileUtils.downloadFile(dFile, sURL.replaceAll("\\{WP\\}", ""+a).replaceAll("\\{NR\\}", sNR));
                        }
                        catch (Exception ex){
                            System.out.println(ex.getMessage());
                            isRunning=false;
                        }

                }

                b++;

            }

        }

    }

    @Test
    public void from4thPeriod(){

        String sOut = "/opt/mypath";

        new File(sOut).mkdir();
        boolean isRunning = true;

        String etID = "a10eea26c1ec20a3681e452a0f8384e3";
        String sSession = "uieqi0e3px2hh3qwut5m0drm";

        Map<String, String> cookies = new HashMap<>();
        cookies.put("_et_coid", etID);
        cookies.put("ASP.NET_SessionId", sSession);
        cookies.put("cookieBar", "hide");
        for(int a=5; a<8; a++){
            isRunning = true;
            new File(sOut+a).mkdir();

            int b=1;

            while(isRunning){


                File dFile = new File(sOut+a+"/"+b+".pdf");

                if(!dFile.exists()){

                    String sNew = "https://edas.landtag.sachsen.de/viewer/viewer_navigation.aspx?dok_nr="+b+"&dok_art=PlPr&leg_per="+a+"&pos_dok=201&dok_id=undefined";
                    try {
                        Document test = Jsoup.connect(sNew).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").execute().parse();

                        String sDownload = "https://ws.landtag.sachsen.de/images/[PER]_PlPr_[NR]_201_1_1_.pdf";

                        try {
                            FileUtils.downloadFile(dFile, sDownload.replace("[PER]", ""+a).replace("[NR]", ""+b));
                        } catch (IOException e) {
                            e.printStackTrace();
                            isRunning=false;

                        }

                        try {
                            Thread.sleep(2000l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
