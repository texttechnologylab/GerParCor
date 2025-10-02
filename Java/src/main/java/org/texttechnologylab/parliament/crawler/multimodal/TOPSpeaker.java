package org.texttechnologylab.downloader;

public class TOPSpeaker {
    private int id;
    private String name;
    private int videoId;

    public TOPSpeaker(int id, String name){
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return "TOPSpeaker: " +
                "id=" + id +
                ", name='" + name + '\'' +
                ", videoId=" + videoId;
    }
}
