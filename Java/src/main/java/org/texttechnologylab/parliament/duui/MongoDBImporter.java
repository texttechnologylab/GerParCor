package org.texttechnologylab.parliament.duui;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.io.IOException;

public class MongoDBImporter extends JCasFileWriter_ImplBase {

    public static final String PARAM_DBConnection = "dbconnection";
    @ConfigurationParameter(name = PARAM_DBConnection, mandatory = true)
    protected String dbconnection;

    MongoDBConnectionHandler dbConnectionHandler = null;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        MongoDBConfig dbConfig = null;
        try {
            dbConfig = new MongoDBConfig(dbconnection);
            dbConnectionHandler = new MongoDBConnectionHandler(dbConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        System.out.println(aJCas);
        System.out.println("Annotations: "+JCasUtil.selectAll(aJCas).size());
        JCasUtil.select(aJCas, DocumentAnnotation.class).stream().forEach(a->{
            System.out.println(a.getAuthor());
            System.out.println(a.getSubtitle());
            System.out.println(a.getPlace());
            System.out.println(a.getTimestamp());
            DocumentMetaData dmd = DocumentMetaData.get(aJCas);
            System.out.println(dmd.getDocumentUri());
            System.out.println(dmd.getDocumentId());
            System.out.println(dmd.getDocumentTitle());
        });



    }


}
