package com.example.timetablemanager.models;

public class ModelDay {

    String title;

    long id,timestamp;
    public ModelDay() {
    }

    public ModelDay(long id, long timestamp, String title) {
        this.id = id;
        this.timestamp = timestamp;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

