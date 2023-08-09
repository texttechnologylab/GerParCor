package org.texttechnologylab.parliament.database;

import org.bson.Document;

/**
 * Class for MongoHelper Methods
 * @author Giuseppe Abrami
 */
public class MongoHelper {


    public static final int MAX_DOCUMENT_SIZE = 1500000;


    public static Document toMongoDocument()  {

        Document mongoDocument = new Document();
        return mongoDocument;


    }


}
