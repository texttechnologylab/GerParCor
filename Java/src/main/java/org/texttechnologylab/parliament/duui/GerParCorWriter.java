package org.texttechnologylab.parliament.duui;

import com.mongodb.BasicDBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasIOUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.hucompute.textimager.uima.type.GerVaderSentiment;
import org.hucompute.textimager.uima.type.Sentiment;
import org.texttechnologylab.annotation.AnnotationComment;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;

import static org.texttechnologylab.parliament.duui.MongoDBStatics.GRIDID;
import static org.texttechnologylab.parliament.duui.MongoDBStatics.iChunkSizeBytes;

/**
 * GerParCor Database Writer
 *
 * @author Giuseppe Abrami
 */
public class GerParCorWriter extends JCasFileWriter_ImplBase {

    private static Set<String> classNames = new HashSet<>(0);
    private static Set<Class> classList = new HashSet<>(0);

    public static final String PARAM_DBConnection = "dbconnection";
    private final String GRIDID = "grid";
    private final String DOCUMENTID = "mongoid";

    @ConfigurationParameter(name = PARAM_DBConnection, mandatory = true)
    protected String dbconnection;

    public static final String PARAM_compress = "sCompress";
    @ConfigurationParameter(name = PARAM_compress, mandatory = false, defaultValue = "true")
    protected String sCompress;

    private boolean bCompress = true;

    MongoDBConnectionHandler dbConnectionHandler = null;
    GridFSBucket gridFS = null;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        MongoDBConfig dbConfig = null;
        try {
            dbConfig = new MongoDBConfig(dbconnection);
            dbConnectionHandler = new MongoDBConnectionHandler(dbConfig);
            this.gridFS = GridFSBuckets.create(dbConnectionHandler.getDatabase(), "grid");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (sCompress.equalsIgnoreCase("false")) {
            bCompress = false;
        }

        classList.add(Sentence.class);
        classList.add(Token.class);
        classList.add(NamedEntity.class);
        classList.add(Dependency.class);
        classList.add(Lemma.class);
        classList.add(GerVaderSentiment.class);

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }


    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        String sGridId = "";
        String sDocumentId = "";

        try {
            AnnotationComment pGridID = JCasUtil.select(aJCas, AnnotationComment.class).stream().filter(ac -> {
                return ac.getKey().equals(GRIDID);
            }).findFirst().get();
            AnnotationComment pDocumentID = JCasUtil.select(aJCas, AnnotationComment.class).stream().filter(ac -> {
                return ac.getKey().equals(DOCUMENTID);
            }).findFirst().get();

            if (pGridID != null) {
                sGridId = pGridID.getValue();
            }
            if (pDocumentID != null) {
                sDocumentId = pDocumentID.getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (sGridId.length() > 0) {

            Bson bQuery= BsonDocument.parse("{\"filename\": \""+sGridId+"\"}" );

            for (GridFSFile gridFSFile : gridFS.find(bQuery)) {
                System.out.println("Remove: "+gridFSFile.getObjectId());
                gridFS.delete(gridFSFile.getObjectId());
            }

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(iChunkSizeBytes)
                    .metadata(new Document("compressed", bCompress))
                    .metadata(new Document("type", "uima"))
                    .metadata(new Document(GRIDID, sGridId));

            GridFSUploadStream uploadStream = gridFS.openUploadStream(sGridId, options);
            try {

                if (bCompress) {
                    File pTempFile = TempFileHandler.getTempFile("aaa", ".xmi");
                    CasIOUtils.save(aJCas.getCas(), new FileOutputStream(pTempFile), SerialFormat.XMI_1_1);
                    File compressedFile = ArchiveUtils.compressGZ(pTempFile);
                    byte[] data = Files.readAllBytes(compressedFile.toPath());
                    uploadStream.write(data);
                    uploadStream.flush();
                    pTempFile.delete();
                    compressedFile.delete();
                } else {
                    CasIOUtils.save(aJCas.getCas(), uploadStream, SerialFormat.XMI_1_1);
                }
                uploadStream.flush();
                uploadStream.close();

                Document pDocument = this.dbConnectionHandler.getObject(sDocumentId);
//                pDocument.put("annotations", countAnnotations(aJCas));

                Document pAnnotations = new Document();
                for (Class aClass : classList) {
                    pAnnotations.put(aClass.getSimpleName(), countAnnotations(aJCas, aClass));
                }
                pDocument.put("annotations", pAnnotations);

                if(JCasUtil.select(aJCas, Sentiment.class).size()>0){
                    try {
                        pDocument.put("sentiment_value", valueSentiment(aJCas).stream().findFirst().get());
                    }
                    catch (Exception e){

                    }
                }


                Document pMeta = pDocument.get("meta", Document.class);
                if (pMeta == null) {
                    pMeta = new Document();
                }
                Document nMeta = getMetaInformation(aJCas);
                for (String s : nMeta.keySet()) {
                    pMeta.put(s, nMeta.get(s));
                }
                pDocument.put("meta", pMeta);

                BasicDBObject whereQuery = new BasicDBObject();
                whereQuery.put("_id", new ObjectId(sDocumentId));

                this.dbConnectionHandler.getCollection().replaceOne(whereQuery, pDocument);
//                this.dbConnectionHandler.updateObject(sDocumentId, pDocument);
                System.out.println("Write: "+sDocumentId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private int countAnnotations(JCas pCas, Class pType){

        int iResult = 0;
        iResult = JCasUtil.select(pCas, pType).size();
        return iResult;

    }

    /**
     * Count Annotations in JCas
     * @param pCas
     * @return
     */
    private Document countAnnotations(JCas pCas) {


        Document rDocument = new Document();

        try {

            if (classNames.size() > 0) {
                for (String className : classNames) {
                    try {
                        Class pClass = Class.forName(className);
                        rDocument.put(pClass.getSimpleName(), JCasUtil.select(pCas, pClass).size());
                    } catch (ClassNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } else {

                for (Annotation annotation : JCasUtil.select(pCas, Annotation.class)) {
                    if (!classNames.contains(annotation.getType().getName())) {
                        rDocument.put(annotation.getType().getShortName(), JCasUtil.select(pCas, annotation.getClass()).size());
                        classNames.add(annotation.getType().getName());

                        if (annotation.getType().getClass().equals(Sentiment.class)) {
                            Double dSentiment = JCasUtil.select(pCas, Sentiment.class).stream().flatMapToDouble(s -> DoubleStream.of(s.getSentiment())).sum();
                            rDocument.put(Sentiment.class.getSimpleName(), dSentiment);
                        }

                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return rDocument;

    }
    private List<Document> valueSentiment(JCas pCas) {

        List<Document> documentList = new ArrayList<>();
        JCasUtil.select(pCas, Sentiment.class).stream().forEach(s->{
            Document nDocument = new Document();
            nDocument.put("begin", s.getBegin());
            nDocument.put("end", s.getEnd());
            nDocument.put("value", s.getSentiment());
            documentList.add(nDocument);
        });

        return documentList;
    }

    /**
     * Get Meta-Informations
     * @param pCas
     * @return
     */
    private Document getMetaInformation(JCas pCas) {

        Document rDocument = new Document();

        return rDocument;

    }
}
