package org.texttechnologylab.parliament.data;

import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.sql.Date;
import java.util.Set;

public interface ParliamentFactory {

    MongoDBConnectionHandler getDatabaseHandler();

    Protocol getProtocol(String sID);

    Set<Protocol> listProtocols();
    Set<Protocol> listProtocols(String sParliament);
    Set<Protocol> listProtocols(String sParliament, Date pStartDate, Date pEndDate);

    Set<Protocol> listProtocols(Date pStartDate, Date pEndDate);

    

}
