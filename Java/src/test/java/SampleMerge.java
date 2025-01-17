import com.google.api.client.util.ArrayMap;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasIOUtils;
import org.bson.conversions.Bson;
import org.dkpro.core.io.xmi.XmiWriter;
import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;

import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.*;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUIAsynchronousProcessor;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategy;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategyByDelemiter;
import org.texttechnologylab.annotation.SpacyAnnotatorMetaData;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.duui.*;
import org.texttechnologylab.utilities.helper.FileUtils;
import org.texttechnologylab.utilities.uima.jcas.JCasTTLabUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class SampleMerge {

    @Test
    public void merge() throws Exception {
        ConcurrentLinkedQueue<String> sampleList = new ConcurrentLinkedQueue<>();

        Set<File> tFiles = new HashSet<>();
        tFiles = FileUtils.getFiles("/home/staff_homes/abrami/Downloads/dott/", "xmi");

        Map<Integer, Sentence> mapSentence = new ArrayMap<>();
        Map<Sentence, Integer> mapResult = new ArrayMap<>();

        String sMerge = "0_11,11_16,11_20,20_25,25_38,38_100";

        for (String s : sMerge.split(",")) {

            for(int a=0; a<4; a++){
                sampleList.add(s);
            }

        }


        tFiles.stream().forEach(f->{

            try {
                JCas pCas = JCasFactory.createJCas();
                CasIOUtils.load(new FileInputStream(f), pCas.getCas());

                JCasUtil.select(pCas.getView("GoldStandard"), Sentence.class).stream().forEach(s->{
                    int iSize = JCasUtil.selectCovered(pCas, Token.class, s).size();
                    mapSentence.put(iSize, s);
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (CASException e) {
                throw new RuntimeException(e);
            } catch (ResourceInitializationException e) {
                throw new RuntimeException(e);
            }


        });
        JCas tCas = JCasFactory.createJCas();
        JCas gold = tCas.createView("GoldStandard");
        JCas init = tCas.getView("_InitialView");

        do {
            AtomicReference<String> sValue = new AtomicReference<>(sampleList.poll());

            mapSentence.keySet().stream().sorted().forEach(ks -> {

                if(sValue.get()!=null) {

                    String[] sSplit = sValue.get().split("_");
                    int iMin = Integer.valueOf(sSplit[0]);
                    int iMax = Integer.valueOf(sSplit[1]);

                    if (ks > iMin && ks <= iMax) {
                        mapResult.put(mapSentence.get(ks), ks);
                        sValue.set(sampleList.poll());
                    }
                }
            });
        }
        while(!sampleList.isEmpty());

        System.out.println("stop");

        StringBuilder sb = new StringBuilder();
        for (Sentence s : mapResult.keySet()) {
            if(sb.length()>0){
                sb.append(" ");
            }
            sb.append(s.getCoveredText());
        }

        gold.setDocumentText(sb.toString());
        gold.setDocumentLanguage("de");

        init.setDocumentText(sb.toString());
        init.setDocumentLanguage("de");

        AtomicBoolean bFirst = new AtomicBoolean();
        bFirst.set(true);

        for (Sentence s : mapResult.keySet()) {
            int iStart = gold.getDocumentText().indexOf(s.getCoveredText());

            Sentence nSentence = new Sentence(init, iStart, iStart+s.getCoveredText().length());
            nSentence.addToIndexes();

            new Sentence(gold, iStart, iStart+s.getCoveredText().length()).addToIndexes();

            List<Annotation> tAnnotations = new ArrayList<>();
            JCasUtil.selectCovered(Annotation.class, s).stream().forEach(a->{
                tAnnotations.add(a);
            });

            tAnnotations.stream().forEach(a->{
                try {
                    JCasTTLabUtils.createAnnotation(gold, a, iStart-s.getBegin(), bFirst.get());
                    bFirst.set(false);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            for (Paragraph paragraph : JCasUtil.select(gold, Paragraph.class)) {
                paragraph.removeFromIndexes();
            }

            for (Sentence sentence : JCasUtil.select(gold, Sentence.class)) {
                new Paragraph(gold, sentence.getBegin(), sentence.getEnd()).addToIndexes();
            }

        }


        int iScale = 1;

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

        composer.add(component);


        AnalysisEngineDescription xmiEngine = createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, "/tmp/example/",
                XmiWriter.PARAM_PRETTY_PRINT, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1"
        );
//        composer.add(new DUUIUIMADriver.Component(xmiEngine).withScale(iScale).build());

        composer.add(new DUUIUIMADriver.Component(createEngineDescription(RemoveAnnotations.class, RemoveAnnotations.PARAM_Classes, Dependency.class.getName())).build());

        composer.run(tCas, "spacy");

        composer.shutdown();

        Set<TOP> dAnnotation = new HashSet<>();
        JCasUtil.select(tCas, SpacyAnnotatorMetaData.class).stream().forEach(a->{
            dAnnotation.add(a);
            if(a.getReference() instanceof Sentence){
                a.getReference().removeFromIndexes();
            }
        });
        dAnnotation.stream().forEach(a->{
            a.removeFromIndexes();
        });

//        Map<String, Sentence> delSentence = new HashMap<>(0);
//        JCasUtil.select(tCas, Sentence.class).stream().forEach(s->{
//
//            if(!delSentence.containsKey(s.getBegin()+"_"+s.getEnd())){
//                delSentence.put(s.getBegin()+"_"+s.getEnd(), s);
//            }
//            else{
//                Sentence sTest = delSentence.get(s.getBegin()+"_"+s.getEnd());
//                if(Integer.valueOf(sTest._id())<Integer.valueOf(s._id())){
//                    delSentence.put(s.getBegin()+"_"+s.getEnd(), s);
//                }
//            }
//
//        });
//
//        delSentence.values().stream().forEach(s->{
//            s.removeFromIndexes();
//        });



        CasIOUtils.save(tCas.getCas(), new FileOutputStream(new File("/tmp/EvalExtended.xmi")), SerialFormat.XMI_1_1_PRETTY);

    }
    @Test
    public void executeSpaCy() throws Exception {

        int iScale = 3;

        File pFile = new File(SampleMerge.class.getClassLoader().getResource("new_rw").getFile());

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
    public void executeSpaCyThueringen() throws Exception {

        int iScale = 10;

        File pFile = new File(SampleMerge.class.getClassLoader().getResource("new_rw").getFile());

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

        AnalysisEngineDescription writerEngine = createEngineDescription(GerParCorWriter.class,
                GerParCorWriter.PARAM_DBConnection, pFile.getAbsolutePath()
        );

        composer.add(new DUUIUIMADriver.Component(writerEngine).withScale(iScale).build());

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

        File pFile = new File(SampleMerge.class.getClassLoader().getResource("rw").getFile());

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
