package org.texttechnologylab.parliament.duui;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

public class SetLanguage extends JCasAnnotator_ImplBase {

    public static final String PARAM_LANGUAGE = "language";

    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    protected String language;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        jCas.setDocumentLanguage(language);
        System.out.println(jCas.getDocumentLanguage());

    }
}
