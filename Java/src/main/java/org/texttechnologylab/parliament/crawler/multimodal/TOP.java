package org.texttechnologylab.parliament.crawler.multimodal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TOP {
    private int id;
    private String name;
    private int videoId;
    private List<TOPSpeech> speakerList;

    public TOP(int id, String name){
        this.id = id;
        this.name = name;
        this.videoId = -1;
        speakerList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public List<TOPSpeech> getSpeakerList() {
        return speakerList;
    }

    public void setSpeakerList(List<TOPSpeech> speakerList) {
        this.speakerList = speakerList;
    }

    public void addSpeaker(TOPSpeech speaker) {
        this.speakerList.add(speaker);
    }

    @Override
    public String toString() {
        String r = "TOP: " +
                "id=" + id +
                ", name='" + name + '\'' +
                ", videoId=" + videoId +
                "\nspeakers:";

        for(TOPSpeech speaker : speakerList){
            r += "\n" + speaker.toString();
        }

        return r;
    }

    public void downloadVideos(String path) throws IOException {
        //if(videoId > -1) {
        //    FileUtils.downloadFile(new File(path + "/Top_" + getId() + "_" + getVideoId() + ".mp4"), BundestagDownloader.websiteUrlToMp4Url(Integer.toString(getVideoId())));
        //}

        for(var speaker : getSpeakerList()){
            speaker.downloadVideos(path, getId());
        }
    }
}
