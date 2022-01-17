package org.texttechnologylab.parliament.divisions;

import com.goebl.david.Request;
import com.goebl.david.Webb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Date;

/**
 * Class for Parsing Minutes of Saarland
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Saarland {

    public static void main(String[] args){

        String sOutPath = args[0];

        int iLimitSkip = 10;

        int c=0;

        boolean isRunning = true;

        while(isRunning) {

            try {
                Thread.sleep(3000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String stringJSONRequest = "{\"Filter\":{\"Periods\":[]},\"Pageination\":{\"Skip\":"+(c*iLimitSkip)+",\"Take\":"+iLimitSkip+"},\"Sections\":{\"Print\":false,\"PlenaryProtocol\":true,\"Law\":false,\"PublicConsultation\":false,\"Operations\":false},\"Sort\":{\"SortType\":0,\"SortValue\":0},\"OnlyTitle\":false,\"Value\":\"\",\"CurrentSearchTab\":2,\"KendoFilter\":null}";

            Request post = Webb.create().post("https://www.landtag-saar.de/umbraco/aawSearchSurfaceController/SearchSurface/GetSearchResults/");
            post = post.body(stringJSONRequest);

            JSONObject rObject = post.ensureSuccess().asJsonObject().getBody();

            JSONArray itemArray = rObject.getJSONArray("Items");

            if(itemArray.length()==0){
                isRunning = false;
            }

            for(int a=0; a<itemArray.length(); a++){

                JSONObject tObject = itemArray.getJSONObject(a);
//                System.out.println(tObject.toString(1));

                String sNumber = tObject.getString("DocumentNumber");
                String sDate = tObject.getString("PublicDate");
                System.out.println(sNumber);

                new File(sOutPath+sNumber.split("/")[0]).mkdir();

                Date pDate = new Date(Long.valueOf(sDate.replace("/Date(","").replace(")/", "")));

                File dFile = new File(sOutPath+sNumber.split("/")[0]+"/"+sNumber.replace("/", "_")+"_"+pDate.toString()+".pdf");

                if(!dFile.exists()){
                    try {
                        FileUtils.downloadFile(dFile, "https://www.landtag-saar.de/Downloadfile.ashx?"+tObject.getString("FilePath").replace("/file.ashx?", "")+"&directDL=true");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }


            c++;
        }


    }

}
