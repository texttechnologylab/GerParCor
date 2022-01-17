package org.texttechnologylab.parliament.helper;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.xmi.XmiWriter;
import org.hucompute.textimager.uima.spacy.SpaCyMultiTagger3;

/**
 * Class for TextImagerProcessing
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class TextImagerProcessing {

    public static AggregateBuilder pipeline = new AggregateBuilder();

    private static AnalysisEngine pAE = null;

    public static void init(String sOutputPath){

        // add different Engines to the Pipeline
        try {
            pipeline.add(createEngineDescription(SpaCyMultiTagger3.class,
//                    SpaCyMultiTagger3.PARAM_REST_ENDPOINT, "http://warogoast.hucompute.org:8000"
                    SpaCyMultiTagger3.PARAM_REST_ENDPOINT, "http://huaxal.hucompute.org:8103"
            ));
            pipeline.add(createEngineDescription(XmiWriter.class,
                    XmiWriter.PARAM_TARGET_LOCATION, sOutputPath,
                    XmiWriter.PARAM_PRETTY_PRINT, true,
                    XmiWriter.PARAM_OVERWRITE, true,
                    XmiWriter.PARAM_VERSION, "1.1",
                    XmiWriter.PARAM_COMPRESSION, "GZIP"
            ));
            // create an AnalysisEngine for running the Pipeline.
            pAE = pipeline.createAggregate();
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
        }
    }

    public TextImagerProcessing(String sOutPath){

        init(sOutPath);

    }

    public JCas runPipeline(JCas pCas) throws AnalysisEngineProcessException {
        SimplePipeline.runPipeline(pCas, pAE);
        return pCas;
    }

}
