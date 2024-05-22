package org.texttechnologylab.parliament.rest;

import com.mongodb.client.model.Filters;
import freemarker.template.Configuration;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.Protocol;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.utilities.helper.RESTUtils;
import org.texttechnologylab.utilities.helper.SparkUtils;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class RestHandler {

    private ParliamentFactory pFactory = null;

    private Configuration cf = new Configuration(Configuration.VERSION_2_3_21);


    private MongoDBConnectionHandler dbConnection = null;

    public RestHandler(ParliamentFactory pFactory){
        this.pFactory = pFactory;
    }

    public void init() throws IOException {

        String templatesPath = "/home/staff_homes/abrami/Projects/GitHub/GerParCor/template";
//        String templatesPath = "/home/gabrami/Projects/GitHub/GerParCor/template";
        Spark.externalStaticFileLocation(templatesPath);
        cf.setDirectoryForTemplateLoading(new File(templatesPath));

        get("/", "text/html", (request, response)->{

            Map<String, Object> attributes = new HashMap<>();

            attributes.put("factory", this.pFactory);
            attributes.put("country", "all");
            attributes.put("devision", "all");
            attributes.put("parliament", "all");

            return new ModelAndView(attributes, "index.ftl");

        }, new FreeMarkerEngine(cf));

        post("/", "text/html", (request, response)->{

            String sCountry = request.queryParams().contains("country") ? request.queryParams("country") : "all";
            String sDevision = request.queryParams().contains("devision") ? request.queryParams("devision") : "all";
            String sParliament = request.queryParams().contains("parliament") ? request.queryParams("parliament") : "all";

            Map<String, Object> attributes = new HashMap<>();

            attributes.put("factory", this.pFactory);
            attributes.put("country", sCountry);
            attributes.put("devision", sDevision);
            attributes.put("parliament", sParliament);


            return new ModelAndView(attributes, "index.ftl");

        }, new FreeMarkerEngine(cf));

        get("/download/:id", "application/json", ((request, response) -> {
            String sDownloadID = request.params("id");
            File rFile = this.pFactory.getProtocol(sDownloadID).getDocumentAsFile();

            return RESTUtils.returnFile(response, rFile);
        }));

        get("/rest", "application/json", ((request, response) -> {

            try {

                JSONObject rObject = new JSONObject();

                String sQuery = request.queryParams("query");
                JSONArray rArray = new JSONArray();

                switch (sQuery){
                    case "country":
                        for (String s : pFactory.listCountries()) {
                            rArray.put(s);
                        }

                    break;

                    case "parliament":
                        for (String s : pFactory.listParliaments()) {
                            rArray.put(s);
                        }

                    break;

                    case "devision":
                        for (String s : pFactory.listDevisions()) {
                            rArray.put(s);
                        }

                    break;

                    case "request":

                        System.out.println(request);

                        List<Bson> query = new ArrayList<>();
//                        query.add(Aggregates.unwind("$timestamp"));
//                        query.add(Aggregates.unwind("$meta.country"));
//                        query.add(Aggregates.group("$meta.parliament", new BsonField("valueMin", BsonDocument.parse("{ $min: \"$timestamp\" }")), new BsonField("valueMax", BsonDocument.parse("{ $max: \"$timestamp\" }"))));
//                        query.add(Aggregates.sort(Sorts.ascending("meta.country", "meta.parliament")));


                        boolean bAnd = true;

                        if(request.queryParams().contains("id")){
                            bAnd = false;

                            JSONArray pArray = new JSONArray();
                            try {
                                pArray = new JSONArray(request.queryParams("id"));
                            } catch (Exception e) {
                                pArray.put(request.queryParams("id"));
                            }
                            for (Object o : pArray) {
                                query.add(Filters.eq("_id", new ObjectId(o.toString())));
                            }

                        }
                        else {

                            if (request.queryParams().contains("date")) {

                            }
                            else{



                            }

                            if (request.queryParams().contains("country")) {

                                JSONArray pArray = new JSONArray();
                                try {
                                    pArray = new JSONArray(request.queryParams("country"));
                                } catch (Exception e) {
                                    pArray.put(request.queryParams("country"));
                                }
                                for (Object o : pArray) {
                                    query.add(Filters.eq("meta.country", o));
                                }

                            }

                            if (request.queryParams().contains("parliament")) {
                                JSONArray pArray = new JSONArray();
                                try {
                                    pArray = new JSONArray(request.queryParams("parliament"));
                                } catch (Exception e) {
                                    pArray.put(request.queryParams("parliament"));
                                }
                                for (Object o : pArray) {
                                    query.add(Filters.eq("meta.parliament", o));
                                }
                            }

                            if (request.queryParams().contains("devision")) {
                                JSONArray pArray = new JSONArray();
                                try {
                                    pArray = new JSONArray(request.queryParams("devision"));
                                } catch (Exception e) {
                                    pArray.put(request.queryParams("devision"));
                                }
                                for (Object o : pArray) {
                                    query.add(Filters.eq("meta.devision", o));
                                }
                            }
                        }

                        Bson bsonQuery = null;
                        if(bAnd){
                            bsonQuery = Filters.and(query);
                        }
                        else{
                            bsonQuery = Filters.or(query);
                        }

                        for (Protocol protocol : pFactory.doQuery(bsonQuery)) {
                            rArray.put(protocol.toJSON());
                        }
                        rObject.put("result", rArray);

                    break;


                }

                if(sQuery.equalsIgnoreCase("download")) {
                    String sDownloadID = request.queryParams("id");
                    File rFile = this.pFactory.getProtocol(sDownloadID).getDocumentAsFile();

                    return RESTUtils.returnFile(response, rFile);

                }
                else {
                    rObject.put("result", rArray);

                    return SparkUtils.prepareReturnSuccess(response, rObject);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return SparkUtils.prepareReturnFailure(response, e.getMessage(), e.getStackTrace());
            }

        }));

    }



}
