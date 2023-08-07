package org.texttechnologylab.parliament;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for Parsing Minutes of Liechtenstein
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Liechtenstein {

    public static void main(String[] args) throws IOException {


        String sPath = "https://www.landtag.li/protokolle/default.aspx?mode=lp&prim=YEAR";

        String sDownload = "https://lp.rechtportal.li/PDF/Landtagsprotokoll_YEAR_MONTH_DAY.pdf";

        String sOut = args[0];
        new File(sOut).mkdir();
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";

        for(int a=2021; a<=2023; a++){

            new File(sOut+a).mkdir();

            Document pDocument = Jsoup.connect(sPath.replace("YEAR", ""+a)).userAgent(ua).get();

            int finalA = a;
            pDocument.select("td.selectionDate").forEach(e->{

                String sClick = e.attr("onClick");

                sClick = sClick.substring(sClick.indexOf("value="));
                sClick = sClick.replace("tag=", "");
                sClick = sClick.replace("value=", "");
                sClick = sClick.replace("'", "");
                String[] monthDay = sClick.split("&");

//                System.out.println(monthDay);

                File dFile = new File(sOut+finalA+"/"+ finalA +"_"+monthDay[0]+"_"+monthDay[1]+".pdf");

                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                if(!dFile.exists()){
                    try {
                        String sDownloadNew = sDownload.replace("YEAR", ""+finalA).replace("MONTH", monthDay[0]).replace("DAY", monthDay[1]);
                        System.out.println(sDownloadNew);
                        FileUtils.downloadFile(dFile, sDownloadNew);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }


            });

        }

    }

}
