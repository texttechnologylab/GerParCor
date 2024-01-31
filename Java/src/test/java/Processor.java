import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.connection.mongodb.MongoDBConfig;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUIAsynchronousProcessor;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategy;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategyByDelemiter;
import org.texttechnologylab.parliament.duui.CountAnnotations;
import org.texttechnologylab.parliament.duui.DUUIGerParCorReader;
import org.texttechnologylab.parliament.duui.GerParCorWriter;
import org.texttechnologylab.parliament.duui.SetLanguage;
import org.texttechnologylab.uima.type.CategorizedSentiment;

import java.io.File;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Processor {

    @Test
    public void executeSentiment() throws Exception {

        int iScale = 60;

        File pFile = new File(Processor.class.getClassLoader().getResource("rw").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);
        String sFilter = "{}";

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

        DUUIPipelineComponent language = new DUUIUIMADriver.Component(createEngineDescription(SetLanguage.class,
                SetLanguage.PARAM_LANGUAGE, "de")).withScale(iScale).build();

        DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-transformers-sentiment:latest").withScale(iScale)
                .withParameter("model_name", "oliverguhr/german-sentiment-bert")
                .withParameter("selection", "text")
                .build();

        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiter()
                .withDelemiter(".")
                .withLength(300000);

        composer.add(language);
        composer.add(component);
//
//        AnalysisEngineDescription writerEngine = createEngineDescription(GerParCorWriter.class,
//                GerParCorWriter.PARAM_DBConnection, pFile.getAbsolutePath()
//        );

        AnalysisEngineDescription countAnnos = createEngineDescription(CountAnnotations.class);

        composer.add(new DUUIUIMADriver.Component(countAnnos).withScale(iScale).build());

        composer.run(processor, "reloaded");

        composer.shutdown();

    }

    @Test
    public void executeSpaCy() throws Exception {

        int iScale = 5;

        File pFile = new File(Processor.class.getClassLoader().getResource("new").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);
        String sFilter = "{\"annotations.Token\":0}";

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

        DUUIPipelineComponent component = new DUUIDockerDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withImageFetching().withScale(iScale)
                .build();

        //DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withScale(iScale)
//                .build();

        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiter()
                .withDelemiter(".")
                .withLength(300000);

        composer.add(component.withSegmentationStrategy(pStrategy));

        AnalysisEngineDescription writerEngine = createEngineDescription(GerParCorWriter.class,
                GerParCorWriter.PARAM_DBConnection, pFile.getAbsolutePath()
        );

        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());

        composer.run(processor, "spacy");

        //composer.shutdown();

    }
    public void example() throws Exception {

        int iScale = 2;

        File pFile = new File(Processor.class.getClassLoader().getResource("rw").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);
        String sFilter = "{}";

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

        DUUIPipelineComponent language = new DUUIUIMADriver.Component(createEngineDescription(SetLanguage.class,
                SetLanguage.PARAM_LANGUAGE, "de")).withScale(iScale).build();

        DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-transformers-sentiment:latest").withScale(iScale)
                .withParameter("model_name", "oliverguhr/german-sentiment-bert")
                .withParameter("selection", "text")
                .build();

        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiter()
                .withDelemiter(".")
                .withLength(300000);

        composer.add(language);
        composer.add(component);
//
//        AnalysisEngineDescription writerEngine = createEngineDescription(GerParCorWriter.class,
//                GerParCorWriter.PARAM_DBConnection, pFile.getAbsolutePath()
//        );

        AnalysisEngineDescription countAnnos = createEngineDescription(CountAnnotations.class);

        composer.add(new DUUIUIMADriver.Component(countAnnos).withScale(iScale).build());

        composer.run(processor, "reloaded");

        composer.shutdown();

    }


}
