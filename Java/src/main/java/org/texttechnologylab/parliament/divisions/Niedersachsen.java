package org.texttechnologylab.parliament.divisions;

import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Niedersachsen
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Niedersachsen {

    public static void main(String[] args){

        String sURL = "https://www.landtag-niedersachsen.de/parlamentsdokumente/steno/ID_wp/endberINDEX.pdf";

        String sOut = args[0];

        boolean isRunning = true;

        for(int a=18; a<20; a++){
            isRunning = true;
            new File(sOut+a).mkdir();

            int b=1;

            while(isRunning){

                String sID = "";
                if(b<10){
                    sID = "00"+b;
                }
                else if(b<100){
                    sID = "0"+b;
                }
                else{
                    sID = ""+b;
                }

                File dFile = new File(sOut+a+"/"+sID+".pdf");

                if(!dFile.exists()){
                    try {
                        FileUtils.downloadFile(dFile, sURL.replace("ID", ""+a).replace("INDEX", sID));
                    } catch (IOException e) {
                        e.printStackTrace();
                        isRunning=false;
                    }
                }

                b++;

            }

        }

    }

}
