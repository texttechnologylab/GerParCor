package org.texttechnologylab.parliament.data;

import org.apache.uima.jcas.JCas;

import java.io.File;
import java.sql.Date;

public interface Protocol extends Comparable<Protocol> {

    String getID();

    Date getStartDate();
    void setStartDate(Date pDate);

    Date getDate();
    void setDate(Date pDate);

    Date getEndDate();
    void setEndDate(Date pDate);

    String getTitle();
    void setTitle(String sValue);

    String getSubtitle();
    void setSubtitle(String sValue);

    String getContent();

    String getLanguage();

    String getParliament();
    void setParliament(String sValue);

    File getDocumentAsFile();

    JCas getDocumentAsJCas();
}
