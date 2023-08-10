package org.texttechnologylab.parliament.crawler.divisions.germany;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.texttechnologylab.utilities.helper.RESTUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for Parsing Minutes of Hamburg
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Hamburg {

    public static void main(String[] args) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("DokumentenArtId", 2);
        params.put("LegislaturPeriodenNummer", 22);
        params.put("DokumentenNummer", 1);
        params.put("AFHTOKE", "U6MYsEkYJV8IyJK3TMX2RqOi3gftZMoS+stlI+qmo7MNH4ZXl2NB0cTN+y0C2AtVMqsA5wD+t8YMzBe71nztIxoiArAzjpeesY25PNFiCtRXXiVIFeOscA9Oycv50ipMlnrBEFJfCP2RWXXrU/Szg5Zw9iZNuLaFjc3+xjtEQaNRaPqnLuorewwZNG3Hv9x9rm2Bk/Gcb4tsSQ48DCw27Jgb75PeW/FXCKSfyBGYhY2YV6+v7F/pPcjJumhEgVo/z4PILgvEqcoJI6l4WqEeVHQpbP6fYcOIaGn0mb6ycGJuYhVU7UQzAK7/3GiTEloiT6SmTNxdG4W4Q7Y35zc+0WYr2Ds2tFV/");

        try {
            JSONObject rObject = RESTUtils.getObjectFromRest("https://www.buergerschaft-hh.de/parldok/dokumentennummer", RESTUtils.METHODS.POST, params);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        Document pDocument = Jsoup.parse(new URL("https://www.buergerschaft-hh.de/parldok/dokumentennummer/1"), 1000);


        System.out.println(pDocument);

    }

}
