package org.texttechnologylab.parliament.data;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;

public interface Protocol extends Comparable<Protocol> {

    String getID();

    Date getDate();
    void setDate(Date pDate);


    String getTitle();
    void setTitle(String sValue);

    String getSubtitle();
    void setSubtitle(String sValue);

    String getContent();

    String getLanguage();

    String getParliament();
    void setParliament(String sValue);

    String getDevision();
    void setDevision(String sValue);

    String getCountry();
    void setCountry(String sValue);

    String getSubPath();
    void setSubPath(String sValue);

    boolean isHistoric();
    void setHistoric(boolean bValue);

    String getUIMADocumentURI();
    String getUIMADocumentBaseURI();
    String getUIMADocumentID();
    String getName();

    void getDocumentAsFile(OutputStream pOutputStream) throws UIMAException, IOException;

    File getDocumentAsFile() throws UIMAException, IOException;

    JCas getDocumentAsJCas() throws UIMAException;

    JSONObject toJSON();
}
