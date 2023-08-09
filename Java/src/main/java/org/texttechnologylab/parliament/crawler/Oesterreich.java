package org.texttechnologylab.parliament.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

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

}
