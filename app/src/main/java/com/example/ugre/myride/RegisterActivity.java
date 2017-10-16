package com.example.ugre.myride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ugre.myride.custom_classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private String TAG = "RegisterActivity:";
    private String email, name, age, password;

    private EditText emailET, nameET, ageET, passET;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "User signed in: " + user.getUid());
                    finish();
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        emailET = (EditText) findViewById(R.id.email);
        nameET = (EditText) findViewById(R.id.name);
        ageET = (EditText) findViewById(R.id.age);
        passET = (EditText) findViewById(R.id.password);

        Button registerBtn = (Button) findViewById(R.id.registerBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.register_dialog));

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailET.getText().toString();
                name = nameET.getText().toString();
                age = ageET.getText().toString();
                password = passET.getText().toString();

                if (email.isEmpty() || name.isEmpty() || age.isEmpty() || password.isEmpty())
                    Toast.makeText(RegisterActivity.this, R.string.register_error_empty, Toast.LENGTH_SHORT).show();
                else if (!isValidDate(age))
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_birthday), Toast.LENGTH_SHORT).show();
                else if( password.length() < 6)
                    Toast.makeText(RegisterActivity.this, R.string.error_password, Toast.LENGTH_SHORT).show();
                else {
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        addUserInDatabase(task.getResult().getUser().getUid(), name, age);
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                }
            }
        });
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

    private void addUserInDatabase(String user_id, String name, String age)
    {
        User newUser = new User(name, age);
        mDatabase.child(user_id).setValue(newUser);
    }

    private static boolean isValidDate(String input) {
        boolean valid = false;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String output = dateFormat.parse(input).toString();
            valid = true;
        } catch (Exception ignore) {}

        return valid;
    }
}
