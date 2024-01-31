package org.texttechnologylab.parliament.duui;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
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
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasIOUtils;
import org.bson.Document;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.texttechnologylab.annotation.AnnotationComment;
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.StringUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import static org.texttechnologylab.parliament.duui.MongoDBStatics.GRIDID;

public class MongoDBImporter extends JCasFileWriter_ImplBase {

    public static final String PARAM_DBConnection = "dbconnection";
    @ConfigurationParameter(name = PARAM_DBConnection, mandatory = true)
    protected String dbconnection;


    public static final String PARAM_Parliament = "parliament";
    @ConfigurationParameter(name = PARAM_Parliament, mandatory = true)
    protected String parliament;

    public static final String PARAM_Country = "country";
    @ConfigurationParameter(name = PARAM_Country, mandatory = true)
    protected String country;

    public static final String PARAM_Subpath = "subpath";
    @ConfigurationParameter(name = PARAM_Subpath, mandatory = false, defaultValue = "")
    protected String subpath;

    public static final String PARAM_Comment = "comment";
    @ConfigurationParameter(name = PARAM_Comment, mandatory = false, defaultValue = "")
    protected String comment;

    public static final String PARAM_Devision = "devision";
    @ConfigurationParameter(name = PARAM_Devision, mandatory = true)
    protected String devision;

    public static final String PARAM_Historical = "historical";
    @ConfigurationParameter(name = PARAM_Historical, mandatory = true)
    protected String historical;


    MongoDBConnectionHandler dbConnectionHandler = null;
    GridFSBucket gridFS = null;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        MongoDBConfig dbConfig = null;
        try {
            dbConfig = new MongoDBConfig(dbconnection);
            dbConnectionHandler = new MongoDBConnectionHandler(dbConfig);
            this.gridFS = GridFSBuckets.create(dbConnectionHandler.getDatabase(), "grid");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        try {
            importJCas(aJCas);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public void importJCas(JCas pCas) throws IOException, NoSuchAlgorithmException {

        boolean bCompress = true;

        String sGridId = "";

        try {
            AnnotationComment pGridID = JCasUtil.select(pCas, AnnotationComment.class).stream().filter(ac -> {
                return ac.getKey().equals(GRIDID);
            }).findFirst().get();
            if(pGridID!=null){
                sGridId = pGridID.getValue();
            }
        }
        catch (Exception e){
//            System.out.println(e.getMessage());
            String sHash = StringUtils.toMD5(pCas.getDocumentText());
            sGridId = sHash;
            AnnotationComment pGridID = new AnnotationComment(pCas);
            pGridID.setKey(GRIDID);
            pGridID.setValue(sGridId);
            pGridID.addToIndexes();
        }



        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(358400)
                .metadata(new Document("type", "uima"))
                .metadata(new Document("compressed", bCompress))
                .metadata(new Document(GRIDID, sGridId));

        GridFSUploadStream uploadStream = gridFS.openUploadStream(sGridId, options);
        try {

            if (bCompress) {
                File pTempFile = TempFileHandler.getTempFile("aaa", ".xmi");
                CasIOUtils.save(pCas.getCas(), new FileOutputStream(pTempFile), SerialFormat.XMI_1_1);
                File compressedFile = ArchiveUtils.compressGZ(pTempFile);
                byte[] data = Files.readAllBytes(compressedFile.toPath());
                uploadStream.write(data);
                uploadStream.flush();
                pTempFile.delete();
                compressedFile.delete();
            } else {
                CasIOUtils.save(pCas.getCas(), uploadStream, SerialFormat.XMI_1_1);
            }
            uploadStream.flush();
            uploadStream.close();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String finalSGridId = sGridId;

        Set<Class> whiteList = new HashSet<>();
        whiteList.add(Sentence.class);
        whiteList.add(Token.class);
        whiteList.add(NamedEntity.class);
        whiteList.add(Dependency.class);
        whiteList.add(Lemma.class);

        JCasUtil.select(pCas, DocumentAnnotation.class).stream().forEach(a->{
            DocumentMetaData dmd = DocumentMetaData.get(pCas);

            Document nDocument = new Document();
            nDocument.put("id", dmd.getDocumentId());
            nDocument.put("documentURI", dmd.getDocumentUri());
            nDocument.put("documentId", dmd.getDocumentId());
            nDocument.put("documentBaseURI", dmd.getDocumentBaseUri());
            nDocument.put("hash", finalSGridId);
            nDocument.put("name", sdf.format(a.getTimestamp()));

            Document pMeta = new Document();
            pMeta.put("country", country);
            pMeta.put("parliament", parliament);
            if(subpath.length()>0) { pMeta.put("subpath", subpath); }
            if(comment.length()>0) { pMeta.put("comment", comment); }
            pMeta.put("historical", historical.equalsIgnoreCase("true") ? true : false);
            pMeta.put("devision", devision);

            nDocument.put("meta", pMeta);

            Document pAnnotations = new Document();
            for (Class aClass : whiteList) {
                pAnnotations.put(aClass.getSimpleName(), countAnnotations(pCas, aClass));
            }
            nDocument.put("annotations", pAnnotations);

            try{
                nDocument.put("nameBackup", dmd.getDocumentTitle());
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
            nDocument.put("timestamp", a.getTimestamp());
            nDocument.put("year", a.getDateYear());
            nDocument.put("month", a.getDateMonth());
            nDocument.put("day", a.getDateDay());
            nDocument.put("grid", finalSGridId);

            dbConnectionHandler.getCollection().insertOne(nDocument);

        });


        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private int countAnnotations(JCas pCas, Class pType){

        int iResult = 0;
            iResult = JCasUtil.select(pCas, pType).size();
        return iResult;

    }

}
