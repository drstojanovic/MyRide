package com.example.ugre.myride.custom_classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ugre.myride.R;
import com.example.ugre.myride.custom_adapters.MemberRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MyRideInfo extends AppCompatActivity {

    private DatabaseReference rideMembersDB, usersDB;
    private StorageReference mStorage;
    private ValueEventListener membersListener;

    private RecyclerView membersRV;
    private RecyclerView.Adapter membersAdapter;
    private RecyclerView.LayoutManager membersLayoutManager;

    private ImageView pets, smoking;
    private TextView ride_from, ride_to, ride_date, ride_time, ride_cost, ride_seats;

    private String ride_id;
    private boolean ride_full = false;
    private int ride_seats_taken;
    private ArrayList<User> membersList;

    private BitmapFactory.Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ride_info);

        options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        ride_from = (TextView) findViewById(R.id.ride_from);
        ride_to = (TextView) findViewById(R.id.ride_to);
        ride_date = (TextView) findViewById(R.id.ride_date);
        ride_time = (TextView) findViewById(R.id.ride_time);
        ride_cost = (TextView) findViewById(R.id.ride_cost);
        ride_seats = (TextView) findViewById(R.id.ride_seats);

        pets = (ImageView) findViewById(R.id.pets);
        smoking = (ImageView) findViewById(R.id.smoking);

        membersRV = (RecyclerView) findViewById(R.id.membersRV);

        Button closeBtn = (Button) findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ride_id = extras.getString("ride_id");
            rideMembersDB = FirebaseDatabase.getInstance().getReference("ride_members").child(ride_id);
            displayRideInfo();
        }
        else
            finish();

        usersDB = FirebaseDatabase.getInstance().getReference("users");

        membersLayoutManager = new LinearLayoutManager(this);
        membersRV.setLayoutManager(membersLayoutManager);
        membersList = new ArrayList<>();

        membersAdapter = new MemberRecyclerAdapter(membersList, ride_id, ride_seats_taken);
        membersRV.setAdapter(membersAdapter);

        membersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                membersList.clear();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String user_id = postSnapshot.getKey();
                    int value = postSnapshot.getValue(Integer.class);

                    // ako je user deciline
                    if (value == 3)
                        continue;

                    if (ride_full && value == 1)
                    {
                        rideMembersDB.child(user_id).setValue(3);
                        break;
                    }

                    final User new_user = new User();
                    new_user.setUid(user_id);
                    new_user.setRide_member(value);

                    usersDB.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            new_user.setName(user.getName());
                            new_user.setBirthday(user.getBirthday());
                            new_user.setRank(user.getRank());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mStorage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user_id + ".jpg");

                    mStorage.getBytes(5 * 1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            try {
                                new_user.setPhoto(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options));
                            }
                            catch (OutOfMemoryError e)
                            {
                                new_user.setPhoto(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar));
                            }
                            if (membersList.indexOf(new_user) < 0) {
                                membersList.add(new_user);
                                membersAdapter.notifyDataSetChanged();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //user exists, but doesn't have profile photo
                            new_user.setPhoto(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar));
                            if (membersList.indexOf(new_user) < 0) {
                                membersList.add(new_user);
                                membersAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

                if (membersList.size() == 0)
                    membersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        rideMembersDB.addValueEventListener(membersListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (membersListener != null)
            rideMembersDB.removeEventListener(membersListener);
        membersList.clear();
    }

    private void displayRideInfo()
    {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("rides");
        mDatabase.child(ride_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Ride ride = dataSnapshot.getValue(Ride.class);

                ride_from.setText(ride.getFrom());
                ride_to.setText(ride.getTo());
                ride_date.setText(ride.getDate());
                ride_time.setText(ride.getTime());
                ride_cost.setText(String.valueOf(ride.getCost()));
                ride_seats.setText(String.valueOf(ride.getSeats_taken()) + "/" +String.valueOf(ride.getSeats()));
                ride_seats_taken = ride.getSeats_taken();

                if (ride.getSeats_taken() >= ride.getSeats())
                    ride_full = true;

                if (!ride.isPets())
                    pets.setVisibility(View.INVISIBLE);
                if (!ride.isSmoking())
                    smoking.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}
