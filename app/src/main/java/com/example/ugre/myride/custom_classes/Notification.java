package com.example.ugre.myride.custom_classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ugre9 on 20/09/2017.
 */

public class Notification {

    private String ride_id, user_id;
    private int type;
    /*
        1 - Someone requested to ride with you;
        2 - You have been accepted to ride;
        3- You have been declined to ride;
        4 - We found you the ride you are looking for;
     */

    public Notification(){}

    public Notification(String id, int t)
    {
        this.ride_id = id;
        this.type = t;
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public static void sendNotification(String user_id, String ride_id, String driver_id, int type){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications").child(user_id);

        Notification notification = new Notification(ride_id, type);
        if (!driver_id.equalsIgnoreCase(""))
            notification.setUser_id(driver_id);

        databaseReference.push().setValue(notification);
    }
}
