package org.texttechnologylab.parliament.crawler;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;
import org.texttechnologylab.utilities.helper.RESTUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for Parsing Minutes of Nationalrat (CH)
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Schweiz {

    public static void main(String[] args){


        String sURI = "https://ws.parlament.ch/odata.svc/Transcript(Language='DE',ID=[ID]L)?$select=Language,LanguageOfText,Text,MeetingDate,ID,SpeakerFirstName,SpeakerFullName,SpeakerLastName,CouncilName&$format=application/json;odata.metadata=full,application/json;odata=verbose";

        String sDownload = args[0];

        int iFolder = 1;

        int iCount = 1;
        int iDownload = 0;

        while(true) {
            try {

                if(iCount%100==0 && iCount>0){
                    Thread.sleep(2000l);
                }

                if(iDownload%1000==0 && iDownload>0){
                    iFolder++;
                }

                new File(sDownload+iFolder).mkdir();

                JSONObject jObject = RESTUtils.getObjectFromRest(sURI.replace("[ID]", "" + iCount), "");

                System.out.println(jObject);

                jObject = jObject.getJSONObject("d");

                String sDate = jObject.getString("MeetingDate");
                String sID = jObject.getString("ID");

                System.out.println(sID + "\t" + sDate);

                FileUtils.writeContent(jObject.toString(), new File(sDownload +iFolder + "/"+iCount+"__"+ sDate + "-" + sID + ".json"));
                iDownload++;


            } catch (Exception e) {
                System.out.println(iCount+" \t "+e.getMessage());
            }
            iCount++;
        }



    }

    @Test
    public void merge() throws IOException {

        // downloadPath
        String sDownload = "/opt/path";

        // new Path
        String sOutPath = "/tmp/outpath/";
        new File(sOutPath).mkdir();

        Set<File> fSet = FileUtils.getFiles(sDownload, ".json");

        AtomicReference<File> currentFile = new AtomicReference<File>();

        final StringBuilder[] sb = {new StringBuilder()};

        fSet.stream().sorted((a, b)-> {
            String sA = a.getName();
            String sB = b.getName();

            sA = sA.substring(sA.indexOf("__")+2, sA.indexOf("-"));
            sB = sB.substring(sB.indexOf("__")+2, sB.indexOf("-"));

            return sA.compareTo(sB);

        }).forEach(f->{

            String sOut = f.getName().substring(f.getName().lastIndexOf("__")+2, f.getName().indexOf("-"))+".txt";

            if(currentFile.get()==null){
                currentFile.set(new File(sOutPath + sOut));
            }

            if(!currentFile.get().getName().equalsIgnoreCase(sOut) && sb[0].length()>0){
                try {
                    FileUtils.writeContent(sb[0].toString(), currentFile.get());
                    sb[0] = new StringBuilder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentFile.set(new File(sOutPath + sOut));
            }

            try {
                String sContent = FileUtils.getContentFromFile(f);

                JSONObject sObject = new JSONObject(sContent);

                if(sObject.has("CouncilName")){

                    boolean bContinue = false;

                    try {
                        bContinue = sObject.getString("CouncilName").equalsIgnoreCase("Nationalrat") || sObject.isNull("CouncilName");
                    }
                    catch (Exception e){
                        bContinue = true;
                    }

                    if(bContinue){

                        if(sObject.has("LanguageOfText")){

                            if(sObject.getString("LanguageOfText").equalsIgnoreCase("DE")){
                                String sText = sObject.getString("Text");

                                sText = Jsoup.parse(sText).text();
                                sText = sText.replaceAll("\\[\\w+\\]", "");
                                sb[0].append(sText+"\n");
                            }
                        }

                    }

                }



            } catch (Exception e) {
                e.printStackTrace();
            }


        });

        if(sb[0].length()>0){
            FileUtils.writeContent(sb[0].toString(), currentFile.get());
        }

    }

}
