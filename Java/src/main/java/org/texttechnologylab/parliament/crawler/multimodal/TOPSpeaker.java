package org.texttechnologylab.parliament.crawler.multimodal;

import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;

public class TOPSpeaker {
    private int speakerId;
    private String name;
    private int speechId;
    private int videoId;

    public TOPSpeaker(int speakerId, String name, int speechId){
        this.speakerId = speakerId;
        this.name = name;
        this.speechId = speechId;
        this.videoId = -1;
    }

    public int getSpeechId() {
        return speechId;
    }

    public void setSpeechId(int speechId) {
        this.speechId = speechId;
    }

    public int getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(int speakerId) {
        this.speakerId = speakerId;
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

    @Override
    public String toString() {
        return "TOPSpeaker: " +
                "speakerId=" + speakerId +
                ", name='" + name + '\'' +
                ", speechId='" + speechId + '\'' +
                ", videoId=" + videoId;
    }

    public void downloadVideos(String path, int topId) throws IOException {
        if(videoId > -1) {
            FileUtils.downloadFile(new File(path + "/Speech_" + topId + "_" + getSpeechId() + "_" + getSpeakerId() + "_" + getVideoId() + ".mp4"), BundestagDownloader.websiteUrlToMp4Url(Integer.toString(getVideoId())));
        }
    }
}
