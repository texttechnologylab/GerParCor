package org.texttechnologylab.parliament.crawler;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.annotation.DocumentModification;
import org.texttechnologylab.parliament.crawler.multimodal.BundestagDownloader;
import org.texttechnologylab.parliament.crawler.multimodal.ProtocolElement;
import org.texttechnologylab.utilities.helper.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Class for Parsing Bundestag-Minutes
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Bundestag {

    @Test
    public void bundestagNeu20(){

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");


        try {

            String sOutputPath = "20/";
            String sBasePath = "/tmp/Bundestag/";
//            TextImagerProcessing tiProcessing = new TextImagerProcessing(sOutputPath);
//            System.setProperty("accessExternalDTD", "true");
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            dbf.setValidating(false);
            dbf.setNamespaceAware(false);
//            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
//            dbf.setFeature("http://xml.org/sax/features/validation", false);
//            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
//            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();


            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat sdfExport = new SimpleDateFormat("yyyy-MM-dd");

            // processing
            String sPath = "/tmp/bundestagNeu/20";
            Set<File> fileSet = FileUtils.getFiles(sPath, ".xml");

            fileSet.stream().forEach(f->{

                Document doc = null;
                try {
                    doc = db.parse(f);

                    // optional, but recommended
                    // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                    doc.getDocumentElement().normalize();
//                    doc.normalizeDocument();

                    String sDatum = doc.getElementsByTagName("datum").item(0).getAttributes().getNamedItem("date").getNodeValue();
                    String sPeriode = doc.getElementsByTagName("wahlperiode").item(0).getTextContent();
                    String sNR = doc.getElementsByTagName("sitzungsnr").item(0).getTextContent();
                    String sText = doc.getElementsByTagName("inhaltsverzeichnis").item(0).getTextContent();

                    sText =  sText.replaceAll("[\\x00-\\x09]", "");
                    sText =  sText.replaceAll("[\\x0B-\\x1F]", "");
                    sText =  sText.replaceAll("[\\x20]", " ");
                    sText =  sText.replaceAll("[\\x7F-\\x9F]", "");
                    sText =  sText.replaceAll("\u0096", "'");
                    sText =  sText.replaceAll("\u0093", "");
                    sText =  sText.replaceAll("\u0084", "");
                    sText =  sText.replaceAll("\u0085", "");
                    sText =  sText.replaceAll("\\-\n", "");
                    sText =  sText.replaceAll("\n", " ");
                    sText =  sText.replaceAll("  ", " ");
                    sText =  sText.replaceAll("&#38;", "&");
//                    sText =  sText.replaceAll("\n", " ");
//                    sText =  sText.replaceAll("I n h a l t :", "Inhalt:");
                    //sText =  sText.replaceAll("&amp;#38", "&");
//                    sText =  sText.replaceAll("\n", " ");

                    JCas pCas = JCasFactory.createText(sText, "de");
                    Date pDate = sdf.parse(sDatum);

                    DocumentMetaData dmd = DocumentMetaData.create(pCas);
                    dmd.setDocumentTitle(sNR+" vom "+sDatum);
                    dmd.setDocumentId(sdfExport.format(pDate));
                    dmd.setDocumentBaseUri(sBasePath);
                    dmd.setDocumentUri(sBasePath+sOutputPath+sdfExport.format(pDate));

                    DocumentAnnotation dma = new DocumentAnnotation(pCas);
                    dma.setAuthor("Bundestagsverwaltung");
                    dma.setSubtitle("Wahlperiode "+sPeriode+": "+sNR);

                    dma.setDateDay(Integer.parseInt(sdfDay.format(pDate)));
                    dma.setDateMonth(Integer.parseInt(sdfMonth.format(pDate)));
                    dma.setDateYear(Integer.parseInt(sdfYear.format(pDate)));
                    dma.setTimestamp(pDate.getTime());
                    dma.addToIndexes();

                    DocumentModification docMod = new DocumentModification(pCas);
                    docMod.setUser("abrami");
                    docMod.setComment("Initial Transformation");
                    docMod.setTimestamp(System.currentTimeMillis());
                    docMod.addToIndexes();

                    DocumentModification dm = new DocumentModification(pCas);
                    dm.setTimestamp(System.currentTimeMillis());
                    dm.setUser("abrami");
                    dm.setComment("Converting");
                    dm.addToIndexes();

                    CasIOUtils.save(pCas.getCas(), new FileOutputStream(new File(dmd.getDocumentUri()+".xmi")), SerialFormat.XMI_1_1);

                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UIMAException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            });

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    @Test
    public void bundestagNeu(){

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");


        try {

            String sOutputPath = "Bundestag/15/";
            String sBasePath = "/tmp/Bundestag/";
//            TextImagerProcessing tiProcessing = new TextImagerProcessing(sOutputPath);
//            System.setProperty("accessExternalDTD", "true");
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
//            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
//            dbf.setFeature("http://xml.org/sax/features/validation", false);
//            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
//            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

//            db.setEntityResolver(new EntityResolver()
//            {
//                @Override
//                public InputSource resolveEntity(String publicId, String systemId)
//                        throws SAXException, IOException
//                {
////                    System.out.println(systemId);
//                    return new InputSource(new FileInputStream(new File("/tmp/bundestag/19/xml/dbtplenarprotokoll.dtd")));
////                    if (systemId.equals("urn:x-com.kdgregory.example.xml.parsing"))
////                    {
////                        // normally you open the DTD file/resource here
////                        return new InputSource(dtd);
////                    }
////                    else
////                    {
////                        throw new SAXException("unable to resolve entity; "
////                                + "public = \"" + publicId + "\", "
////                                + "system = \"" + systemId + "\"");
////                    }
//                }
//            });

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat sdfExport = new SimpleDateFormat("yyyy-MM-dd");

            // processing
            String sPath = "/home/staff_homes/abrami/Downloads/pp15-data/";
            Set<File> fileSet = FileUtils.getFiles(sPath, ".xml");

            fileSet.stream().forEach(f->{

                Document doc = null;
                try {
                    doc = db.parse(f);

                // optional, but recommended
                // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                    doc.getDocumentElement().normalize();
//                    doc.normalizeDocument();

                    String sPeriode = doc.getElementsByTagName("WAHLPERIODE").item(0).getTextContent();
                    String sType = doc.getElementsByTagName("DOKUMENTART").item(0).getTextContent();
                    String sNR = doc.getElementsByTagName("NR").item(0).getTextContent();
                    String sDatum = doc.getElementsByTagName("DATUM").item(0).getTextContent();
                    String sText = doc.getElementsByTagName("TEXT").item(0).getTextContent();

                    sText =  sText.replaceAll("[\\x00-\\x09]", "");
                    sText =  sText.replaceAll("[\\x0B-\\x1F]", "");
                    sText =  sText.replaceAll("[\\x20]", " ");
                    sText =  sText.replaceAll("[\\x7F-\\x9F]", "");
                    sText =  sText.replaceAll("\u0096", "'");
                    sText =  sText.replaceAll("\u0093", "");
                    sText =  sText.replaceAll("\u0084", "");
                    sText =  sText.replaceAll("\u0085", "");
                    sText =  sText.replaceAll("\\-\n", "");
                    sText =  sText.replaceAll("\n", " ");
                    sText =  sText.replaceAll("  ", " ");
                    sText =  sText.replaceAll("&#38;", "&");
//                    sText =  sText.replaceAll("\n", " ");
//                    sText =  sText.replaceAll("I n h a l t :", "Inhalt:");
                    //sText =  sText.replaceAll("&amp;#38", "&");
//                    sText =  sText.replaceAll("\n", " ");

                    JCas pCas = JCasFactory.createText(sText, "de");
                    Date pDate = sdf.parse(sDatum);

                    DocumentMetaData dmd = DocumentMetaData.create(pCas);
                    dmd.setDocumentTitle(sType+" vom "+sDatum);
                    dmd.setDocumentId(sdfExport.format(pDate));
                    dmd.setDocumentBaseUri(sBasePath);
                    dmd.setDocumentUri(sBasePath+sOutputPath+sdfExport.format(pDate));

                    DocumentAnnotation dma = new DocumentAnnotation(pCas);
                    dma.setAuthor("Bundestagsverwaltung");
                    dma.setSubtitle("Wahlperiode "+sPeriode+": "+sNR);


                    dma.setDateDay(Integer.parseInt(sdfDay.format(pDate)));
                    dma.setDateMonth(Integer.parseInt(sdfMonth.format(pDate)));
                    dma.setDateYear(Integer.parseInt(sdfYear.format(pDate)));
                    dma.setTimestamp(pDate.getTime());
                    dma.addToIndexes();

                    DocumentModification docMod = new DocumentModification(pCas);
                    docMod.setUser("abrami");
                    docMod.setComment("Initial Transformation");
                    docMod.setTimestamp(System.currentTimeMillis());
                    docMod.addToIndexes();

                    DocumentModification dm = new DocumentModification(pCas);
                    dm.setTimestamp(System.currentTimeMillis());
                    dm.setUser("abrami");
                    dm.setComment("Converting");
                    dm.addToIndexes();

                    CasIOUtils.save(pCas.getCas(), new FileOutputStream(new File(dmd.getDocumentUri()+".xmi")), SerialFormat.XMI_1_1);

                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UIMAException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                     e.printStackTrace();
                }


            });

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void downloadWahlperiode(String sessionID, int iWahlperiode) throws IOException {

        int offset = 0;

        while(true) {

            org.jsoup.nodes.Document nDocument = Jsoup.connect("https://www.bundestag.de/ajax/filterlist/de/services/opendata/"+sessionID+"?limit=10&noFilterSet=true&offset="+offset).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();

            Elements el = nDocument.select("div.bt-documents-description");


            el.forEach(e -> {

                String sName = e.select("strong").text();
                String sURI = e.select("a").attr("href");

                String sID = sName.substring(0, sName.indexOf(".")).substring(sName.substring(0, sName.indexOf(".")).lastIndexOf(" ") + 1);
                System.out.println(sName);
                System.out.println(sURI);

                try {
                    FileUtils.downloadFile(new File("/tmp/bundestagNeu/"+iWahlperiode+"/" + sID + ".xml"), "https://bundestag.de"+sURI);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            });
            offset+=10;
        }
    }

    public static void downloadWahlperiodeMultimodal(String sessionID, String videoSessionID, int iWahlperiode) throws IOException {

        int offset = 0;

        String baseDownloadPath = "./downloads/bundestagNeu/" + iWahlperiode;

        File baseFolder = new File(baseDownloadPath);
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }

        while(true) {


            BundestagDownloader downloader = new BundestagDownloader();
            List<ProtocolElement> results = downloader.downloadLatestContent(sessionID, videoSessionID, iWahlperiode, "10", Integer.toString(offset));

            if(results.isEmpty())
                return;

            for(var result : results) {

                String resultDownloadPath = baseDownloadPath + "/" + result.getProtocolId();

                File folder = new File(resultDownloadPath);

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                result.downloadEverything(resultDownloadPath);
            }

            offset+=10;
        }
    }

    @Test
    public void wahlperioden() throws IOException {
        //downloadWahlperiode("543410-543410", 19);
        //downloadWahlperiode("866354-866354", 20);
        downloadWahlperiodeMultimodal("058442-1058442", "442112-442112", 21);
    }

    @Test
    public void Periode19() throws IOException {

        org.jsoup.nodes.Document pDocument = Jsoup.parse(new URL("https://www.bundestag.de/services/opendata#bt-collapse-543410"), 3000);


        //Elements el = pDocument.select("table.bt-table-data tbody tr td.title a");

        int offset = 0;

        while(true) {

            org.jsoup.nodes.Document nDocument = Jsoup.connect("https://www.bundestag.de/ajax/filterlist/de/services/opendata/543410-543410?limit=10&noFilterSet=true&offset="+offset).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();

            Elements el = nDocument.select("div.bt-documents-description");


            el.forEach(e -> {

                String sName = e.select("strong").text();
                String sURI = e.select("a").attr("href");

                String sID = sName.substring(0, sName.indexOf(".")).substring(sName.substring(0, sName.indexOf(".")).lastIndexOf(" ") + 1);
                System.out.println(sName);
                System.out.println(sURI);

                try {
                    FileUtils.downloadFile(new File("/tmp/bundestagNeu/19/" + sID + ".xml"), "https://bundestag.de"+sURI);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            });
            offset+=10;
        }



    }

    @Test
    public void Drucksachen() throws IOException {

        //Elements el = pDocument.select("table.bt-table-data tbody tr td.title a");

        int offset = 0;

        while(true) {

            org.jsoup.nodes.Document nDocument = Jsoup.connect("https://www.bundestag.de/ajax/filterlist/de/services/opendata/854776-854776?limit=10&noFilterSet=true&offset="+offset).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();

            Elements el = nDocument.select("div.bt-documents-description li a");


            el.forEach(e -> {

                if(e.attr("title").equalsIgnoreCase("xml")){
                    String sLink = e.attr("href");
                    String sName = sLink.substring(sLink.lastIndexOf("/")+1);
                    sName = sName.substring(0, sName.indexOf("?"));
                    try {
                        FileUtils.downloadFile(new File("/tmp/Drucksachen/"+sName+".xml"), sLink);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                if(e.attr("title").equalsIgnoreCase("JSON")){
                    String sLink = e.attr("href");
                    String sName = sLink.substring(sLink.lastIndexOf("/")+1);
                    sName = sName.substring(0, sName.indexOf("?"));
                    try {
                        FileUtils.downloadFile(new File("/tmp/Drucksachen/"+sName+".json"), sLink);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            });
            offset+=10;
        }

    }
}
