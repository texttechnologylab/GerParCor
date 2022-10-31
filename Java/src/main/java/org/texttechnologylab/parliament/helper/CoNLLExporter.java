package org.texttechnologylab.parliament.helper;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.io.conll.Conll2000Writer;
import org.dkpro.core.io.xmi.XmiReader;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Class to convert XMI to CoNLL
 * @author Giuseppe Abrami
 */
public class CoNLLExporter {

    /**
     * You need two arguments:
     *  0 : Path to the input files
     *  1 : Path to the target location
     * @param args
     * @throws UIMAException
     * @throws IOException
     */
    public static void main(String[] args) throws UIMAException, IOException {

        CollectionReaderDescription xmiReader = CollectionReaderFactory.createReaderDescription(
                XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, args[0],
                XmiReader.PARAM_PATTERNS, "**/*.xmi.gz"
        );

        AnalysisEngineDescription conllWriter = createEngineDescription(Conll2000Writer.class,
                Conll2000Writer.PARAM_TARGET_LOCATION, args[1],
                Conll2000Writer.PARAM_WRITE_POS, true,
                Conll2000Writer.PARAM_WRITE_CHUNK, true,
                Conll2000Writer.PARAM_WRITE_COVERED_TEXT, true
        );

        SimplePipeline.runPipeline(xmiReader, conllWriter);


    }

}
