package org.texttechnologylab.parliament.data;

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

}
