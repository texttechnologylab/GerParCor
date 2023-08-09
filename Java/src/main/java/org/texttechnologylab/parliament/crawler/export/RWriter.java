package org.texttechnologylab.parliament.crawler.export;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.io.xmi.XmiReader;
import org.junit.Test;
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class RWriter extends JCasFileWriter_ImplBase {

    public static final String PARAM_OUTPUT = "output";
    @ConfigurationParameter(name = PARAM_OUTPUT, mandatory = false, defaultValue = "/tmp/")
    protected String output;

    StringBuilder sb = new StringBuilder();

    AtomicInteger iCounter = new AtomicInteger(0);

    @Override
    public void destroy() {

        try {
            FileUtils.writeContent(sb.toString(), new File(output));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.destroy();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        DocumentAnnotation da = JCasUtil.select(aJCas, DocumentAnnotation.class).stream().findFirst().get();
        DocumentMetaData metaData = DocumentMetaData.get(aJCas);

        for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {

            if(sb.length()>0){
                sb.append("\n");
            }
            else{
                sb.append("id");
                sb.append("\t");
                sb.append("year");
                sb.append("\t");
                sb.append("date");
                sb.append("\t");
                sb.append("number");
                sb.append("\t");
                sb.append("title");
                sb.append("\t");
                sb.append("subtitle");
                sb.append("\t");
                sb.append("timestamp");
                sb.append("\t");
                sb.append("begin");
                sb.append("\t");
                sb.append("end");
                sb.append("\t");
                sb.append("sentence");
                sb.append("\n");
            }

            sb.append(iCounter.getAndIncrement());
            sb.append("\t");
            sb.append(da.getDateYear());
            sb.append("\t");
            sb.append(da.getDateYear()+"-"+da.getDateMonth()+"-"+da.getDateDay());
            sb.append("\t");
            sb.append(metaData.getDocumentId().substring(0, metaData.getDocumentId().indexOf(".")));
            sb.append("\t");
            sb.append(metaData.getDocumentTitle());
            sb.append("\t");
            sb.append(da.getSubtitle());
            sb.append("\t");
            sb.append(da.getTimestamp());
            sb.append("\t");
            sb.append(sentence.getBegin());
            sb.append("\t");
            sb.append(sentence.getEnd());
            sb.append("\t");
            sb.append(sentence.getCoveredText().replaceAll("\n", " "));

        }

    }

    @Test
    public void testHessen() throws UIMAException, IOException {

        AggregateBuilder pipeline = new AggregateBuilder();

        CollectionReaderDescription reader = createReaderDescription(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, "/resources/public/abrami/GerParCor/1"+"/**"+".xmi.gz",
                XmiReader.PARAM_SORT_BY_SIZE, true);

        pipeline.add(createEngineDescription(RWriter.class,
                RWriter.PARAM_OUTPUT, "/tmp/hessen.csv")
                );

        SimplePipeline.runPipeline(reader, pipeline.createAggregateDescription());


    }
}
