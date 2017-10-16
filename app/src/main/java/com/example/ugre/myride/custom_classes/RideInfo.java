package com.example.ugre.myride.custom_classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ugre.myride.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import me.grantland.widget.AutofitTextView;

public class RideInfo extends AppCompatActivity implements View.OnClickListener{

    private StorageReference mStorage;
    private DatabaseReference usersDB, membersDB;
    private FirebaseUser mUser;

    private File localFileProfileImage;

    private ImageView user_avatar, pets, smoking;
    private TextView user_name, user_age, user_rank, ride_from, ride_to, ride_date, ride_time, ride_cost, ride_seats;
    private Button requestBtn;
    private AutofitTextView messageTV;
    private ConstraintLayout ratingCL;
    private RatingBar ratingBar;

    private String user_id, ride_id;
    private double rank;
    private int seats_available;
    private int mod, new_mod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_info);

        /*DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*0.8),(int) (height*0.7));*/

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        usersDB = FirebaseDatabase.getInstance().getReference("users");
        membersDB = FirebaseDatabase.getInstance().getReference("ride_members");

        user_avatar = (ImageView) findViewById(R.id.user_avatar);
        user_name = (TextView) findViewById(R.id.user_name);
        user_age = (TextView) findViewById(R.id.user_age);
        user_rank = (TextView) findViewById(R.id.user_rank);

        ride_from = (TextView) findViewById(R.id.ride_from);
        ride_to = (TextView) findViewById(R.id.ride_to);
        ride_date = (TextView) findViewById(R.id.ride_date);
        ride_time = (TextView) findViewById(R.id.ride_time);
        ride_cost = (TextView) findViewById(R.id.ride_cost);
        ride_seats = (TextView) findViewById(R.id.ride_seats);
        messageTV = (AutofitTextView) findViewById(R.id.message);

        ratingCL = (ConstraintLayout) findViewById(R.id.ratingCL);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        requestBtn = (Button) findViewById(R.id.requestBtn);
        Button closeBtn = (Button) findViewById(R.id.closeBtn);
        requestBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);

        pets = (ImageView) findViewById(R.id.pets);
        smoking = (ImageView) findViewById(R.id.smoking);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user_id = extras.getString("user_id"); // user koji nudi voznju
            ride_id = extras.getString("ride_id");

            displayUserInfo();
            displayRideInfo();
        }
        else
            finish();
    }

    private void displayUserInfo()
    {
        try {
            localFileProfileImage = File.createTempFile("profileImage",".jpg");

            mStorage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user_id + ".jpg");
            mStorage.getFile(localFileProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        user_avatar.setImageBitmap(BitmapFactory.decodeFile(localFileProfileImage.getAbsolutePath(), options));
                    }
                    catch (OutOfMemoryError e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        usersDB.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user_name.setText(user.getName());
                rank = user.getRank();
                user_rank.setText("Stars: " + String.valueOf(rank));
                user_age.setText("Age: " + String.valueOf(user.getAge()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if (rank == 0)
                    rank = v;
                else
                    rank = (double)Math.round((rank+v)/2 * 100) / 100d;
                FirebaseDatabase.getInstance().getReference("users").child(user_id).child("rank").setValue(rank);
                membersDB.child(ride_id).child(mUser.getUid()).setValue(new_mod);
            }
        });
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
                seats_available = ride.getSeats() - ride.getSeats_taken();

                if (!ride.isPets())
                    pets.setVisibility(View.INVISIBLE);
                if (!ride.isSmoking())
                    smoking.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        membersDB.child(ride_id).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                {
                    mod = 0;
                }
                else {
                    mod = dataSnapshot.getValue(Integer.class);
                }
                displayButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayButtons()
    {
        requestBtn.setVisibility(View.INVISIBLE);
        messageTV.setVisibility(View.VISIBLE);
        ratingCL.setVisibility(View.GONE);
        switch (mod)
        {
            case 0:
                new_mod = 1;
                messageTV.setVisibility(View.INVISIBLE);
                requestBtn.setText(getString(R.string.join));
                requestBtn.setVisibility(View.VISIBLE);
                break;
            case 1:
                messageTV.setTextColor(getResources().getColor(R.color.yellow));
                messageTV.setText(getString(R.string.message_requested));
                break;
            case 2:
                new_mod = 4;
                messageTV.setTextColor(getResources().getColor(R.color.green));
                messageTV.setText(getString(R.string.message_accepted));
                ratingCL.setVisibility(View.VISIBLE);
                break;
            case 3:
                messageTV.setTextColor(getResources().getColor(R.color.red));
                messageTV.setText(getString(R.string.message_declined));
                break;
            case 5:
                new_mod = 6;
                messageTV.setTextColor(getResources().getColor(R.color.green));
                messageTV.setText(getString(R.string.message_accepted));
                ratingCL.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.requestBtn:

                if (seats_available > 0) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ride_members");
                    mDatabase.child(ride_id).child(mUser.getUid()).setValue(1);
                    Notification.sendNotification(user_id, ride_id, user_id, 1);
                }
                else
                    Toast.makeText(this, getString(R.string.message_full), Toast.LENGTH_SHORT).show();
                // TODO Ne uzimas u obzir ako mu treba vise od jednog mesta
                break;

            case R.id.closeBtn:
                finish();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        user_avatar.setImageBitmap(null);
        localFileProfileImage.delete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}
