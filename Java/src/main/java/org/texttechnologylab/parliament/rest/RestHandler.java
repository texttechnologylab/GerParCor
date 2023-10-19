package org.texttechnologylab.parliament.rest;

import freemarker.template.Configuration;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class RestHandler {

    private ParliamentFactory pFactory = null;

    private Configuration cf = new Configuration(Configuration.VERSION_2_3_21);


    private MongoDBConnectionHandler dbConnection = null;

    public RestHandler(ParliamentFactory pFactory){
        this.pFactory = pFactory;
    }

    public void init() throws IOException {

//        String templatesPath = "/home/staff_homes/abrami/Projects/GitHub/GerParCor/template";
        String templatesPath = "/home/gabrami/Projects/GitHub/GerParCor/template";
        cf.setDirectoryForTemplateLoading(new File(templatesPath));

        get("/", "text/html", (request, response)->{

            Map<String, Object> attributes = new HashMap<>();

            attributes.put("factory", this.pFactory);
//            attributes.put("protocols", this.pFactory.listProtocols());

            return new ModelAndView(attributes, "index.ftl");

        }, new FreeMarkerEngine(cf));

    }



}
