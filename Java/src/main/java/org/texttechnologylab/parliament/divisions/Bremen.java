package org.texttechnologylab.parliament.divisions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Class for Parsing Minutes of Bremen
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Bremen {

    public static void main(String[] args) throws IOException {

        String sPath = "https://paris.bremische-buergerschaft.de/starweb/paris/servlet.starweb?path=paris/LISSHPLPRList.web&search=WP=KEY AND PARL=L AND DART=P";

        String sOut = args[0];

        String sWP = "14,15,16,17,18,19,20";

        for (String s : sWP.split(",")) {

            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";
            URL nURL = new URL(sPath.replaceAll("KEY", s).replaceAll(" ", "%20"));

            new File(sOut+s).mkdir();

            //Document pDocument = Jsoup.parse(nURL, 5000);
            String sHTML = Jsoup.connect(nURL.toString()).userAgent(ua).get().html();
            Document d = Jsoup.parse(sHTML);
            Elements options = d.select("select option");

            options.forEach(o->{
                String sValue = o.attr("value");
                String sResult = null;
                try {
                    sResult = Jsoup.connect("https://paris.bremische-buergerschaft.de/"+sValue).userAgent(ua).get().html();
                    //System.out.println(sResult);

                    Document resultDocument = Jsoup.parse(sResult);
                    resultDocument.select("td a").forEach(el->{
                        if(el.text().equalsIgnoreCase("PDF")){
                            //System.out.println(el.attr("href"));

                            try {
                                File pFile = new File(sOut+s+"/"+el.attr("href").substring(el.attr("href").lastIndexOf("/")+1));

                                if(!pFile.exists()){
                                    Thread.sleep(1000l);
                                    FileUtils.downloadFile(pFile, el.attr("href").replace("http://", "https://"));
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }



            });



        }


    }

}
