package org.texttechnologylab.parliament.divisions;

import com.goebl.david.Request;
import com.goebl.david.Webb;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for Parsing Minutes of Baden-Würtemberg
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class BaWue {

    public static void main(String[] args) {

        String sBaseSavePath = args[0];

        String sBaseRequest = "https://parlis.landtag-bw.de/parlis/browse.tt.json";
        String sBaseResult = "https://parlis.landtag-bw.de/parlis/report.tt.html?report_id=";

        String sRequest = "{\"action\":\"SearchAndDisplay\",\"sources\":[\"Star\"],\"report\":{\"rhl\":\"main\",\"rhlmode\":\"add\",\"format\":\"suchergebnis-dokumentnummer\",\"mime\":\"html\",\"sort\":\"SORT03 SORT02\"},\"search\":{\"lines\":{\"l1\":\"P\",\"l2\":\"ID/NR\"},\"serverrecordname\":\"dokument\"}}";

        String sLP = "9,10,11,12,13,14,15,16,17";

        for (String s : sLP.split(",")) {

            new File(sBaseSavePath + s).mkdir();

            int a = 1;
            boolean itemsLeft = true;
            while (itemsLeft) {


                Request r = Webb.create().post(sBaseRequest);
                r = r.body(sRequest.replace("ID", s).replace("NR", "" + a));

                JSONObject rObject = r.ensureSuccess().asJsonObject().getBody();

                if (rObject.has("item_count")) {
                    itemsLeft = rObject.getInt("item_count") > 0;
                    String sID = rObject.getString("report_id");

                    try {
                        Document pEntry = Jsoup.connect(sBaseResult + sID).get();
//                    System.out.println(pEntry);

                        Element e = pEntry.select("section").select("span a").get(0);

                        File dFile = new File(sBaseSavePath + s + "/" + e.text().replace("/", "_") + ".pdf");

                        if (!dFile.exists()) {
                            FileUtils.downloadFile(dFile, e.attr("href"));
                        }

                        System.out.println(e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    itemsLeft = false;
                }

                a++;
            }


        }


    }
}
