package org.texttechnologylab.parliament.divisions;

import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Berlin
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Berlin {

    public static void main(String[] args){

        String sWP = "11,12,13,14,15,16,17,18";
        String sOut = args[0];

        String sURL = "https://pardok.parlament-berlin.de/starweb/adis/citat/VT/{WP}/PlenarPr/p{WP}-{ID}-wp.pdf";

        for (String s : sWP.split(",")) {

            new File(sOut+s).mkdir();

            for(int a=1; a<200; a++){
                String sID = "";
                if(a<10){
                    sID = "00"+a;
                }
                else if(a<100){
                    sID = "0"+a;
                }
                else{
                    sID = ""+a;
                }

                String sDownload = sURL.replace("{WP}", s);
                sDownload = sDownload.replace("{ID}", sID);


                try {
                    File dFile = new File(sOut+s+"/"+s+"_"+sID+".pdf");
                    if(!dFile.exists()){
                        FileUtils.downloadFile(dFile, sDownload);
                    }
                } catch (IOException e) {

                    sDownload = sDownload.replace("-wp", "");
                    File dFile = new File(sOut + s + "/" + s + "_" + sID + ".pdf");
                    try {
                        FileUtils.downloadFile(dFile, sDownload);
                    }
                    catch (Exception e1){

                        String sURIBackup = "https://pardok.parlament-berlin.de/starweb/adis/citat/VT/{WP}/PlenarPr/p{WP}{ID}.pdf";
                        sDownload = sURIBackup.replace("{WP}", s);
                        sDownload = sDownload.replace("{ID}", sID);
                        try {
                            FileUtils.downloadFile(dFile, sDownload);
                        } catch (IOException ex) {

                            sURIBackup = "https://pardok.parlament-berlin.de/starweb/adis/citat/VT/{WP}/PlenarPr/p{WP}-{ID}.pdf";
                            sDownload = sURIBackup.replace("{WP}", s);
                            sDownload = sDownload.replace("{ID}", sID);
                            try {
                                FileUtils.downloadFile(dFile, sDownload);
                            } catch (IOException ex2) {
                                ex2.printStackTrace();
                                break;

                            }

                        }
                    }


                }


            }
        }

    }

}
