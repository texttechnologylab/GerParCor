package org.texttechnologylab.downloader;

import java.util.ArrayList;
import java.util.List;

public class TOP {
    private int id;
    private String name;
    private int videoId;
    private List<TOPSpeaker> speakerList;

    public TOP(int id, String name){
        this.id = id;
        this.name = name;
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

    public List<TOPSpeaker> getSpeakerList() {
        return speakerList;
    }

    public void setSpeakerList(List<TOPSpeaker> speakerList) {
        this.speakerList = speakerList;
    }

    public void addSpeaker(TOPSpeaker speaker) {
        this.speakerList.add(speaker);
    }

    @Override
    public String toString() {
        String r = "TOP: " +
                "id=" + id +
                ", name='" + name + '\'' +
                ", videoId=" + videoId +
                "\nspeakers:";

        for(TOPSpeaker speaker : speakerList){
            r += "\n" + speaker.toString();
        }

        return r;
    }
}
