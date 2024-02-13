package org.texttechnologylab.parliament.duui;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.texttechnologylab.annotation.AnnotationComment;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CheckingDouble extends JCasFileWriter_ImplBase {

    Map<String, Integer> doubleSet = new ConcurrentHashMap<>(0);

    @Override
    protected void finalize() throws Throwable {
        super.finalize();



    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        Set<String> sentenceCheck = new HashSet<>(0);
        DocumentMetaData dmd = DocumentMetaData.get(jCas);
        JCasUtil.select(jCas, Sentence.class).stream().forEach(s->{

            String sValue = s.getBegin()+"_"+s.getEnd();
            if(!sentenceCheck.contains(sValue)){
                sentenceCheck.add(sValue);
            }
            else{

                int iValue = 0;
                if(doubleSet.containsKey(dmd.getDocumentUri())){
                    iValue = doubleSet.get(dmd.getDocumentUri());
                }
                iValue++;
                doubleSet.put(dmd.getDocumentUri(), iValue);
                try {
                    FileUtils.write(new File("/tmp/problems"), dmd.getDocumentUri()+"\n", true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        StringBuilder sb = new StringBuilder();

        doubleSet.entrySet().stream().sorted((e1, e2)->{
            return e1.getValue().compareTo(e2.getValue());
        }).forEach(e->{
            if(sb.length()>0){
                sb.append("\n");
            }
            sb.append(e.getValue()+"\t"+e.getKey());
        });

        try {
            FileUtils.write(new File("/tmp/problemsFinal"), sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
