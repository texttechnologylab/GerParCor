package org.texttechnologylab.parliament.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;
import org.texttechnologylab.utilities.helper.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for Parsing Minutes of Nationalrat (AT)
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Oesterreich {

    public static void main(String[] args) throws IOException {

        // after wget on Parliament-Site
        String sDownload = args[0];

        new File(sDownload).mkdir();

        Document d = Jsoup.parse(FileUtils.getContentFromFile(new File("/path/to/downloaded_File")));


        Elements e = d.select("table.tabelle tbody tr");

        e.forEach(el->{
            String sDatum = el.child(1).select("span").text();
            sDatum = sDatum.replace("Datum ", "");
            String sSitzung = el.child(3).select("span a").text();

            String finalSDatum = sDatum;
            el.child(5).select("a").forEach(pdf->{
                if(pdf.text().equalsIgnoreCase("pdf") || pdf.text().contains("PDF")){
                    System.out.println(pdf.attr("href"));

                    File dFile = new File(sDownload+ finalSDatum +"_"+sSitzung+".pdf");

                    if(!dFile.exists()) {
                        try {
                            FileUtils.downloadFile(dFile, pdf.attr("href"));
                            try {
                                Thread.sleep(1000l);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            });
        });

    }

    @Test
    public void Nationalrat() throws IOException {

        File inputFile = new File("/home/gabrami/Downloads/Nationalrat.csv");

        String sOut = "/tmp/austria/Nationalrat/";
        new File(sOut).mkdir();

        String sFile = StringUtils.getContent(inputFile);

        String[] sPrime = sFile.split("\n");

        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfOutput = new SimpleDateFormat("dd.MM.yyyy");

        String sBaseDownloadPath = "https://www.parlament.gv.at";

        for (String s : sPrime) {
            String[] sSplit = s.split(";");

            try {
                int i = Integer.valueOf(sSplit[4]);

                System.out.println(sSplit[2]);
                System.out.println(sSplit[4]);
                System.out.println(sSplit[9]);
                System.out.println(sSplit[10]);

                new File(sOut + "" + sSplit[2]).mkdir();

                String sDate = sSplit[9].substring(0, sSplit[9].indexOf("T"));
                Date pDate = sdfInput.parse(sDate);
                File dFile = new File(sOut + "" + sSplit[2] + "/" +i+"_"+sdfOutput.format(pDate)+".pdf");
                if(!dFile.exists()){
                    Document pDocument = Jsoup.parse(sSplit[10]);
                    for (Element a : pDocument.select("a")) {
                        if(a.attr("href").endsWith(".pdf")){
                            FileUtils.downloadFile(dFile, sBaseDownloadPath+a.attr("href"));
                        }
                    }
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }

        }


    }

}
