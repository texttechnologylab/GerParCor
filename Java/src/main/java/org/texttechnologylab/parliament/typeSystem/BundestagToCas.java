package org.texttechnologylab.parliament.typeSystem;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.javatuples.Pair;
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.annotation.DocumentModification;
import org.texttechnologylab.annotation.parliamentary.*;
import org.texttechnologylab.parliament.crawler.multimodal.BundestagDownloader;
import org.texttechnologylab.parliament.crawler.multimodal.ProtocolElement;
import org.texttechnologylab.parliament.crawler.multimodal.TOPSpeech;
import org.texttechnologylab.parliament.helper.XMLParserHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BundestagToCas {

    public BundestagToCas(File bundestagXML, ProtocolElement protocolInfo) throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        dbf.setValidating(false);
        dbf.setNamespaceAware(false);

        DocumentBuilder builder = dbf.newDocumentBuilder();

        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                // You can match by filename or by systemId string
                if (systemId != null && systemId.endsWith("dbtplenarprotokoll.dtd")) {
                    return new InputSource(new FileInputStream("./downloads/bundestagNeu/dbtplenarprotokoll.dtd"));
                }
                // Default behavior for other entities
                return null;
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat sdfExport = new SimpleDateFormat("yyyy-MM-dd");

        Document doc = null;

        try {
            doc = builder.parse(bundestagXML);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            String sDatum = doc.getElementsByTagName("datum").item(0).getAttributes().getNamedItem("date").getNodeValue();
            String sPeriode = doc.getElementsByTagName("wahlperiode").item(0).getTextContent();
            String sNR = doc.getElementsByTagName("sitzungsnr").item(0).getTextContent();
            String sText = doc.getElementsByTagName("inhaltsverzeichnis").item(0).getTextContent();

            System.out.println("Proccesing " + sPeriode + " " + sNR);

            NodeList sessionNodes = doc.getElementsByTagName("sitzungsverlauf");

            if(sessionNodes.getLength() != 1){
                System.out.println("WARNING! Periode " + sPeriode + ", sNr. " + sNR + "has " + sessionNodes.getLength() + " sitzungsverläufe instead of just 1!");
            }

            StringBuilder protocolString = new StringBuilder();
            JCas cas = JCasFactory.createJCas();

            Protocol tProtocol = new Protocol(cas);
            //tProtocol.setDate();  TODO
            tProtocol.setElectionPeriod(Integer.parseInt(sPeriode));
            tProtocol.setSessionNumber(Integer.parseInt(sNR));
            tProtocol.addToIndexes();



            if(sessionNodes.getLength() > 0){

                int agendaNo = 0;

                if(XMLParserHelper.getFirstSubNodeByName(sessionNodes.item(0), "sitzungsbeginn") != null){
                    processAgenda(protocolString, agendaNo, XMLParserHelper.getFirstSubNodeByName(sessionNodes.item(0), "sitzungsbeginn"), tProtocol, cas, protocolInfo);
                    agendaNo++;
                }

                for(Node agenda : XMLParserHelper.getAllSubNodesByName(sessionNodes.item(0), "tagesordnungspunkt")) {
                    processAgenda(protocolString, agendaNo, agenda, tProtocol, cas, protocolInfo);
                    agendaNo++;
                }
            }

            cas.setDocumentText(protocolString.toString());
            cas.setDocumentLanguage("de");
            Date pDate = sdf.parse(sDatum);

            DocumentMetaData dmd = DocumentMetaData.create(cas);
            dmd.setDocumentTitle("Protokoll vom " + sDatum);
            dmd.setDocumentId(sdfExport.format(pDate));
            dmd.setDocumentBaseUri("./downloads/bundestagNeu");
            String sitzungsNr = sNR;
            if(sitzungsNr.length() == 2)
                sitzungsNr = "0" + sitzungsNr;
            else if(sitzungsNr.length() == 1)
                sitzungsNr = "00" + sitzungsNr;
            dmd.setDocumentUri("./downloads/bundestagNeu/" + sPeriode + "/" + sPeriode + sitzungsNr + "/" + sdfExport.format(pDate));

            DocumentAnnotation dma = new DocumentAnnotation(cas);
            dma.setAuthor("Bundestagsverwaltung");
            dma.setSubtitle("Wahlperiode " + sPeriode + ": " + sNR);


            dma.setDateDay(Integer.parseInt(sdfDay.format(pDate)));
            dma.setDateMonth(Integer.parseInt(sdfMonth.format(pDate)));
            dma.setDateYear(Integer.parseInt(sdfYear.format(pDate)));
            dma.setTimestamp(pDate.getTime());
            dma.addToIndexes();

            DocumentModification docMod = new DocumentModification(cas);
            docMod.setUser("bundan");
            docMod.setComment("Initial Transformation");
            docMod.setTimestamp(System.currentTimeMillis());
            docMod.addToIndexes();

            DocumentModification dm = new DocumentModification(cas);
            dm.setTimestamp(System.currentTimeMillis());
            dm.setUser("bundan");
            dm.setComment("Converting");
            dm.addToIndexes();

            CasIOUtils.save(cas.getCas(), new FileOutputStream(new File(dmd.getDocumentUri()+".xmi")), SerialFormat.XMI_1_1);

            System.out.println("ok");

            JCasUtil.select(cas, Video.class).forEach(v -> {

                System.out.println(v.getUrl() + ": ");
                JCasUtil.selectCovered(SpeechText.class, v).forEach(s -> {
                    System.out.println("    - " + s.getCoveredText());
                });

            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void processAgenda(StringBuilder protocolString, int index, Node agendaNode, Protocol tProtocol, JCas cas, ProtocolElement pe){

        Agenda tAgenda = new Agenda(cas);

        // Set basic info
        tAgenda.setBegin(protocolString.length());
        tAgenda.setProtocol(tProtocol);
        tAgenda.setIndex(index);

        Element element = (Element) agendaNode;
        if(element.hasAttribute("top-id"))
            tAgenda.setTitle(element.getAttribute("top-id"));


        // Get "agenda introduction speech"
        var textSections = XMLParserHelper.getSpeechAndComments(agendaNode);
        int speechNo = 0;
        int begin = protocolString.length();
        for(Pair<Integer, String> section : textSections){

            if(section.getValue0() == 0){
                SpeechText tSpeechText = new SpeechText(cas);
                tSpeechText.setSpeaker(null);
                tSpeechText.setBegin(protocolString.length());
                protocolString.append(cleanText(section.getValue1()));
                tSpeechText.setEnd(protocolString.length() - 1);
                tSpeechText.addToIndexes();
            }else{
                Comment tComment = new Comment(cas);
                tComment.setBegin(protocolString.length());
                protocolString.append(cleanText(section.getValue1()));
                tComment.setEnd(protocolString.length() - 1);
                tComment.addToIndexes();
            }

            speechNo = 1;
        }

        if(speechNo == 1){
            TOPSpeech topSpeaker = pe.getCurrentSpeech();

            Video video = new Video(cas);
            video.setBegin(begin);
            video.setIndex(speechNo);
            video.setId(Integer.toString(topSpeaker.getVideoId()));
            video.setUrl(BundestagDownloader.getVideoWebsiteUrl(Integer.toString(topSpeaker.getVideoId())));
            video.setEnd(protocolString.length());
            video.addToIndexes();
            pe.currentSpeechPlus();

        }

        // Get speeches inside agenda
        List<Node> speechNodes = XMLParserHelper.getAllSubNodesByName(agendaNode, "rede");
        for(Node speechNode : speechNodes){
            processSpeech(protocolString, index, speechNo, speechNode, cas, pe);
            speechNo++;
        }

        tAgenda.setEnd(protocolString.length() - 1);

    }

    public void processSpeech(StringBuilder protocolString, int agendaNo, int speechNo, Node speechNode, JCas cas, ProtocolElement pe){

        Speaker tSpeaker = new Speaker(cas);

        Node speakerNode = XMLParserHelper.getFirstSubNodeByName(speechNode, "redner");
        Element element = (Element) speakerNode;

        tSpeaker.setFirstName(Objects.requireNonNull(XMLParserHelper.getFirstSubNodeByName(speakerNode, "vorname")).getTextContent());
        tSpeaker.setLastName(Objects.requireNonNull(XMLParserHelper.getFirstSubNodeByName(speakerNode, "nachname")).getTextContent());

        String title = "";
        if(XMLParserHelper.getFirstSubNodeByName(speakerNode, "titel") != null){
            title = Objects.requireNonNull(XMLParserHelper.getFirstSubNodeByName(speakerNode, "titel")).getTextContent();
        }

        tSpeaker.setId(element.getAttribute("id"));

        Node role = XMLParserHelper.getFirstSubNodeByName(speakerNode, "rolle_lang");
        if(role != null){
            tSpeaker.setRole(role.getTextContent());
        }else{
            role = XMLParserHelper.getFirstSubNodeByName(speakerNode, "rolle_kurz");
            if(role != null)
                tSpeaker.setRole(role.getTextContent());
        }

        tSpeaker.addToIndexes();

        Speech tSpeech = new Speech(cas);
        tSpeech.setId(element.getAttribute("id"));
        tSpeech.setIndex(speechNo);
        tSpeech.setBegin(protocolString.length());

        var textSections = XMLParserHelper.getSpeechAndComments(speechNode);
        for(Pair<Integer, String> section : textSections){

            if(section.getValue0() == 0){
                SpeechText tSpeechText = new SpeechText(cas);
                tSpeechText.setSpeaker(tSpeaker);
                tSpeechText.setBegin(protocolString.length());
                protocolString.append(cleanText(section.getValue1()));
                tSpeechText.setEnd(protocolString.length() - 1);
                tSpeechText.addToIndexes();
            }else{
                Comment tComment = new Comment(cas);
                tComment.setBegin(protocolString.length());
                protocolString.append(cleanText(section.getValue1()));
                tComment.setEnd(protocolString.length() - 1);
                tComment.addToIndexes();
            }
        }

        tSpeech.setEnd(protocolString.length() - 1);
        tSpeech.addToIndexes();

        TOPSpeech speech = pe.getCurrentSpeech();
        if(!ProtocolElement.xmlNameMatchesVideoName(tSpeaker.getFirstName(), title, tSpeaker.getLastName(), pe.getCurrentSpeech().getName())){
            System.out.println("Incorrect speech... looking for right one.");
            speech = pe.findSpeechBy(tSpeaker.getFirstName(), title, tSpeaker.getLastName());
        }

        if(speech != null) {
            System.out.println("Speech found.");
            Video video = new Video(cas);
            video.setIndex(speechNo);
            video.setId(Integer.toString(speech.getVideoId()));
            video.setUrl(BundestagDownloader.getVideoWebsiteUrl(Integer.toString(speech.getVideoId())));
            video.setBegin(tSpeech.getBegin());
            video.setEnd(tSpeech.getEnd());
            video.addToIndexes();
            pe.currentSpeechPlus();
        }
    }

    public static String cleanText(String sText){
        sText = sText.replaceAll("[\\x00-\\x09]", "");
        sText = sText.replaceAll("[\\x0B-\\x1F]", "");
        sText = sText.replaceAll("[\\x20]", " ");
        sText = sText.replaceAll("[\\x7F-\\x9F]", "");
        sText = sText.replaceAll("\u0096", "'");
        sText = sText.replaceAll("\u0093", "");
        sText = sText.replaceAll("\u0084", "");
        sText = sText.replaceAll("\u0085", "");
        sText = sText.replaceAll("\\-\n", "");
        sText = sText.replaceAll("\n", " ");
        sText = sText.replaceAll("  ", " ");
        sText = sText.replaceAll("&#38;", "&");
        sText =  sText.replaceAll("I n h a l t :", "Inhalt:");
        return sText;
    }
}
