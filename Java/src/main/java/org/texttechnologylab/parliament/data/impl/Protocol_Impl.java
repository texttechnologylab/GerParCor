package org.texttechnologylab.parliament.data.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.apache.tools.ant.taskdefs.TempFile;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.Protocol;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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

    public Protocol_Impl(Document pDocument){
        this.pDocument = pDocument;
    }

    @Override
    public String getID() {
        return pDocument.getObjectId("_id").toString();
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
        return pDocument.getString("nameBackup");
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
    public File getDocumentAsFile() throws UIMAException, IOException {
        File pFile = TempFileHandler.getTempFile("aaa", ".xmi");
            getDocumentAsFile(new FileOutputStream(pFile));
        return pFile;
    }

    @Override
    public void getDocumentAsFile(OutputStream pOutputStream) throws UIMAException, IOException {
        JCas pCas = JCasFactory.createJCas();

        String gridID = pDocument.getString("grid");
        GridFSBucket gridFS = GridFSBuckets.create(pFactory.getDatabaseHandler().getDatabase(), "grid");

        try (GridFSDownloadStream downloadStream = gridFS.openDownloadStream(gridID)) {
            CasIOUtils.load(downloadStream, pCas.getCas());
            CasIOUtils.save(pCas.getCas(), pOutputStream, SerialFormat.XMI_1_1);
        } catch (IOException e) {
            File tFile = TempFileHandler.getTempFile("aaa", ".xmi.gz");
            tFile.deleteOnExit();
            gridFS.downloadToStream(gridID, new FileOutputStream(tFile));
            File nFile = ArchiveUtils.decompressGZ(tFile);
            nFile.deleteOnExit();
            Files.copy(nFile.toPath(), pOutputStream);



        }

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
    public JSONObject toJSON() {
        JSONObject rObject = new JSONObject();

            rObject.put("id", getID());
            rObject.put("name", getName());
            rObject.put("parliament", getParliament());
            rObject.put("devision", getDevision());
            rObject.put("history", isHistoric());
            rObject.put("country", getCountry());

        return rObject;
    }

    @Override
    public Double getSentiment() {
        return pDocument.get("sentiment_value", Document.class).getDouble("value");
    }

    public Document getInformation(String sValue){
        return pDocument.get(sValue, Document.class);
    }

    @Override
    public Integer getToken() {
        return getInformation("annotations").getInteger("Token");
    }

    @Override
    public Integer getSentence() {
        return getInformation("annotations").getInteger("Sentence");
    }

    @Override
    public Integer getLemma() {
        return getInformation("annotations").getInteger("Lemma");
    }

    @Override
    public Integer getDependency() {
        return getInformation("annotations").getInteger("Dependency");
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
