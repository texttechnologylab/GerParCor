package org.texttechnologylab.parliament.data.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.bson.Document;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.Protocol;

import java.io.File;
import java.io.IOException;
import java.sql.Date;

public class Protocol_Impl implements Protocol {

    ParliamentFactory pFactory = null;
    Document pDocument = null;

    public Protocol_Impl(ParliamentFactory pFactory, String sID){
        this.pFactory = pFactory;
        this.pDocument = pFactory.getDatabaseHandler().getObject(sID);
    }

    public Protocol_Impl(ParliamentFactory pFactory, Document pDocument){
        this.pFactory = pFactory;
        this.pDocument = pDocument;
    }

    @Override
    public String getID() {
        return pDocument.getString("_id");
    }

    @Override
    public Date getDate() {
        Date pDate = new Date(pDocument.getLong("timestamp"));
        return pDate;
    }

    @Override
    public void setDate(Date pDate) {
        pDocument.put("timestamp", pDate.getTime());

    }


    @Override
    public String getTitle() {
        return pDocument.getString("name");
    }

    @Override
    public void setTitle(String sValue) {
        this.pDocument.put("name", sValue);
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
        return pDocument.get("meta", Document.class).getString("parliament");
    }

    @Override
    public void setParliament(String sValue) {
        pDocument.get("meta", Document.class).put("parliament", sValue);
    }

    @Override
    public String getDevision() {
        return pDocument.get("meta", Document.class).getString("devision");

    }

    @Override
    public void setDevision(String sValue) {
        pDocument.get("meta", Document.class).put("devision", sValue);
    }

    @Override
    public String getCountry() {
        return pDocument.get("meta", Document.class).getString("country");
    }

    @Override
    public void setCountry(String sValue) {
        pDocument.get("meta", Document.class).put("country", sValue);

    }

    @Override
    public String getSubPath() {
        return pDocument.get("meta", Document.class).getString("subpath");

    }

    @Override
    public void setSubPath(String sValue) {
        pDocument.get("meta", Document.class).put("subpath", sValue);
    }

    @Override
    public boolean isHistoric() {
        return pDocument.get("meta", Document.class).getBoolean("historical");
    }

    @Override
    public void setHistoric(boolean bValue) {
        pDocument.get("meta", Document.class).put("historical", bValue);
    }

    @Override
    public String getUIMADocumentURI() {
        return pDocument.getString("documentURI");
    }

    @Override
    public String getUIMADocumentBaseURI() {
        return pDocument.getString("documentBaseURI");
    }

    @Override
    public String getUIMADocumentID() {
        return pDocument.getString("documentId");
    }

    @Override
    public String getName() {
        return pDocument.getString("name");
    }

    @Override
    public File getDocumentAsFile() {
        return null;
    }

    public void update(){

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", this.getID());
        this.pFactory.getDatabaseHandler().getCollection().updateOne(whereQuery, this.pDocument);
    }

    @Override
    public JCas getDocumentAsJCas() throws UIMAException {

        JCas pCas = JCasFactory.createJCas();

        String gridID = pDocument.getString("grid");
        GridFSBucket gridFS = GridFSBuckets.create(pFactory.getDatabaseHandler().getDatabase(), "grid");
        try (GridFSDownloadStream downloadStream = gridFS.openDownloadStream(gridID)) {
            CasIOUtils.load(downloadStream, pCas.getCas());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return pCas;
    }

    @Override
    public int compareTo(Protocol protocol) {
        return this.getID().compareTo(protocol.getID());
    }

    @Override
    public String toString() {
        return this.getCountry()+"\t"+this.getID()+"\t"+this.getName();
    }
}
