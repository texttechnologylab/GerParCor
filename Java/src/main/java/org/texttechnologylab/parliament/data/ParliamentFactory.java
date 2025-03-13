package org.texttechnologylab.parliament.data;

import org.bson.conversions.Bson;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.sql.Date;
import java.util.List;
import java.util.Set;

public interface ParliamentFactory {

    MongoDBConnectionHandler getDatabaseHandler();

    Protocol getProtocol(String sID);

    List<Protocol> listProtocols();
    List<Protocol> listProtocols(String sParliament);
    List<Protocol> listProtocols(String sParliament, String sDevision);
    List<Protocol> listProtocols(String sParliament, String sDevision, String sCountry);

    long countProtocols(String sParliament, String sDevision, String sCountry);
    long countProtocols();

    List<Protocol> listProtocols(String sParliament, String sDevision, String sCountry, int iSkip, int iLimit);

    List<Protocol> listProtocols(String sParliament, Date pStartDate, Date pEndDate);

    List<Protocol> listProtocols(Date pStartDate, Date pEndDate);

    Set<String> listCountries();

    Set<String> listDevisions();


    Set<String> listParliaments();


    Set<String> listParliaments(String sCountry);

    void getTimeRanges();

    Set<Protocol> doQuery(Bson query);
}
