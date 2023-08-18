package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Berlin
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Bayern {

    public static void main(String[] args){

        String sOut = args[0];

        String sURL = "https://www.bayern.landtag.de/webangebot2/webangebot/protokolle;jsessionid=F2E67182B0985A0507DB5F4AE7BF1DDE?execution=e1s1";

        String s = "18";

            new File(sOut+s).mkdir();

        try {
            Document pDocument = Jsoup.connect(sURL).get();

            pDocument.select("table tbody tr").stream().forEach(tr->{

//                System.out.println(tr);
                Elements el = tr.select("td");
                if(el.size()>1) {
                    Element e1 = el.get(0);
                    Element e2 = el.get(1);
                    Element e3 = el.get(2);
//                    System.out.println(e1);

                    String sID = e2.text().substring(0, e2.text().indexOf("."));

                    String sDatum = e2.text().replaceAll(".", "");
                    sDatum = sDatum.substring(0, 4)+sDatum.substring(7, 8);

                    String sWS = "https://www.bayern.landtag.de/webangebot2/webangebot/protokolle;jsessionid=F2E67182B0985A0507DB5F4AE7BF1DDE?execution=e1s1";

                    //Jsoup.connect(sWS).data()



                    File dFile = new File(sOut+s+"/"+e2.text()+"_"+e1.text()+".pdf");
                    if(!dFile.exists()){

                        try {
                            FileUtils.downloadFile(dFile, e3.select("a").get(1).attr("href"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }

                }

            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


}
