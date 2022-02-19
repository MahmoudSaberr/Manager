package com.example.timetablemanager.models;

public class ModelTask {

    String id, title, description, dayId, time, timestamp, uid;

    public ModelTask() {
    }

    public ModelTask(String id, String title, String description, String dayId, String time, String timestamp, String uid) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dayId = dayId;
        this.time = time;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDayId() {
        return dayId;
    }

    public void setDayId(String dayId) {
        this.dayId = dayId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

