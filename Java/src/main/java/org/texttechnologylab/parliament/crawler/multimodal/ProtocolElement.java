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
        FileUtils.downloadFile(new File(path + "/" + getVideoId() + ".xml"), getProtocolUrl());

        // Download Video
        if(videoId > -1) {
            FileUtils.downloadFile(new File(path + "/Session_" + getVideoId() + ".mp4"), BundestagDownloader.websiteUrlToMp4Url(Integer.toString(getVideoId())));
        }

        for(var top : getTopList()){
            top.downloadVideos(path);
        }
    }
}
