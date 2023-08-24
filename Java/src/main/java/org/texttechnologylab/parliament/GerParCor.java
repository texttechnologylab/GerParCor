package org.texttechnologylab.parliament;

import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.LuaConsts;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.parliament.duui.MongoDBImporter;
import org.texttechnologylab.parliament.rest.RestHandler;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class GerParCor implements SparkApplication {

    public static void main(String[] args) throws Exception {
        GerParCor parliament = new GerParCor();
        importData();

        parliament.init();
    }

    public static void importData() throws Exception {

        int iWorkers = 1;

        AsyncCollectionReader testReader = new AsyncCollectionReader("/storage/xmi/GerParCorDownload", ".xmi.gz", 1, 10, false, "/tmp/testgerparcor", false, "all");

        DUUILuaContext ctx = new DUUILuaContext().withJsonLibrary();

        DUUIComposer composer = new DUUIComposer()
                //       .withStorageBackend(new DUUIArangoDBStorageBackend("password",8888))
                .withWorkers(iWorkers)
                .withLuaContext(ctx).withSkipVerification(true);

        // Instantiate drivers with options
        DUUIUIMADriver uima_driver = new DUUIUIMADriver();

        // A driver must be added before components can be added for it in the composer.
        composer.addDriver(uima_driver);

        composer.add(new DUUIUIMADriver.Component(createEngineDescription(MongoDBImporter.class, MongoDBImporter.PARAM_DBConnection, "/home/staff_homes/abrami/Projects/GitHub/GerParCor/Java/src/main/resources/rw")).build().withScale(iWorkers));

        composer.run(testReader, "import");

    }

    @Override
    public void init() {
        Spark.port(8081);
        Spark.staticFileLocation("html");

        String sDBConfig = GerParCor.class.getClassLoader().getResource("rw").getPath();

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
