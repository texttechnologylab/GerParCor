import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.bson.conversions.Bson;
import org.dkpro.core.io.xmi.XmiWriter;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.connection.mongodb.MongoDBConfig;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUIAsynchronousProcessor;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategy;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategyByDelemiter;
import org.texttechnologylab.parliament.duui.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Export {

    @Test
    public void execute() throws Exception {

        int iScale = 5;

        File pFile = new File(Export.class.getClassLoader().getResource("new_ro").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);
        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": \"Weimar_Republic\"}";

        List<Bson> pQuery = new ArrayList<>();
        Date pDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-02-14");
        pQuery.add(Aggregates.lookup("grid.files", "grid", "filename", "file"));
        pQuery.add(Aggregates.match(Filters.and(Filters.lt("file.uploadDate", pDate), Filters.regex("documentURI", "older"))));
        pQuery.add(Aggregates.sort(Sorts.ascending("file.length")));


        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, sFilter));

        DUUIComposer composer = new DUUIComposer()
                .withSkipVerification(true)
                .withWorkers(iScale)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());

        DUUIDockerDriver dockerDriver = new DUUIDockerDriver();
        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver();
        DUUISwarmDriver swarmDriver = new DUUISwarmDriver();
        composer.addDriver(dockerDriver, remoteDriver, uimaDriver, swarmDriver);

        AnalysisEngineDescription writerEngine = createEngineDescription(CSVExport.class,
                CSVExport.PARAM_TARGET_LOCATION, "/tmp/csv/"
        );

        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());

        composer.run(processor, "csv");

        //composer.shutdown();

    }

}
