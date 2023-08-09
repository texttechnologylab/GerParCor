package org.texttechnologylab.parliament.data.impl;

import org.bson.Document;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.Protocol;

import java.sql.Date;

public class Protocol_Impl implements Protocol {

    ParliamentFactory pFactory = null;
    Document pDocument = null;

    public Protocol_Impl(ParliamentFactory pFactory, String sID){
        pFactory = pFactory;
        pDocument = pFactory.getDatabaseHandler().getObject(sID);
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public void setDate(Date pDate) {

    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String sValue) {

    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public void setSubtitle(String sValue) {

    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public String getParliament() {
        return null;
    }

    @Override
    public void setParliament(String sValue) {

    }

    @Override
    public int compareTo(Protocol protocol) {
        return this.getID().compareTo(protocol.getID());
    }

}
