package org.texttechnologylab.downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BundestagDownloader {

    // example: downloadLatestContent("https://www.bundestag.de/ajax/filterlist/de/services/opendata/1058442-1058442", "https://www.bundestag.de/ajax/filterlist/de/dokumente/protokolle/442112-442112", "10", "0");
    public void downloadLatestContent(String xmlAjax, String videoAjax, String limit, String offset) throws IOException {
        //?noFilterSet=true&limit=10&offset=0
        downloadLatestContent(xmlAjax, videoAjax, true, limit, offset);
    }

    public void downloadLatestContent(String xmlAjax, String videoAjax, boolean noFilterSet, String limit, String offset) throws IOException {
        //?noFilterSet=true&limit=10&offset=0
        getData(xmlAjax, videoAjax, true, limit, offset);
    }

    public void getData(String xmlAjax, String videoAjax, boolean noFilterSet, String limit, String offset) throws IOException {
        List<ProtocolElement> result = getXmls(xmlAjax + "?noFilterSet=" + noFilterSet + "&limit=" + limit + "&offset=" + offset);
        getVideos(videoAjax + "?noFilterSet=" + noFilterSet + "&limit=" + limit + "&offset=" + offset, result);

        for(ProtocolElement element : result){
            System.out.println(element.toString());
        }
    }

    private List<ProtocolElement> getXmls(String url) throws IOException {
        // Parse the HTML
        Document doc = Jsoup.connect(url).get();

        // Get all <tr> elements in tbody
        Elements trElements = doc.select("tbody tr");

        System.out.println("Found " + trElements.size() + " protocols\n");

        // Extract XML href from each <tr>
        List<ProtocolElement> protocols = new ArrayList<>();
        for (Element tr : trElements) {
            // Get the protocol name/title
            Element titleElement = tr.selectFirst("strong");
            String title = titleElement != null ? titleElement.text() : "Unknown";

            // Get the XML link
            Element xmlLinkElement = tr.selectFirst("a[href$=.xml]");
            if (xmlLinkElement != null) {
                String xmlHref = xmlLinkElement.attr("href");

                // Get XML ID
                String xmlId = xmlHref.split("/")[xmlHref.split("/").length - 1];
                xmlId = xmlId.substring(0, xmlId.length() - 4); // remove ".xml"

                // Get Session ID
                String sessionNo = title.split("Plenarprotokoll der ")[1].split(". Sitzung")[0];

                protocols.add(new ProtocolElement(Integer.parseInt(xmlId), Integer.parseInt(sessionNo)));
            }

            //String videoUrl = "https://www.bundestag.de/ajax/filterlist/de/dokumente/protokolle/442112-442112?" + url.split("\\?")[1];
            //getVideo(videoUrl);
        }

        return protocols;
    }

    public void getVideos(String videoUrl, List<ProtocolElement> protocolElements) throws IOException {
        Document doc = Jsoup.connect(videoUrl).get();
        Elements sessions = doc.select("tbody tr");

        int i = 0;
        for(Element session : sessions){

            // Extract session name (number, date)
            Element sessionNameElement = session.selectFirst("h3");
            String sessionName = sessionNameElement.text();

            // Extract main "Video der Sitzung" href
            Element fullVideoLinkElement = session.selectFirst("a:contains(Video der Sitzung)");
            if (fullVideoLinkElement != null) {
                String fullVideoLinkHref = fullVideoLinkElement.attr("href");

                // Check session ID
                if(Integer.parseInt(sessionName.split(". Sitzung,")[0]) == protocolElements.get(i).getSessionNo() ){
                    protocolElements.get(i).setVideoId(Integer.parseInt(fullVideoLinkHref.split("videoid=")[1]));
                }
            }

            // Extract all TOPs
            Elements topElements = session.select("ul.bt-top-liste > li");
            int topId = 0;
            for (Element topElement : topElements) {
                // Get TOP name
                Element topNameElement = topElement.selectFirst("strong");
                String topName = topNameElement.text();

                TOP top = new TOP(topId, topName.split("TOP ")[1]);

                // Get "Video der Sitzung" for this TOP
                Element topVideoLinkElement = topElement.selectFirst("a:contains(Video der Sitzung)");
                if (topVideoLinkElement != null) {
                    String topVideoHref = topVideoLinkElement.attr("href");
                    top.setVideoId(Integer.parseInt(topVideoHref.split("videoid=")[1]));
                }

                // Get all "Video des Redebeitrags" for this TOP
                Elements speechLiElements = topElement.select("ul.bt-redner-liste > li");
                if (!speechLiElements.isEmpty()) {
                    for (Element speechLi : speechLiElements) {
                        Element speakerNameElement = speechLi.selectFirst("strong");
                        String speakerName = speakerNameElement.text();

                        Element speakerLinkElement = speechLi.selectFirst("a");
                        String speakerHref = speakerLinkElement.attr("href");
                        int speakerId = Integer.parseInt(speakerHref.split("-")[speakerHref.split("-").length - 1]);

                        TOPSpeaker speaker = new TOPSpeaker(speakerId, speakerName);

                        Element videoLinkElement = speechLi.selectFirst("a:contains(Video des Redebeitrags)");
                        if(videoLinkElement != null){
                            speaker.setVideoId(Integer.parseInt(videoLinkElement.attr("href").split("videoid=")[1]));
                        }

                        top.addSpeaker(speaker);
                    }
                }
                topId++;
                protocolElements.get(i).addTop(top);
            }

            i++;
        }
    }

    private String getVideoWebsiteUrl(String videoId){
        return "https://www.bundestag.de/mediathek/video?videoid=" + videoId;
    }

    private String websiteUrlToMp4Url(String videoId){
        return "https://cldf-od.r53.cdn.tv1.eu/1000153copo/ondemand/app144277506/145293313/" + videoId + "/" + videoId + "_h264_512_288_514kb_baseline_de_514.mp4";
    }

    private String websiteUrlToVttUrl(String videoId){
        return "https://webtv.bundestag.de/pservices/player/vtt/?application=144277506&content=" + videoId;
    }
}
