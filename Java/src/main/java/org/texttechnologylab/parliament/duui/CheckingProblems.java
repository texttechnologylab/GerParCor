package org.texttechnologylab.parliament.duui;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.texttechnologylab.annotation.AnnotationComment;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CheckingProblems extends JCasFileWriter_ImplBase {

    Map<Integer, Set<String>> equalMap = new ConcurrentHashMap<>(0);
    Set<String> equalSet = new ConcurrentSkipListSet<>();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        StringBuilder sb = new StringBuilder();

        equalMap.entrySet().stream().forEach(e -> {

            e.getValue().forEach(f -> {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(e.getKey());
                sb.append("\t");
                sb.append(f);
            });

        });
        FileUtils.write(new File("/tmp/problems"), sb.toString());

        StringBuilder sb2 = new StringBuilder();

        equalSet.stream().forEach(e -> {

            if (sb2.length() > 0) {
                sb2.append("\n");
            }
            sb2.append(e);

        });

        FileUtils.write(new File("/tmp/problemsList"), sb2.toString());

    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        String sText = jCas.getDocumentText();
        DocumentMetaData dmd = DocumentMetaData.get(jCas);
        String sDocumentURI = dmd.getDocumentUri();

        String pID = "-1";
        try {
            AnnotationComment pDocumentID = JCasUtil.select(jCas, AnnotationComment.class).stream().filter(ac -> {
                return ac.getKey().equals("mongoid");
            }).findFirst().get();
            pID = pDocumentID.getValue();
        } catch (Exception e) {

        }

        int iHash = sText.hashCode();

        boolean bError = false;

        Set<String> values = new HashSet<>(0);
        if (equalMap.containsKey(iHash)) {
            System.out.println(iHash);
            values = equalMap.get(iHash);
            bError = true;
        }
        values.add(pID + "\t" + dmd.getDocumentUri());
        equalMap.put(iHash, values);

        if(bError){
            System.out.println(sDocumentURI.replace("file:", ""));
            File tFile = new File(sDocumentURI.replace("file:", ""));

            if(tFile.getName().endsWith(".xmi.gz")){
                try {
                    File tempFile = TempFileHandler.getTempFile("temp_", ".xmi");
                    ArchiveUtils.decompressGZ(Paths.get(tFile.getPath()), Paths.get(tempFile.getPath()));
                    JCas tempCas = JCasFactory.createJCas();
                    CasIOUtils.load(new FileInputStream(tempFile), tempCas.getCas());
                    String origString = tempCas.getDocumentText();
                    String dbString = jCas.getDocumentText();
                    if(!origString.equalsIgnoreCase(dbString)){
                        System.out.println(sDocumentURI);
                        equalSet.add(sDocumentURI);
                    }
                    tempFile.delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (UIMAException e) {
                    throw new RuntimeException(e);
                }
            }

        }


    }
}
