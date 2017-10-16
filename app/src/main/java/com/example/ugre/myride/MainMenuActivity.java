package com.example.ugre.myride;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ugre.myride.custom_classes.User;
import com.example.ugre.myride.custom_services.FirebaseNotificationService;
import com.google.android.gms.tasks.OnFailureListener;
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

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private String TAG = "MainMenuActivity:";

    private ImageView user_avatar;
    private TextView user_name, user_rank;

    private File localFileProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        Button findRideBtn = (Button) findViewById(R.id.findRideBtn);
        Button offerRideBtn = (Button) findViewById(R.id.offerRideBtn);
        Button myRidesbtn = (Button) findViewById(R.id.myRidesBtn);
        Button optionsBtn = (Button) findViewById(R.id.optionsBtn);

        findRideBtn.setOnClickListener(this);
        offerRideBtn.setOnClickListener(this);
        myRidesbtn.setOnClickListener(this);
        optionsBtn.setOnClickListener(this);

        user_avatar = (ImageView) findViewById(R.id.user_avatar);
        user_name = (TextView) findViewById(R.id.user_name);
        user_rank = (TextView) findViewById(R.id.user_rank);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    Log.d(TAG, "User signed out!");
                    startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    displayUserInfo(user.getUid());
                    displayProfilePicture();
                    startService(new Intent(MainMenuActivity.this, FirebaseNotificationService.class));
                }
            }
        };
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.findRideBtn:
                startActivity(new Intent(MainMenuActivity.this, FindRideActivity.class));
                break;
            case R.id.offerRideBtn:
                startActivity(new Intent(MainMenuActivity.this, OfferRideActivity.class));
                break;
            case R.id.myRidesBtn:
                startActivity(new Intent(MainMenuActivity.this, MyRidesActivity.class));
                break;
            case R.id.optionsBtn:
                startActivity(new Intent(MainMenuActivity.this, OptionsActivity.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
        user_avatar.setImageBitmap(null);
        localFileProfileImage.delete();
    }

    private void displayUserInfo(String uid)
    {
        mDatabase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user_name.setText(user.getName());
                user_rank.setText(getString(R.string.rank) + " " + String.valueOf(user.getRank()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void displayProfilePicture()
    {
        try {
            localFileProfileImage = File.createTempFile("profileImage",".jpg");

            mStorage = FirebaseStorage.getInstance().getReference().child("profile_images/" + mAuth.getCurrentUser().getUid() + ".jpg");
            mStorage.getFile(localFileProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        user_avatar.setImageBitmap(BitmapFactory.decodeFile(localFileProfileImage.getAbsolutePath(), options));
                    }catch (OutOfMemoryError e)
                    {
                        user_avatar.setImageDrawable(getResources().getDrawable(R.drawable.default_avatar));
                        e.printStackTrace();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    user_avatar.setImageDrawable(getResources().getDrawable(R.drawable.default_avatar));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
