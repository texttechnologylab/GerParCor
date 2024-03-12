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
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
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
import org.texttechnologylab.utilities.uima.jcas.JCasTTLabUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static org.texttechnologylab.parliament.duui.MongoDBStatics.iChunkSizeBytes;

/**
 * GerParCor SampleWriter
 *
 * @author Giuseppe Abrami
 */
public class SampleWriter extends JCasFileWriter_ImplBase {

    private ConcurrentLinkedQueue<String> sampleList = new ConcurrentLinkedQueue<>();

    private JCas emptyCas = null;

    private List<TOP> annotationList = new ArrayList<>(0);

    StringBuilder sb = new StringBuilder(0);

    public static final String PRAM_sample = "paramSample";
    @ConfigurationParameter(name = PRAM_sample, mandatory = false, defaultValue = "5_10")
    protected String paramSample;

    public static final String PRAM_amount = "paramAmount";
    @ConfigurationParameter(name = PRAM_amount, mandatory = false, defaultValue = "5")
    protected String paramAmount;


    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        String[] sSample = paramSample.split(",");

        for (String s : sSample) {
            for(int a=0; a<=Integer.valueOf(paramAmount); a++){
                sampleList.add(s);
            }
        }

        try {
            emptyCas = JCasFactory.createJCas();
        } catch (CASException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        emptyCas.setDocumentText(sb.toString());
        emptyCas.setDocumentLanguage("de");

        JCasUtil.select(emptyCas, Sentence.class).stream().forEach(s->{
            System.out.println(JCasUtil.selectCovered(Token.class, s).size()+"\t"+s.getCoveredText());
        });

        CasIOUtils.save(emptyCas.getCas(), new FileOutputStream(new File("/tmp/SampleNew.xmi")), SerialFormat.XMI_1_1);

    }


    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        boolean bFound = false;

        while(!bFound) {

            String sTest = sampleList.poll();
            int iBegin = Integer.parseInt(sTest.split("_")[0]);
            int iEnd = Integer.parseInt(sTest.split("_")[1]);

            List<Sentence> pSentence = JCasUtil.select(aJCas, Sentence.class).stream().filter(s -> {
                int iSize = JCasUtil.selectCovered(Token.class, s).size();
                if(iSize<4){
                    return false;
                }
                return iSize > iBegin && iSize < iEnd;
            }).sorted((s1, s2)->{
                return Integer.valueOf(s1.getCoveredText().length()).compareTo(s2.getCoveredText().length())*-1;
            }).collect(Collectors.toList());


            if (pSentence.isEmpty()) {
                String sBackup = sTest;
                sampleList.add(sTest);
                bFound=true;
            }
            else{
                bFound=true;

                Sentence pValue = pSentence.stream().findFirst().get();
                int sBegin = pValue.getBegin();
                int sEnd = pValue.getEnd();

                if(sb.length()>0){
                    sb.append("\n");
                }

                String sText = replaceNonPrintable(pValue.getCoveredText());

                sb.append(sText);

                String checkString = sb.toString();
                int newBegin = checkString.indexOf(sText);
                int newEnd = newBegin+sText.length();

                Sentence newSentence = new Sentence(emptyCas, newBegin, newEnd);
                newSentence.addToIndexes();

            }
        }

        if(sampleList.isEmpty()){
            try {
                finalize();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static String replaceNonPrintable(String sInput){
        return sInput.replaceAll("\\x00", "").replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD]", "");
    }


}
