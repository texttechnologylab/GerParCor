import org.apache.uima.UIMAException;
import org.apache.uima.util.CasIOUtils;
import org.texttechnologylab.parliament.GerParCor;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.impl.ParliamentFactory_Impl;
import org.texttechnologylab.parliament.database.MongoDBConfig;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.io.File;
import java.io.IOException;

public class Test {

    @org.junit.Test
    public void getDocument() {

        String sDBConfig = GerParCor.class.getClassLoader().getResource("rw").getPath();

        try {
            MongoDBConfig dbConfig = new MongoDBConfig(sDBConfig);
            MongoDBConnectionHandler pHandler = new MongoDBConnectionHandler(dbConfig);

            ParliamentFactory pFactory = new ParliamentFactory_Impl(pHandler);

            File rFile = pFactory.getProtocol("653113e369a2ca2861f90435").getDocumentAsFile();

            System.out.println(rFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UIMAException e) {
            throw new RuntimeException(e);
        }


    }
}
