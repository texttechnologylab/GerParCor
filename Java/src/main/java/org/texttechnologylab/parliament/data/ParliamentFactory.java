package org.texttechnologylab.parliament.data;

import org.bson.conversions.Bson;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.sql.Date;
import java.util.List;
import java.util.Set;

public interface ParliamentFactory {

    MongoDBConnectionHandler getDatabaseHandler();

    Protocol getProtocol(String sID);

    Set<Protocol> listProtocols();
    Set<Protocol> listProtocols(String sParliament);
    Set<Protocol> listProtocols(String sParliament, String sDevision);
    Set<Protocol> listProtocols(String sParliament, String sDevision, String sCountry);

    long countProtocols(String sParliament, String sDevision, String sCountry);
    long countProtocols();

    Set<Protocol> listProtocols(String sParliament, String sDevision, String sCountry, int iSkip, int iLimit);

    Set<Protocol> listProtocols(String sParliament, Date pStartDate, Date pEndDate);

    Set<Protocol> listProtocols(Date pStartDate, Date pEndDate);

    Set<String> listCountries();

    Set<String> listDevisions();


    Set<String> listParliaments();


    Set<String> listParliaments(String sCountry);

    void getTimeRanges();

    Set<Protocol> doQuery(Bson query);
}
