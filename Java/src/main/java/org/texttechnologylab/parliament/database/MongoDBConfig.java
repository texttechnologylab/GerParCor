package org.texttechnologylab.parliament.database;

import java.io.*;
import java.util.Properties;

/**
 * Class for managing properties for connection to a MongoDB
 * @author Giuseppe Abrami
 */
public class MongoDBConfig extends Properties {

    /**
     * Constructor with a File-Object for the Config-File
     * @param pFile
     * @throws IOException
     */
    public MongoDBConfig(File pFile) throws IOException {
        this(pFile.getAbsolutePath());
    }

    /**
     * Constructor with the path of the Config-File
     * @param sPath
     * @throws IOException
     */

    public MongoDBConfig(String sPath) throws IOException {
        String current = new File( "." ).getCanonicalPath();
        BufferedReader lReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sPath)), "UTF-8"));
        this.load(lReader);
        lReader.close();
    }

    /**
     * Method for the Hostname
     * @return
     */
    public String getMongoHostname(){
        return getProperty("remote_host", "127.0.0.1");

    }

    /**
     * Method for the Username
     * @return
     */
    public String getMongoUsername(){
        return getProperty("remote_user", "user");

    }

    /**
     * Method for the Password
     * @return
     */
    public String getMongoPassword(){
        return getProperty("remote_password", "password");
    }

    /**
     * Method for the Port
     * @return
     */
    public int getMongoPort(){
        return Integer.valueOf(getProperty("remote_port", "27017"));
    }


    /**
     * Method for the Database name
     * @return
     */
    public String getMongoDatabase(){
        return getProperty("remote_database", "database");
    }

    /**
     * Method for the Collection to connect
     * @return
     */
    public String getMongoCollection(){
        return getProperty("remote_collection", "collection");
    }


}
