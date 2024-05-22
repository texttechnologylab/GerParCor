package org.texttechnologylab.parliament;

import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.impl.ParliamentFactory_Impl;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.parliament.rest.RestHandler;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.io.IOException;

public class GerParCor implements SparkApplication {

    public static void main(String[] args) throws Exception {
        GerParCor parliament = new GerParCor();
//        importData();

        parliament.init();
    }


    @Override
    public void init() {
        Spark.port(8081);
        Spark.staticFileLocation("html");

        String sDBConfig = GerParCor.class.getClassLoader().getResource("new_ro").getPath();

        try {
            MongoDBConfig dbConfig = new MongoDBConfig(sDBConfig);
            MongoDBConnectionHandler pHandler = new MongoDBConnectionHandler(dbConfig);


            ParliamentFactory pFactory = new ParliamentFactory_Impl(pHandler);

            //pFactory.getTimeRanges();

            RestHandler m = new RestHandler(pFactory);
            m.init();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() {
        SparkApplication.super.destroy();
    }

}
