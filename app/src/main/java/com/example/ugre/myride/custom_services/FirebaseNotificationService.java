package com.example.ugre.myride.custom_services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ugre.myride.MainMenuActivity;
import com.example.ugre.myride.R;
import com.example.ugre.myride.custom_classes.MyRideInfo;
import com.example.ugre.myride.custom_classes.Notification;
import com.example.ugre.myride.custom_classes.Ride;
import com.example.ugre.myride.custom_classes.RideInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

/**
 * Created by ugre9 on 20/09/2017.
 */

public class FirebaseNotificationService extends Service {

    private FirebaseDatabase mDatabase;
    private ChildEventListener ridesListener;
    private FirebaseAuth mAuth;
    Context context;
    static FirebaseNotificationService sInstance;

    private Ride mRide;

    static String TAG = "FirebaseService";

    public FirebaseNotificationService(){}

    public static FirebaseNotificationService getInstance()
    {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        sInstance = this;

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    stopSelf();
                    stopRideSearch();
                }
            }
        });
        Log.d(TAG, "onCreate called");
        setupNotificationListener();

        ridesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (mRide == null)
                    return;
                else if (isDateExpired()) {
                    stopRideSearch();
                    return;
                }

                Ride ride = dataSnapshot.getValue(Ride.class);
                ride.setId(dataSnapshot.getKey());
                compareRide(ride);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (mRide == null)
                    return;

                Ride ride = dataSnapshot.getValue(Ride.class);
                ride.setId(dataSnapshot.getKey());
                compareRide(ride);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupNotificationListener()
    {
        mDatabase.getReference().child("notifications")
                .child(mAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot != null){
                            Notification notification = dataSnapshot.getValue(Notification.class);

                            showNotification(context,notification,dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void showNotification(Context context, Notification notification, String notification_key){
        String description = "";

        Intent backIntent = new Intent(context, MainMenuActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intent = new Intent(context, RideInfo.class);

        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainMenuActivity.class);*/

        switch (notification.getType())
        {
            case 1: // Requested
                description = getString(R.string.notification_requested);
                intent = new Intent(context, MyRideInfo.class);
                break;
            case 2: // Accepted
                description = getString(R.string.notification_accepted);
                intent.putExtra("user_id", notification.getUser_id());
                break;
            case 3: // Declined
                description = getString(R.string.notification_declined);
                intent.putExtra("user_id", notification.getUser_id());
                break;
            case 4: // Ride found
                description = getString(R.string.notification_ride_found);
                intent.putExtra("user_id", notification.getUser_id());
                break;
        }

        intent.putExtra("ride_id", notification.getRide_id());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Ride")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText(description)
                .setAutoCancel(true);

        final PendingIntent pendingIntent = PendingIntent.getActivities(context, 900,
                new Intent[] {intent}, PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =  (NotificationManager)context. getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notification.getType(), mBuilder.build());

        deleteNotification(notification_key);
    }

    private void deleteNotification(String notification_key) {
        mDatabase.getReference().child("notifications")
                .child(mAuth.getCurrentUser().getUid())
                .child(notification_key)
                .removeValue();
    }

    public void startRideSearch(Ride ride)
    {
        Log.d(TAG, "startRideSearch called");
        mRide = ride;
        mDatabase.getReference().child("rides").addChildEventListener(ridesListener);
    }

    public void stopRideSearch()
    {
        Log.d(TAG, "stopRideSearch called");
        mRide = null;
        mDatabase.getReference().child("rides").removeEventListener(ridesListener);
    }

    private void compareRide(Ride ride)
    {
        boolean fromB = ride.getFrom().equalsIgnoreCase(mRide.getFrom());
        boolean toB = ride.getTo().equalsIgnoreCase(mRide.getTo());
        boolean dateB = ride.getDate().equalsIgnoreCase(mRide.getDate());
        boolean petB = ride.isPets() == mRide.isPets();
        boolean smokeB = ride.isSmoking() == mRide.isSmoking();

        int hour = Integer.parseInt(mRide.getTime().split(":")[0]);
        String[] strings = ride.getTime().split(":");
        int ride_hour = Integer.parseInt(strings[0]);
        boolean timeB = Math.abs(ride_hour - hour) <= 1;

        if (fromB && toB && dateB && timeB && ride.getCost() <= mRide.getCost() && ride.getSeats()-ride.getSeats_taken() >= mRide.getSeats() && petB && smokeB)
        {
            Notification notification = new Notification(ride.getId(), 4);
            notification.setUser_id(ride.getUid());
            showNotification(context, notification, "");
            stopRideSearch();
        }
    }

    private boolean isDateExpired()
    {
        String[] strings = mRide.getDate().split("/");
        Calendar calendar = Calendar.getInstance();

        int year = Integer.parseInt(strings[2]);
        int month = Integer.parseInt(strings[1]);
        int day = Integer.parseInt(strings[0]);

        int yearNow = calendar.get(Calendar.YEAR);
        int monthNow = calendar.get(Calendar.MONTH);
        monthNow++;
        int dayNow = calendar.get(Calendar.DAY_OF_MONTH);

        boolean yearB = year < yearNow;
        boolean monthB = year <= yearNow && month < monthNow;
        boolean dayB = year <= yearNow && month <= monthNow && day < dayNow;

        return yearB || monthB || dayB;
    }
}
