import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.connection.mongodb.MongoDBConfig;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIPipelineComponent;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUIAsynchronousProcessor;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.parliament.duui.CheckingProblems;
import org.texttechnologylab.parliament.duui.DUUIGerParCorReader;
import org.texttechnologylab.parliament.duui.DependencyDistanceEngine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Dependency {

    @Test
    public void run(){

        String pMongoDbConfigPath = System.getProperty("config", "src/main/resources/new_ro");
        String pFilter = System.getProperty("filter", "{\"meta.parliament\": \"Reichstag\"}");
        String pOutput = System.getProperty("output", "/tmp/dep");
//        String pOutput = System.getProperty("output", "/storage/projects/stoeckel/syntactic-language-change/mdd/");
        boolean pOverwrite = Boolean.parseBoolean(System.getProperty("overwrite", "false"));
        CompressionMethod pCompression = CompressionMethod.valueOf(System.getProperty("compression", "NONE"));
        boolean pFailOnError = Boolean.parseBoolean(System.getProperty("failOnError", "false"));

        List<Bson> pQuery = new ArrayList<>();
        pQuery.add(Aggregates.lookup("grid.files", "grid", "filename", "file"));
        pQuery.add(Aggregates.sort(Sorts.ascending("file.length")));


        int pScale = 5;

        try {
            MongoDBConfig mongoDbConfig = new MongoDBConfig(pMongoDbConfigPath);
//            DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(mongoDbConfig, pQuery));
            DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(mongoDbConfig, pFilter));

            DUUIComposer composer = new DUUIComposer()
                    .withSkipVerification(true)
                    .withWorkers(pScale)
//                    .withCasPoolsize(4 * pScale)
                    .withLuaContext(new DUUILuaContext().withJsonLibrary());

            DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
            composer.addDriver(uimaDriver);

            DUUIPipelineComponent dependency = new DUUIUIMADriver.Component(
                    createEngineDescription(
                            DependencyDistanceEngine.class,
                            DependencyDistanceEngine.PARAM_TARGET_LOCATION, pOutput,
                            DependencyDistanceEngine.PARAM_OVERWRITE, pOverwrite,
                            DependencyDistanceEngine.PARAM_COMPRESSION, pCompression,
                            DependencyDistanceEngine.PARAM_FAIL_ON_ERROR, pFailOnError
                    )
            ).withScale(pScale).build();

            composer.add(dependency);
            composer.run(processor, "mDD");
//            composer.shutdown();

//            Assertions.assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
//            Assertions.fail();
        }
    }

}
