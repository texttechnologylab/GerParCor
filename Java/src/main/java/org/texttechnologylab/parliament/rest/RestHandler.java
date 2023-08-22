package org.texttechnologylab.parliament.rest;

import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

public class RestHandler {

    private MongoDBConnectionHandler dbConnection = null;

    public RestHandler(MongoDBConnectionHandler dbConnection){
        this.dbConnection = dbConnection;
    }

    public static void init(){

    }



}
