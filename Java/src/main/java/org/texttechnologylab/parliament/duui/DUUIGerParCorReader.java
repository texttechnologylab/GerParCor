package org.texttechnologylab.parliament.duui;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import de.tudarmstadt.ukp.dkpro.core.api.io.ProgressMeter;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.texttechnologylab.DockerUnifiedUIMAInterface.connection.mongodb.MongoDBConfig;
import org.texttechnologylab.DockerUnifiedUIMAInterface.connection.mongodb.MongoDBConnectionHandler;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUICollectionReader;
import org.texttechnologylab.annotation.AnnotationComment;
import org.texttechnologylab.utilities.helper.ArchiveUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reader for GerParCor
 *
 * @author Giuseppe Abrami
 */
public class DUUIGerParCorReader implements DUUICollectionReader {

    private ProgressMeter progress;
    private ConcurrentLinkedQueue<Document> loadedItems = new ConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<Document> items = new ConcurrentLinkedQueue();

    private MongoDBConfig dbConfig = null;
    private MongoDBConnectionHandler mongoDBConnectionHandler = null;

    private final String GRIDID = "grid";
    private final String DOCUMENTID = "mongoid";

    private GridFSBucket gridFS = null;

    private AtomicInteger docNumber = new AtomicInteger();
    private long _maxItems = 0;

    private List<Bson> pAggregateQuery = new ArrayList<>();

    private MongoCursor<Document> results = null;

    private boolean bFinish = false;

    private String sQuery = "{}";

    private boolean bOverrideMeta = false;

    private int iLimit = 1000;
    private AtomicInteger iSkip = new AtomicInteger(0);

    public DUUIGerParCorReader(MongoDBConfig dbConfig) {
        this(dbConfig, "{}");
    }

    public DUUIGerParCorReader(MongoDBConfig dbConfig, String sFilter) {
        this(dbConfig, sFilter, 1000);
    }

    public DUUIGerParCorReader(MongoDBConfig dbConfig, String sFilter, int iLimit) {
        this.dbConfig = dbConfig;
        this.sQuery = sFilter;
        this.iLimit = iLimit;
        init();
    }

    public DUUIGerParCorReader(MongoDBConfig dbConfig, List<Bson> pAggregateQuery) {
        this.dbConfig = dbConfig;
        this.pAggregateQuery = pAggregateQuery;
        System.out.println("Init connection to " + dbConfig.getMongoDatabase() + "\t" + dbConfig.getMongoCollection());
        init();
    }

    public DUUIGerParCorReader(MongoDBConfig dbConfig, List<Bson> pAggregateQuery, int iLimit) {
        this.dbConfig = dbConfig;
        this.iLimit = iLimit;
        this.pAggregateQuery = pAggregateQuery;
        System.out.println("Init connection to " + dbConfig.getMongoDatabase() + "\t" + dbConfig.getMongoCollection());
        init();
    }

    @Override
    public ProgressMeter getProgress() {
        return this.progress;
    }

    public DUUIGerParCorReader withOverrideMeta() {
        this.bOverrideMeta = true;
        return this;
    }

    boolean getOverrideMeta() {
        return this.bOverrideMeta;
    }

    private void performQuery(){
        if(pAggregateQuery.size()>0){
            List<Bson> tList = new ArrayList<>();
            for (Bson bson : pAggregateQuery) {
                tList.add(bson);
            }
            tList.add(Aggregates.limit(iLimit));
            tList.add(Aggregates.skip(iLimit*(iSkip.get())));
            results = mongoDBConnectionHandler.getCollection().aggregate(tList).allowDiskUse(true).cursor();
        }
        else{
            this.results = mongoDBConnectionHandler.getCollection().find(BsonDocument.parse(sQuery)).limit(iLimit).skip(iLimit * (iSkip.get())).cursor();
        }
    }

    private void init() {

        this.mongoDBConnectionHandler = new MongoDBConnectionHandler(dbConfig);

        this.gridFS = GridFSBuckets.create(mongoDBConnectionHandler.getDatabase(), "grid");
        performQuery();
        if(pAggregateQuery.size()==0) {
            _maxItems = mongoDBConnectionHandler.getCollection().countDocuments(BsonDocument.parse(sQuery));
        }
        else{
            List<Bson> tList = new ArrayList<>();
            for (Bson bson : pAggregateQuery) {
                tList.add(bson);
            }
            tList.add(Aggregates.count("sum"));
            MongoCursor<Document> pResultMax = mongoDBConnectionHandler.getCollection().aggregate(tList).allowDiskUse(true).cursor();
            _maxItems = pResultMax.next().getInteger("sum");
        }
        progress = new ProgressMeter(_maxItems);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (docNumber.get() < _maxItems || bFinish) {
                    while (results.hasNext()) {
                        if (loadedItems.size() < iLimit) {
                            loadedItems.add(results.next());
//                            System.out.println("Size: "+loadedItems.size());
                        }
                        else{
                            try {
                                Thread.sleep(1000l);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    getMoreItems();

                }
            }
        };
        Thread popThread = new Thread(r);
        popThread.start();

    }

    private void getMoreItems() {
        if(!bFinish && pAggregateQuery.size()==0) {
            System.out.println("Loaded-Items: " + loadedItems.size());
            System.out.println("Skip: " + iSkip.incrementAndGet());
//            results = mongoDBConnectionHandler.getCollection().find(BsonDocument.parse(sQuery)).limit(iLimit).skip(iLimit * (iSkip.get())).cursor();
            performQuery();
            if(!results.hasNext()){
                bFinish=true;
            }
        }
    }

    @Override
    public void getNextCas(JCas pCas) {

        pCas.reset();
        System.out.println("Loaded Items: "+loadedItems.size());
        Document pDocument = loadedItems.poll();

        if(pDocument==null){
            System.out.println("get more items!");
            getMoreItems();

            try {
                Thread.sleep(2000l);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

            if(results.hasNext()){
                pDocument = results.next();
            }
        }

        if (pDocument == null) {
            return;
        }


        try {
            getCas(pCas, pDocument);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        docNumber.addAndGet(1);


        if(JCasUtil.select(pCas, DocumentMetaData.class).size()==0){
            DocumentMetaData newMetaData = new DocumentMetaData(pCas);
            newMetaData.setDocumentUri(pDocument.getString("documentURI"));
            newMetaData.setDocumentBaseUri(pDocument.getString("documentBaseURI"));
            newMetaData.setDocumentId(pDocument.getString("documentId"));
            newMetaData.setDocumentTitle(pDocument.getString("documentId"));
            newMetaData.addToIndexes();
        }


        if (getOverrideMeta()) {
            DocumentMetaData dmd = DocumentMetaData.get(pCas);
            if (dmd == null) {
                dmd = DocumentMetaData.create(pCas);
            }
            dmd.setDocumentBaseUri("/opt/corpora/");
            dmd.setDocumentId(pDocument.getObjectId("_id").toString());
            String sPath = pDocument.containsKey("path") ? pDocument.getString("path") : "";
            dmd.setDocumentUri(dmd.getDocumentBaseUri() + (sPath.length() > 0 ? sPath + "/" : "") + pDocument.getObjectId("_id").toString());
        }

        progress.setDone(docNumber.get());
        progress.setLeft(_maxItems - docNumber.get());

        if(docNumber.get()%2==0) {
            System.out.printf("%s: \t %s \t %s\n", progress, formatSize(pCas.size()), pDocument.getObjectId("_id").toString());
        }

    }

    @Override
    public boolean hasNext() {
        return loadedItems.size()>0;
    }

    @Override
    public long getSize() {
        return _maxItems;
    }

    private void getCas(JCas pEmpty, Document pDocument) throws IOException {

        String gridID = pDocument.getString(GRIDID);

        Bson bQuery= BsonDocument.parse("{\"filename\": \""+gridID+"\"}" );

        MongoCursor<GridFSFile> mongoGrid = gridFS.find(bQuery).cursor();

        if(!mongoGrid.hasNext()){
            String sBackupValue = pDocument.getString("documentURI")+".xmi.gz";
            File checkFile = new File(sBackupValue);
            if(checkFile.exists()){
                File tempFile = TempFileHandler.getTempFile("t" + gridID, ".xmi.gz");
                File nFile = ArchiveUtils.decompressGZ(checkFile);
                CasIOUtils.load(new FileInputStream(nFile), pEmpty.getCas());
                tempFile.delete();
                nFile.delete();
            }
            else{
                System.out.println("No data can be loaded!\t"+pDocument.getObjectId("_id").toString());
            }
        }
        else {

            try (GridFSDownloadStream downloadStream = gridFS.openDownloadStream(gridID)) {
                Document pMeta = downloadStream.getGridFSFile().getMetadata();
                boolean bCompressed = true;
                if (pMeta.containsKey("compressed")) {
                    bCompressed = pMeta.getBoolean("compressed");
                } else {
                    bCompressed = false;
                }

                boolean bError = false;

                if (!bCompressed) {
                    try {
                        CasIOUtils.load(downloadStream, pEmpty.getCas());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        bError = true;
                    }
                }

                if (bError || bCompressed) {
                    File tempFile = TempFileHandler.getTempFile("t" + gridID, ".xmi.gz");
                    gridFS.downloadToStream(gridID, new FileOutputStream(tempFile));
                    File nFile = ArchiveUtils.decompressGZ(tempFile);
                    CasIOUtils.load(new FileInputStream(nFile), pEmpty.getCas());
                    tempFile.delete();
                    nFile.delete();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                AnnotationComment id = JCasUtil.select(pEmpty, AnnotationComment.class).stream().filter(ac -> ac.getKey().equals(DOCUMENTID)).findFirst().get();
                if (id != null) {
                    if (!(id.getValue().equalsIgnoreCase(pDocument.getObjectId("_id").toString()))) {
                        id.setValue(pDocument.getObjectId("_id").toString());
                        id.addToIndexes();
                    }
                }
            } catch (Exception e) {
                AnnotationComment id = new AnnotationComment(pEmpty);
                id.setKey(DOCUMENTID);
                id.setValue(pDocument.getObjectId("_id").toString());
                id.addToIndexes();
            }

            try {
                AnnotationComment id = JCasUtil.select(pEmpty, AnnotationComment.class).stream().filter(ac -> ac.getKey().equals(GRIDID)).findFirst().get();
                if (id != null) {
                    if (!(id.getValue().equalsIgnoreCase(pDocument.getString(GRIDID)))) {
                        id.setValue(pDocument.getString(GRIDID));
                        id.addToIndexes();
                    }
                }
            } catch (Exception e) {
                AnnotationComment id = new AnnotationComment(pEmpty);
                id.setKey(GRIDID);
                id.setValue(pDocument.getString(GRIDID));
                id.addToIndexes();
            }
        }

    }

    @Override
    public long getDone() {
        return docNumber.get();
    }

    public String formatSize(long lSize) {

        int u = 0;
        for (; lSize > 1024 * 1024; lSize >>= 10) {
            u++;
        }
        if (lSize > 1024)
            u++;
        return String.format("%.1f %cB", lSize / 1024f, " kMGTPE".charAt(u));

    }
}
