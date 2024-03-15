import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.bson.conversions.Bson;
import org.dkpro.core.io.xmi.XmiWriter;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.connection.mongodb.MongoDBConfig;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUIAsynchronousProcessor;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIFileReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.reader.DUUIFileReaderLazy;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.writer.TTLabXmiWriter;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategy;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategyByDelemiter;
import org.texttechnologylab.annotation.SpacyAnnotatorMetaData;
import org.texttechnologylab.parliament.duui.*;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Processor {

    @Test
    public void executeSentiment() throws Exception {

        int iScale = 10;

        File pFile = new File(Processor.class.getClassLoader().getResource("new_rw").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);
        List<Bson> pQuery = new ArrayList<>();

        pQuery.add(Aggregates.lookup("grid.files", "grid", "filename", "file"));
        pQuery.add(Aggregates.match(Filters.exists("sentiment_value", false)));
        pQuery.add(Aggregates.sort(Sorts.ascending("file.length")));
//        pQuery.add(Aggregates.sample(5));

        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, pQuery, 1000));

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

//        DUUIPipelineComponent component = new DUUIRemoteDriver.Component("http://localhost:5000").withScale(iScale)
//                .build();
        DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/gervader_duui:latest").withScale(iScale)
                .build();

        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiter()
                .withDelemiter(".")
                .withLength(300000);

//        composer.add(language);
//        composer.add(component.withSegmentationStrategy(pStrategy));
        composer.add(component);
//
        AnalysisEngineDescription writerEngine = createEngineDescription(GerParCorWriter.class,
                GerParCorWriter.PARAM_DBConnection, pFile.getAbsolutePath()
        );

        AnalysisEngineDescription writerEngineXMI = createEngineDescription(TTLabXmiWriter.class,
                TTLabXmiWriter.PARAM_PRETTY_PRINT, true,
                TTLabXmiWriter.PARAM_VERSION, "1.1",
                TTLabXmiWriter.PARAM_TARGET_LOCATION, "/tmp/gerparcor"
        );

//        AnalysisEngineDescription countAnnos = createEngineDescription(CountAnnotations.class);

//        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());
        composer.add(new DUUIUIMADriver.Component(writerEngineXMI).withScale(iScale).build());

        composer.run(processor, "reloaded");

        composer.shutdown();

    }

    @Test
    public void executeSpaCy() throws Exception {

        int iScale = 3;

        File pFile = new File(Processor.class.getClassLoader().getResource("new_rw").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);
        String sFilter = "{\"documentURI\": { $regex: \"older\"}, \"annotations.DocumentMetaData\": 0}";

        List<Bson> pQuery = new ArrayList<>();
        Date pDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-02-14");
        pQuery.add(Aggregates.lookup("grid.files", "grid", "filename", "file"));
        pQuery.add(Aggregates.match(Filters.and(Filters.lt("file.uploadDate", pDate), Filters.regex("documentURI", "older"))));
        pQuery.add(Aggregates.sort(Sorts.ascending("file.length")));


        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, pQuery));

        DUUIComposer composer = new DUUIComposer()
                .withSkipVerification(true)
                .withWorkers(iScale)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());

        DUUIDockerDriver dockerDriver = new DUUIDockerDriver();
        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver();
        DUUISwarmDriver swarmDriver = new DUUISwarmDriver();
        composer.addDriver(dockerDriver, remoteDriver, uimaDriver, swarmDriver);


        DUUIPipelineComponent removeComponent = new DUUIUIMADriver.Component(createEngineDescription(RemoveAnnotations.class)).withScale(iScale)
                .build();

        DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withScale(iScale)
                .build();

        //DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withScale(iScale)
//                .build();

        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiterModified()
                .withDelemiter(".")
                .withDebug()
                .withLength(500000);

        composer.add(removeComponent);
        composer.add(component.withSegmentationStrategy(pStrategy));

        AnalysisEngineDescription writerEngine = createEngineDescription(GerParCorWriter.class,
                GerParCorWriter.PARAM_DBConnection, pFile.getAbsolutePath()
        );

        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());

        composer.run(processor, "spacy");

        //composer.shutdown();

    }

    @Test
    public void executeSpaCyManual() throws Exception {

        int iScale = 1;

        JCas pCas = JCasFactory.createJCas();

        CasIOUtils.load(new FileInputStream("/tmp/SampleNew.xmi"), pCas.getCas());

        DUUIComposer composer = new DUUIComposer()
                .withSkipVerification(true)
                .withWorkers(iScale)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());

        DUUIDockerDriver dockerDriver = new DUUIDockerDriver();
        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver();
        DUUISwarmDriver swarmDriver = new DUUISwarmDriver();
        composer.addDriver(dockerDriver, remoteDriver, uimaDriver, swarmDriver);


        DUUIPipelineComponent component = new DUUIDockerDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withScale(iScale)
                .build();

        DUUIPipelineComponent remove = new DUUIUIMADriver.Component(createEngineDescription(RemoveAnnotations.class, RemoveAnnotations.PARAM_Classes, SpacyAnnotatorMetaData.class.getName()+","+ Dependency.class.getName())).withScale(iScale)
                .build();



        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiterModified()
                .withDelemiter(".")
                .withDebug()
                .withLength(500000);

        composer.add(component);
        composer.add(remove);


        AnalysisEngineDescription xmiEngine = createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, "/tmp/example/",
                XmiWriter.PARAM_PRETTY_PRINT, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1"
        );
        composer.add(new DUUIUIMADriver.Component(xmiEngine).withScale(iScale).build());

        composer.run(pCas, "spacy");

        //composer.shutdown();

    }

    @Test
    public void executeSpaCyThueringen() throws Exception {

        int iScale = 1;

        File pFile = new File(Processor.class.getClassLoader().getResource("new_rw").getFile());

        MongoDBConfig pConfig = new MongoDBConfig(pFile);

        List<Bson> pQuery = new ArrayList<>();
        pQuery.add(Aggregates.lookup("grid.files", "grid", "filename", "file"));
        pQuery.add(Aggregates.match(Filters.eq("meta.parliament", "Thueringen")));
        pQuery.add(Aggregates.sort(Sorts.descending("file.length")));

        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, pQuery));

        DUUIComposer composer = new DUUIComposer()
                .withSkipVerification(true)
                .withWorkers(iScale)
                .withLuaContext(new DUUILuaContext().withJsonLibrary());

        DUUIDockerDriver dockerDriver = new DUUIDockerDriver();
        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver();
        DUUISwarmDriver swarmDriver = new DUUISwarmDriver();
        composer.addDriver(dockerDriver, remoteDriver, uimaDriver, swarmDriver);


        DUUIPipelineComponent removeComponent = new DUUIUIMADriver.Component(createEngineDescription(RemoveAnnotations.class)).withScale(iScale)
                .build();

        DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withScale(iScale)
                .build();

        //DUUIPipelineComponent component = new DUUISwarmDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4").withScale(iScale)
//                .build();

        DUUISegmentationStrategy pStrategy = new DUUISegmentationStrategyByDelemiterModified()
                .withDelemiter(".")
                .withDebug()
                .withLength(500000);

        composer.add(removeComponent);
        composer.add(component.withSegmentationStrategy(pStrategy));

        AnalysisEngineDescription xmiEngine = createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, "/tmp/example/",
                XmiWriter.PARAM_PRETTY_PRINT, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1"
        );
        composer.add(new DUUIUIMADriver.Component(xmiEngine).withScale(iScale).build());

        composer.run(processor, "spacy");

        //composer.shutdown();

    }

//    @Test
//    public void executeDouble() throws Exception {
//
//        int iScale = 1;
//
//        File pFile = new File(Processor.class.getClassLoader().getResource("new_ro").getFile());
//
//        MongoDBConfig pConfig = new MongoDBConfig(pFile);
////        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": { $regex: \"Weimar\"}}";
//
////        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": \"Third_Reich\"}";
//        String sFilter = "{\"documentURI\": { $regex: \"older\"}, \"annotations.DocumentMetaData\": 1}";
////        String sFilter = "{\"documentURI\": { $regex: \"older\"}}";
//
//        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, sFilter));
//
//        DUUIComposer composer = new DUUIComposer()
//                .withSkipVerification(true)
//                .withWorkers(iScale)
//                .withLuaContext(new DUUILuaContext().withJsonLibrary());
//
//        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
//        composer.addDriver(uimaDriver);
//
//        AnalysisEngineDescription writerEngine = createEngineDescription(CheckingDouble.class);
//
//        AnalysisEngineDescription xmiEngine = createEngineDescription(XmiWriter.class,
//                XmiWriter.PARAM_TARGET_LOCATION, "/tmp/example/",
//                XmiWriter.PARAM_PRETTY_PRINT, true,
//                XmiWriter.PARAM_OVERWRITE, true,
//                XmiWriter.PARAM_VERSION, "1.1",
//                XmiWriter.PARAM_COMPRESSION, "GZIP"
//        );
//
////        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());
//
//        composer.add(new DUUIUIMADriver.Component(xmiEngine).withScale(iScale).build());
//
//        composer.run(processor, "checking");
//
//        //composer.shutdown();
//
//    }
//
//    @Test
//    public void executeBaWue() throws Exception {
//
//        int iScale = 3;
//
//        File pFile = new File(Processor.class.getClassLoader().getResource("new_ro").getFile());
//
//        MongoDBConfig pConfig = new MongoDBConfig(pFile);
////        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": { $regex: \"Weimar\"}}";
//
////        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": \"Third_Reich\"}";
////        String sFilter = "{\"documentURI\": { $regex: \"older\"}}";
//        String sFilter = "{\"documentURI\": { $regex: \"older\"}}";
//
//        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, sFilter));
//
//        DUUIComposer composer = new DUUIComposer()
//                .withSkipVerification(true)
//                .withWorkers(iScale)
//                .withLuaContext(new DUUILuaContext().withJsonLibrary());
//
//        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
//        composer.addDriver(uimaDriver);
//
//        AnalysisEngineDescription writerEngine = createEngineDescription(CheckingDouble.class);
//
//        AnalysisEngineDescription xmiEngine = createEngineDescription(XmiWriter.class,
//                XmiWriter.PARAM_TARGET_LOCATION, "/tmp/xmiExample/",
//                XmiWriter.PARAM_PRETTY_PRINT, true,
//                XmiWriter.PARAM_OVERWRITE, true,
//                XmiWriter.PARAM_VERSION, "1.1",
//                XmiWriter.PARAM_COMPRESSION, "GZIP"
//        );
//
////        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());
//
//        composer.add(new DUUIUIMADriver.Component(xmiEngine).withScale(iScale).build());
//
//        composer.run(processor, "checking");
//
//        //composer.shutdown();
//
//    }

//    @Test
//    public void exporter() throws Exception {
//
//        int iScale = 1;
//
//        File pFile = new File(Processor.class.getClassLoader().getResource("new_ro").getFile());
//
//        MongoDBConfig pConfig = new MongoDBConfig(pFile);
////        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": { $regex: \"Weimar\"}}";
//
////        String sFilter = "{\"meta.parliament\": \"Reichstag\", \"meta.comment\": \"Third_Reich\"}";
////        String sFilter = "{\"documentURI\": { $regex: \"older\"}}";
//        String sFilter = "{\"grid\": \"24cd7c2f555a862876cfb2ad7cd62309\"}";
//
//        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(new DUUIGerParCorReader(pConfig, sFilter));
//
//        DUUIComposer composer = new DUUIComposer()
//                .withSkipVerification(true)
//                .withWorkers(iScale)
//                .withLuaContext(new DUUILuaContext().withJsonLibrary());
//
//        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();
//        composer.addDriver(uimaDriver);
//
//        AnalysisEngineDescription writerEngine = createEngineDescription(CheckingDouble.class);
//
//        AnalysisEngineDescription xmiEngine = createEngineDescription(XmiWriter.class,
//                XmiWriter.PARAM_TARGET_LOCATION, "/tmp/xmiExample/",
//                XmiWriter.PARAM_PRETTY_PRINT, true,
//                XmiWriter.PARAM_OVERWRITE, true,
//                XmiWriter.PARAM_VERSION, "1.1",
//                XmiWriter.PARAM_COMPRESSION, "GZIP"
//        );
//
////        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());
//
//        composer.add(new DUUIUIMADriver.Component(xmiEngine).withScale(iScale).build());
//
//        composer.run(processor, "checking");
//
//        //composer.shutdown();
//
//    }
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

//        AnalysisEngineDescription countAnnos = createEngineDescription(CountAnnotations.class);

//        composer.add(new DUUIUIMADriver.Component(countAnnos).withScale(iScale).build());

        composer.run(processor, "reloaded");

        composer.shutdown();

    }


}
