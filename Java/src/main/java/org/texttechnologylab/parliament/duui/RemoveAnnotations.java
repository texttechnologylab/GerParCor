package org.texttechnologylab.parliament.duui;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.texttechnologylab.annotation.AnnotatorMetaData;

import java.util.HashSet;
import java.util.Set;

public class RemoveAnnotations extends JCasAnnotator_ImplBase {

    public static final String PARAM_Classes = "removeClasses";
    @ConfigurationParameter(name = PARAM_Classes, mandatory = false)
    protected String removeClasses;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        Set<TOP> tAnnotation = new HashSet<>();

        if(removeClasses.length()==0){

            JCasUtil.select(jCas, Annotation.class).forEach(a->{
                if(!(a instanceof DocumentMetaData)) {
                    tAnnotation.add(a);
                }
            });

        }
        else{
            for (String s : removeClasses.split(",")) {
                try {
                    Class pClass = Class.forName(s);
                    JCasUtil.select(jCas, pClass).stream().forEach(a->{
                        tAnnotation.add((TOP) a);
                    });
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        tAnnotation.stream().forEach(a->{
            a.removeFromIndexes();
        });



    }
}
