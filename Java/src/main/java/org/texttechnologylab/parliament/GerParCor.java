package org.texttechnologylab.parliament;

import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.parliament.rest.RestHandler;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.io.IOException;

public class GerParCor implements SparkApplication {

    public static void main(String[] args) {

    }

    @Override
    public void init() {
        Spark.port(8081);
        Spark.staticFileLocation("html");

        String sDBConfig = GerParCor.class.getClassLoader().getResource("dbconnection.txt").getPath();

        try {
            MongoDBConfig dbConfig = new MongoDBConfig(sDBConfig);
            MongoDBConnectionHandler pHandler = new MongoDBConnectionHandler(dbConfig);

            RestHandler m = new RestHandler(pHandler);
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
