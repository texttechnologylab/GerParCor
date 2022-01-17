package org.texttechnologylab.parliament.divisions;

import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Hessen
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Hessen {

    public static void main(String[] args) {

        String sOutPath = args[0];

        String sURL = "http://starweb.hessen.de/cache/PLPR/{WP}/{NR}/{NRLONG}.pdf";

        new File(sOutPath).mkdir();


            for (int a = 1; a <= 20; a++) {

                boolean isRunning = true;

                new File(sOutPath + a).mkdir();

                int b = 1;
                while (isRunning) {

                    String sNumber = "";
                    String sWP = ""+a;

                    if(a<10){
                        sWP = "0"+a;
                    }

                    if(b<10){
                        sNumber="0000"+b;
                    }
                    else if(b<100){
                        sNumber="000"+b;
                    }
                    else if(b<1000){
                        sNumber="00"+b;
                    }

                    String sDetailNumber = sNumber.substring(4);

                    try {
                        Thread.sleep(1000l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    File dFile = new File(sOutPath + a + "/" + b + ".pdf");

                    if (!dFile.exists()) {
                        try {
                            FileUtils.downloadFile(dFile, sURL.replace("{WP}", "" + sWP).replace("{NR}", sDetailNumber).replace("{NRLONG}", sNumber));
                        } catch (IOException e) {
                            e.printStackTrace();
                            isRunning = false;
                        }
                    }

                    b++;

                }


        }


    }

}
