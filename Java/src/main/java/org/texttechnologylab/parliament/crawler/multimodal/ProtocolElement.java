package org.texttechnologylab.parliament.crawler.multimodal;

import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolElement {
    private int protocolId;
    private String protocolUrl;
    private int videoId;
    private int sessionNo;
    private List<TOP> topList;

    public ProtocolElement(int protocolId, String protocolUrl, int sessionNo){
        this.protocolId = protocolId;
        this.protocolUrl = protocolUrl;
        this.sessionNo = sessionNo;
        this.videoId = -1;
        topList = new ArrayList<>();
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public String getProtocolUrl() {
        return protocolUrl;
    }

    public void setProtocolUrl(String protocolUrl) {
        this.protocolUrl = protocolUrl;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public int getSessionNo() {
        return sessionNo;
    }

    public void setSessionNo(int sessionNo) {
        this.sessionNo = sessionNo;
    }

    public List<TOP> getTopList() {
        return topList;
    }

    public void setTopList(List<TOP> topList) {
        this.topList = topList;
    }

    public void addTop(TOP top){
        topList.add(top);
    }

    @Override
    public String toString() {
        String r = "ProtocolElement: " +
                "protocolId=" + protocolId +
                ", protocolUrl=" + protocolUrl +
                ", videoId=" + videoId +
                ", sessionNo=" + sessionNo +
                "\nTOPs:";

        for(TOP top : topList){
            r += "\n" + top.toString();
        }

        return r;
    }

    public void downloadEverything(String path) throws IOException {

        // Download XML
        FileUtils.downloadFile(new File(path + "/" + getProtocolId() + ".xml"), getProtocolUrl());

        // Download Video
        //if(videoId > -1) {
        //    FileUtils.downloadFile(new File(path + "/Session_" + getVideoId() + ".mp4"), BundestagDownloader.websiteUrlToMp4Url(Integer.toString(getVideoId())));
        //}

        for(var top : getTopList()){
            top.downloadVideos(path);
        }
    }

    private int currentSpeech = 0;
    public TOPSpeech getCurrentSpeech(){

        System.out.println(currentSpeech);

        int skipped = 0;
        for (TOP top : getTopList()){
            if(top.getSpeakerList().size() + skipped <= currentSpeech){
                skipped += top.getSpeakerList().size();
                continue;
            }else{
                return top.getSpeakerList().get(currentSpeech - skipped);
            }
        }

        return null;
    }

    public void currentSpeechMinus(){
        currentSpeech--;
    }

    public void currentSpeechPlus(){
        currentSpeech++;
    }

    public TOPSpeech findSpeechBy(String firstName, String title, String lastName){
        int startedAt = currentSpeech;
        currentSpeechPlus();

        while(true){

            TOPSpeech speaker = getCurrentSpeech();

            if(speaker == null){
                if(currentSpeech == 0) {
                    System.out.println("Protocol does not have any video speeches.");
                    return null;  // No videos exist
                }

                currentSpeech = 0;
                continue;
            }

            if(xmlNameMatchesVideoName(firstName, title, lastName, speaker.getName())){
                return speaker;
            }

            if(startedAt == currentSpeech) {
                return null;  // No video found
            }

            currentSpeechPlus();
        }
    }

    public static boolean xmlNameMatchesVideoName(String firstName, String title, String lastName, String videoName){
        String xmlName = lastName + ", " + (title.isEmpty() ? "" : title + " ") + firstName;

        String[] videoNameSplit = videoName.split(",");

        videoName = videoNameSplit[0] + (videoNameSplit.length > 1 ?  "," + videoNameSplit[1] : "");

        return xmlName.equals(videoName);
    }
}
