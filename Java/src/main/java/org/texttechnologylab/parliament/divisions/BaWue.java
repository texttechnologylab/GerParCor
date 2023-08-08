package org.texttechnologylab.parliament.divisions;

import com.goebl.david.Request;
import com.goebl.david.Webb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

        String sLP = "17";

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


    @Test
    public void getOld() {

        System.getProperties().put("http.proxySet", "true");
        System.getProperties().put("http.proxyHost", "203.243.63.16");
        System.getProperties().put("http.proxyPort", "80"); //port is

        String sPath = "/tmp/gerparcor/bawueAlt/";
        new File(sPath).mkdir();

        String sBaseRequest = "https://www.landtag-bw.de/home/dokumente/dokumente-1952-1996/contentBoxes/drucksachen-plenarprotokolle-195.ltbw_drucksachen.oldDocumentSearchAction.do";

        Request r = Webb.create().post(sBaseRequest);

        Set<String> fnfe = new HashSet<>(0);


        String sBaseLink = "https://www.landtag-bw.de/files/live";

        for (int wp = 1; wp < 10; wp++) {

            new File(sPath+""+wp).mkdir();

            int iSearch = 1;
            boolean running=true;
            while(running) {
                r = Webb.create().post(sBaseRequest);
                r = r.param("searchTerm", (wp<10 ? "0"+wp : wp)+"/"+iSearch);
                r = r.param("doctype", "protokoll");
                r = r.param("wp", "1p");
                iSearch++;
                JSONObject rObject = new JSONObject();
                try {
                    rObject = r.ensureSuccess().asJsonObject().getBody();
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                if(rObject.length()==0){
                    running = false;
                    continue;

                }

                JSONArray testArray = rObject.getJSONArray("results");

                if(testArray.getJSONObject(0).has("Keine Ergebnisse gefunden")){
                    running=false;
                }
                else{
                    JSONArray pArray = rObject.getJSONArray("results");
                    for(int a=0; a<pArray.length(); a++){
                        JSONObject tObject = pArray.getJSONObject(a);
                        int finalWp = wp;
                        int finalA = a;
                        tObject.keySet().forEach(k->{
                            String sLink = tObject.getString(k);
                            if (finalA % 3 == 0) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            try {
                                File pFile = new File(sPath+""+ finalWp +"/"+sLink.substring(sLink.lastIndexOf("/")+1));
                                if(!pFile.exists()) {
                                    FileUtils.downloadFile(pFile, sBaseLink + sLink);
                                    System.out.println("Downloaded: "+pFile.getName());


                                }
//                                else{
//                                    System.out.println("Already Exists: "+pFile.getName());
//                                }
                            } catch (IOException e) {
                                fnfe.add(e.getMessage());
                            }
                        });
                    }
                }

            }

        }
        for (String f : fnfe) {
            System.out.println(f);

        }




    }

    @Test
    public void veryOld() throws IOException {

        String sPath = "/tmp/old/";
        File nFile = new File(sPath);
        nFile.mkdir();

        String sFile = "https://www.wlb-stuttgart.de/literatursuche/digitale-bibliothek/digitale-sammlungen/landtagsprotokolle/digitale-praesentation/zeitliche-gliederung/zeitraum-1952-1996/";

        Document pPage = Jsoup.connect(sFile).get();
        AtomicBoolean doRun = new AtomicBoolean(false);
        pPage.select("#c15879 table tbody tr a").forEach(e->{
            if(e.text().contains("1979")){
                doRun.set(true);
            }
            if(e.text().contains("Protokolle") && doRun.get()) {
                System.out.println(e.text());
                System.out.println(e.attr("href"));
                try {
                    Document internalPage = Jsoup.connect(e.attr("href")).get();

                    internalPage.select(".tx-dlf-tools-pdf-work a").forEach(el -> {
                        System.out.println(el.attr("href"));

                        try {
                            FileUtils.downloadFile(new File(sPath+e.text()+".pdf"), el.attr("href"));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                    });

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });


    }
}
