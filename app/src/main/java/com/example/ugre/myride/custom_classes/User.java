package com.example.ugre.myride.custom_classes;

import android.graphics.Bitmap;

import java.security.PublicKey;
import java.util.Calendar;

/**
 * Created by ugre9 on 03/09/2017.
 */

public class User {

    private String uid;
    private String name;
    private String birthday;
    private double rank;
    private int ride_member;
    /*
        1 - prijavio se;
        2 - prihvacen i niko nije ocenio jos;
        3 - odbijen;
        4 - user je ocenio samo;
        5 - driver je ocenio samo;
        6 - obojica su ocenili;
     */
    private Bitmap photo;

    public User(){}

    public User(String p_name, String p_birthday)
    {
        name = p_name;
        birthday = p_birthday;
        rank = 0;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public int getRide_member() {
        return ride_member;
    }

    public void setRide_member(int ride_member) {
        this.ride_member = ride_member;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User user = (User) obj;

        return uid.equalsIgnoreCase(user.uid);
    }

    public int getAge(){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        String[] strings = this.birthday.split("/");
        int day = Integer.parseInt(strings[0]);
        int month = Integer.parseInt(strings[1]);
        int year = Integer.parseInt(strings[2]);

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        return age;
    }
}
