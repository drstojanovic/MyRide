package com.example.ugre.myride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.ugre.myride.custom_adapters.RideRecyclerAdapter;
import com.example.ugre.myride.custom_classes.Ride;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRidesActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private DatabaseReference dbRides, dbRideMembers;
    private ChildEventListener rideListener;

    private RecyclerView joinedRidesRV, offeredRidesRV;
    private RecyclerView.Adapter joinedRidesAdapter, offeredRidesAdapter;
    private RecyclerView.LayoutManager joinedLayoutManager, offeredLayoutManager;

    private ArrayList<Ride> joinedRidesList, offeredRideList, tempRideList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        dbRides = FirebaseDatabase.getInstance().getReference("rides");
        dbRideMembers = FirebaseDatabase.getInstance().getReference("ride_members");

        joinedRidesRV = (RecyclerView) findViewById(R.id.joinedRidesRV);
        offeredRidesRV = (RecyclerView) findViewById(R.id.offeredRidesRV);

        joinedRidesRV.setHasFixedSize(true);
        offeredRidesRV.setHasFixedSize(true);

        joinedLayoutManager = new LinearLayoutManager(this);
        offeredLayoutManager = new LinearLayoutManager(this);

        joinedRidesRV.setLayoutManager(joinedLayoutManager);
        offeredRidesRV.setLayoutManager(offeredLayoutManager);

        joinedRidesList = new ArrayList<>();
        offeredRideList = new ArrayList<>();
        tempRideList = new ArrayList<>();

        joinedRidesAdapter = new RideRecyclerAdapter(joinedRidesList, this);
        offeredRidesAdapter = new RideRecyclerAdapter(offeredRideList, this);

        joinedRidesRV.setAdapter(joinedRidesAdapter);
        offeredRidesRV.setAdapter(offeredRidesAdapter);

        // TODO stavi neki progressBar

        rideListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                ride.setId(dataSnapshot.getKey());

                if (ride.getUid().equalsIgnoreCase(mUser.getUid())) {
                    offeredRideList.add(ride);
                    offeredRidesAdapter.notifyDataSetChanged();
                }
                else {
                    tempRideList.add(ride);
                    dbRideMembers.child(ride.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(mUser.getUid())) {
                                Ride ride = new Ride();
                                ride.setId(dataSnapshot.getKey());
                                joinedRidesList.add(tempRideList.remove(tempRideList.indexOf(ride)));
                                joinedRidesAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbRides.orderByChild("date").addChildEventListener(rideListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (rideListener != null)
            dbRides.removeEventListener(rideListener);

        joinedRidesList.clear();
        offeredRideList.clear();
        tempRideList.clear();
    }
}
