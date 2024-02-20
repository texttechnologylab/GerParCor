package org.texttechnologylab.parliament.duui;

import com.google.gson.Gson;
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
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CSVExport extends JCasFileWriter_ImplBase {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        DocumentAnnotation da = JCasUtil.selectSingle(jCas, DocumentAnnotation.class);
        DocumentMetaData dmd = JCasUtil.selectSingle(jCas, DocumentMetaData.class);
        if(da!=null){
            StringBuilder sb = new StringBuilder();

            try {
                String sFile = sdf.format(new Date(da.getTimestamp()));
                NamedOutputStream outputStream = getOutputStream(dmd.getDocumentId(), ".csv");
                NamedOutputStream outputStreamPlain = getOutputStream(dmd.getDocumentId(), ".txt");

                JCasUtil.select(jCas, Sentence.class).stream().forEach(s->{

                    if(sb.length()>0){
                        sb.append("\n");
                    }
                    sb.append(s.getCoveredText());
                    sb.append("\t");
                    sb.append(sFile);
                    sb.append("\t");
                    sb.append(s.getBegin());
                    sb.append("\t");
                    sb.append(s.getEnd());

                });

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStreamPlain, StandardCharsets.UTF_8))) {
                    writer.write(jCas.getDocumentText());
                }

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
                    writer.write(sb.toString());
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
