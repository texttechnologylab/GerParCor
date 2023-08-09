package org.texttechnologylab.parliament.data.impl;

import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.Protocol;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.sql.Date;
import java.util.Set;

public class ParliamentFactory_Impl implements ParliamentFactory {

    private MongoDBConnectionHandler pDBHandler = null;

    public ParliamentFactory_Impl(MongoDBConnectionHandler dbConnectionHandler){
        pDBHandler = dbConnectionHandler;
    }

    @Override
    public MongoDBConnectionHandler getDatabaseHandler() {
        return pDBHandler;
    }

    @Override
    public Set<Protocol> listProtocols() {
        return null;
    }

    @Override
    public Set<Protocol> listProtocols(String sParliament) {
        return null;
    }

    @Override
    public Set<Protocol> listProtocols(String sParliament, Date pStartDate, Date pEndDate) {
        return null;
    }
}
