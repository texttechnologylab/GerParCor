package org.texttechnologylab.parliament.data.impl;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.texttechnologylab.parliament.data.ParliamentFactory;
import org.texttechnologylab.parliament.data.Protocol;
import org.texttechnologylab.parliament.database.MongoDBConnectionHandler;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    public Protocol getProtocol(String sID) {
        Document pDocument = pDBHandler.getObject(sID);
        Protocol rProtocol = new Protocol_Impl(this, pDocument);
        return rProtocol;
    }

    @Override
    public Set<Protocol> listProtocols() {
        Set<Protocol> rSet = new HashSet<>(0);
        Bson sort = BsonDocument.parse("{timestamp: 1}");

        MongoCursor<Document> pCursor = this.getDatabaseHandler().getCollection().find().sort(sort).cursor();
        pCursor.forEachRemaining(d->{
            rSet.add(new Protocol_Impl(this, d));
        });

        return rSet;
    }

    @Override
    public Set<Protocol> listProtocols(String sParliament) {

        if(sParliament.equalsIgnoreCase("all")){
            return listProtocols();
        }

        Set<Protocol> rSet = new HashSet<>(0);
        Bson sort = BsonDocument.parse("{timestamp: 1}");

            MongoCursor<Document> pCursor = this.getDatabaseHandler().getCollection().find(BsonDocument.parse("{\"meta.parliament\": "+sParliament+"}")).sort(sort).cursor();
            pCursor.forEachRemaining(d->{
                rSet.add(new Protocol_Impl(this, d));
            });

        return rSet;

    }

    @Override
    public Set<Protocol> listProtocols(String sCountry, String sDevision) {
        Set<Protocol> rSet = new HashSet<>(0);

        List<Bson> filters = new ArrayList<>();
        if(sCountry.equalsIgnoreCase("all")) filters.add(Filters.eq("meta.country", sCountry));
        if(sDevision.equalsIgnoreCase("all"))filters.add(Filters.eq("meta.devision", sDevision));
        MongoCursor<Document> pCursor = this.getDatabaseHandler().getCollection().find(Filters.and(filters)).cursor();
        pCursor.forEachRemaining(d->{
            rSet.add(new Protocol_Impl(this, d));
        });

        return rSet;
    }

    @Override
    public Set<Protocol> listProtocols(String sParliament, String sDevision, String sCountry) {
        return listProtocols(sParliament, sDevision, sCountry, 0, 30);
    }

    @Override
    public Set<Protocol> listProtocols(String sParliament, String sDevision, String sCountry, int iSkip, int iLimit) {
        Set<Protocol> rSet = new HashSet<>(0);

        List<Bson> filters = new ArrayList<>();
        if(!sCountry.equalsIgnoreCase("all")) filters.add(Filters.eq("meta.country", sCountry));
        if(!sDevision.equalsIgnoreCase("all")) filters.add(Filters.eq("meta.devision", sDevision));
        if(!sParliament.equalsIgnoreCase("all")) filters.add(Filters.eq("meta.parliament", sParliament));
        Bson sort = BsonDocument.parse("{timestamp: 1}");

        MongoCursor<Document> pCursor = this.getDatabaseHandler().getCollection().find(filters.size()>0 ? Filters.and(filters) : BsonDocument.parse("{}")).sort(sort).skip(iSkip*iLimit).limit(iLimit).cursor();
        pCursor.forEachRemaining(d->{
            rSet.add(new Protocol_Impl(this, d));
        });

        return rSet;
    }

    @Override
    public Set<Protocol> listProtocols(String sParliament, Date pStartDate, Date pEndDate) {
        return null;
    }

    @Override
    public Set<Protocol> listProtocols(Date pStartDate, Date pEndDate) {
        return null;
    }

    @Override
    public Set<String> listCountries() {

        Set<String> rSet = new HashSet<>(0);

        List<Bson> query = new ArrayList<>();

        query.add(Aggregates.group("$meta.country", new BsonField("count", BsonDocument.parse("{ $sum: 1 }"))));
        query.add(Aggregates.sort(Sorts.descending("_id")));

        MongoCursor<Document> pResult = this.getDatabaseHandler().getCollection().aggregate(query).cursor();

        pResult.forEachRemaining(r->{
            rSet.add(r.getString("_id"));
        });

        return rSet;

    }

    @Override
    public Set<String> listDevisions() {
        Set<String> rSet = new HashSet<>(0);

        List<Bson> query = new ArrayList<>();

        query.add(Aggregates.group("$meta.devision", new BsonField("count", BsonDocument.parse("{ $sum: 1 }"))));

        MongoCursor<Document> pResult = this.getDatabaseHandler().getCollection().aggregate(query).cursor();

        pResult.forEachRemaining(r->{
            rSet.add(r.getString("_id"));
        });

        return rSet;
    }

    @Override
    public Set<String> listParliaments() {
        Set<String> rSet = new HashSet<>(0);

        List<Bson> query = new ArrayList<>();

        query.add(Aggregates.group("$meta.parliament", new BsonField("count", BsonDocument.parse("{ $sum: 1 }"))));

        MongoCursor<Document> pResult = this.getDatabaseHandler().getCollection().aggregate(query).cursor();

        pResult.forEachRemaining(r->{
            rSet.add(r.getString("_id"));
        });

        return rSet;
    }

    @Override
    public void getTimeRanges(){
        List<Bson> query = new ArrayList<>();

        query.add(Aggregates.unwind("$timestamp"));
        query.add(Aggregates.unwind("$meta.country"));
        query.add(Aggregates.group("$meta.parliament", new BsonField("valueMin", BsonDocument.parse("{ $min: \"$timestamp\" }")), new BsonField("valueMax", BsonDocument.parse("{ $max: \"$timestamp\" }"))));
        query.add(Aggregates.sort(Sorts.ascending("meta.country", "meta.parliament")));

        MongoCursor<Document> pResult = this.getDatabaseHandler().getCollection().aggregate(query).cursor();

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        pResult.forEachRemaining(r->{

            System.out.println(r.get("_id") +" \t & " + sdf.format(new Timestamp(r.getLong("valueMin"))) +" \t & " + sdf.format(new Timestamp(r.getLong("valueMax"))) );
        });


    }

    @Override
    public Set<Protocol> doQuery(Bson query){

        Set<Protocol> rSet = new HashSet<>(0);

        MongoCursor<Document> pResult = this.getDatabaseHandler().getCollection().find(query).cursor();

        while(pResult.hasNext()){
            Document d = pResult.next();
            rSet.add(new Protocol_Impl(d));
        }

        return rSet;

    }
}
