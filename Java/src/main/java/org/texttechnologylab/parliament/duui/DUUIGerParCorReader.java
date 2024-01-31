package org.texttechnologylab.parliament.duui;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import de.tudarmstadt.ukp.dkpro.core.api.io.ProgressMeter;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.bson.BsonDocument;
import org.bson.Document;
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

    //_currentMemorySize.getAndAdd(-factor * (long) file.length);

    private static AtomicInteger docNumber = new AtomicInteger();
    private long _maxItems = 0;

    private MongoCursor<Document> results = null;

    private String sQuery = "{}";

    private boolean bOverrideMeta = false;

    private int iLimit = 100;
    private int iSkip = 0;

    public DUUIGerParCorReader(MongoDBConfig dbConfig) {
        this(dbConfig, "{}");
    }

    public DUUIGerParCorReader(MongoDBConfig dbConfig, String sFilter) {
        this.dbConfig = dbConfig;
        this.sQuery = sFilter;
        System.out.println("Init connection to " + dbConfig.getMongoDatabase() + "\t" + dbConfig.getMongoCollection());
        init();
    }

    @Override
    public ProgressMeter getProgress() {
        return null;
    }

    public DUUIGerParCorReader withOverrideMeta() {
        this.bOverrideMeta = true;
        return this;
    }

    boolean getOverrideMeta() {
        return this.bOverrideMeta;
    }

    private void init() {

        this.mongoDBConnectionHandler = new MongoDBConnectionHandler(dbConfig);

        this.gridFS = GridFSBuckets.create(mongoDBConnectionHandler.getDatabase(), "grid");
        results = mongoDBConnectionHandler.getCollection().find(BsonDocument.parse(sQuery)).limit(iLimit).cursor();
        _maxItems = mongoDBConnectionHandler.getCollection().countDocuments(BsonDocument.parse(sQuery));
        progress = new ProgressMeter(_maxItems);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (docNumber.get() < _maxItems) {
                    while (results.hasNext()) {
                        if (loadedItems.size() < 10) {
                            loadedItems.add(results.next());
                        }
                        try {
                            Thread.sleep(1000l);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
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
        results = mongoDBConnectionHandler.getCollection().find(BsonDocument.parse(sQuery)).limit(iLimit).skip(++iSkip).cursor();
    }

    @Override
    public void getNextCas(JCas pCas) {

        Document pDocument = loadedItems.poll();

        if (pDocument == null) {
            pDocument = results.next();
        }

        docNumber.addAndGet(1);


        try {
            getCas(pCas, pDocument);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        System.out.printf("%s: \t %s \t %s\n", progress, formatSize(pCas.size()), pDocument.getObjectId("_id").toString());

    }

    @Override
    public boolean hasNext() {
        return results.hasNext();
    }

    @Override
    public long getSize() {
        return _maxItems;
    }

    private void getCas(JCas pEmpty, Document pDocument) throws IOException {

        String gridID = pDocument.getString(GRIDID);


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
                File tempFile = TempFileHandler.getTempFile("t"+gridID, ".xmi.gz");
                gridFS.downloadToStream(gridID, new FileOutputStream(tempFile));
                File nFile = ArchiveUtils.decompressGZ(tempFile);
                CasIOUtils.load(new FileInputStream(nFile), pEmpty.getCas());
                tempFile.delete();
                nFile.delete();
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

        } catch (Exception e) {
            e.printStackTrace();
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
