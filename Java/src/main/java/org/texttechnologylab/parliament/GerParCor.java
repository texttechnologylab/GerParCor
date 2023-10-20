package org.texttechnologylab.parliament;

import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.LuaConsts;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.impl.ParliamentFactory_Impl;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.parliament.duui.MongoDBImporter;
import org.texttechnologylab.parliament.rest.RestHandler;
import org.texttechnologylab.uimadb.databases.mongo.Mongo;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

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

        String sDBConfig = GerParCor.class.getClassLoader().getResource("rw").getPath();

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
