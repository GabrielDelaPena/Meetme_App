package be.ehb.meetme.models;

import java.util.ArrayList;

public class Meetup {
    Double lat, lon;
    String sender, receiver, id, date, description, location;

    public Meetup() {
    }

    public Meetup(Double lat, Double lon, String sender, String receiver, String id, String date, String description, String location) {
        this.lat = lat;
        this.lon = lon;
        this.sender = sender;
        this.receiver = receiver;
        this.id = id;
        this.date = date;
        this.description = description;
        this.location = location;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }
}
