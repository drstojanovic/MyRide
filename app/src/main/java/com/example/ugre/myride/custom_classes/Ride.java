package com.example.ugre.myride.custom_classes;

/**
 * Created by ugre9 on 03/09/2017.
 */

public class Ride {

    private String from, to;
    private String date, time;
    private int cost, seats, seats_taken;
    private boolean pets, smoking;
    private String uid, id;

    public Ride(){}

    public Ride(String from, String to, String date, String time, int cost, int seats, boolean pets, boolean smoking, String uid) {
        this.from = from;
        this.to = to;
        this.date = date;
        this.time = time;
        this.cost = cost;
        this.seats = seats;
        this.seats_taken = 0;
        this.pets = pets;
        this.smoking = smoking;
        this.uid = uid;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public boolean isPets() {
        return pets;
    }

    public void setPets(boolean pets) {
        this.pets = pets;
    }

    public boolean isSmoking() {
        return smoking;
    }

    public void setSmoking(boolean smoking) {
        this.smoking = smoking;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSeats_taken() {
        return seats_taken;
    }

    public void setSeats_taken(int seats_taken) {
        this.seats_taken = seats_taken;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Ride)) {
            return false;
        }
        Ride ride = (Ride) obj;

        return id.equalsIgnoreCase(ride.id);
    }
}
