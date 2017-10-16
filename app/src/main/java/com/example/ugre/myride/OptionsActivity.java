package com.example.ugre.myride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ugre.myride.custom_services.FirebaseNotificationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorage;

    private ProgressDialog progressDialog;

    private String TAG = "OptionsActivity:";
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    Log.d(TAG, "User signed out!");
                    progressDialog.dismiss();
                    stopService(new Intent(OptionsActivity.this, FirebaseNotificationService.class));
                    startActivity(new Intent(OptionsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        mStorage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user.getUid() + ".jpg");

        Button cameraBtn = (Button) findViewById(R.id.upload_camera);
        Button galleryBtn = (Button) findViewById(R.id.upload_gallery);
        Button signOutBtn = (Button) findViewById(R.id.signOutBtn);

        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        signOutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.upload_camera:
                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "ProfilePicture");
                imagesFolder.mkdirs();

                File image = new File(imagesFolder, "ProfPic.png");
                fileUri = Uri.fromFile(image);

                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                startActivityForResult(imageIntent, 0);
                break;
            case R.id.upload_gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(pickPhoto , 1);
                break;
            case R.id.signOutBtn:
                progressDialog.setMessage(getString(R.string.signout_dialog));
                progressDialog.show();
                mAuth.signOut();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 || requestCode == 1)
        {
            if (resultCode == RESULT_OK) {

                if (data != null) {
                    progressDialog.setMessage(getString(R.string.upload_dialog));
                    progressDialog.show();

                    fileUri = data.getData();

                    mStorage.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(OptionsActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(OptionsActivity.this, "Failed to upload picture, please try again!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
            else {
                Toast.makeText(this, "Action canceled!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
