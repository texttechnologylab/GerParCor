package org.texttechnologylab.parliament.duui;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.uima.type.CategorizedSentiment;

import java.util.HashMap;
import java.util.Map;

public class CountAnnotations extends JCasFileWriter_ImplBase {

    static Map<String, Double> annoCount = new HashMap();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        annoCount.clear();
        annoCount.put("pos", 0.0d);
        annoCount.put("neg", 0.0d);
        annoCount.put("neu", 0.0d);
    }

    @Override
    public void destroy() {

        System.out.println("Annotations: ");
            annoCount.keySet().stream().sorted().forEach(k->{
                System.out.println(k+"\t"+annoCount.get(k));
            });

            super.destroy();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {


        JCasUtil.select(jCas, CategorizedSentiment.class).forEach(a->{
            System.out.println(a.getBegin()+"\t"+a.getEnd()+"\t"+a.getSentiment()+"\t"+a.getPos()+"\t"+a.getNeu()+"\t"+a.getNeg());
            annoCount.put("pos", annoCount.get("pos")+a.getPos());
            annoCount.put("neg", annoCount.get("neg")+a.getPos());
            annoCount.put("neu", annoCount.get("neu")+a.getPos());
        });




    }
}
