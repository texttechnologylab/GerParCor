package org.texttechnologylab.downloader;

import java.util.ArrayList;
import java.util.List;

public class ProtocolElement {
    private int protocolId;
    private int videoId;
    private int sessionNo;
    private List<TOP> topList;

    public ProtocolElement(int protocolId, int sessionNo){
        this.protocolId = protocolId;
        this.sessionNo = sessionNo;
        topList = new ArrayList<>();
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
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
                ", videoId=" + videoId +
                ", sessionNo=" + sessionNo +
                "\nTOPs:";

        for(TOP top : topList){
            r += "\n" + top.toString();
        }

        return r;
    }
}
