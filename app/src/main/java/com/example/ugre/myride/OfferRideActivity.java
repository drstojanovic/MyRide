package com.example.ugre.myride;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.ugre.myride.custom_classes.Ride;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OfferRideActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{

    private DatabaseReference mDatabase;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private ProgressDialog progressDialog;
    private Calendar calendar;

    private EditText dateET, timeET, costET, seatsET;
    private AutoCompleteTextView fromET, toET;
    private CheckBox pets, smoking;
    private int day, month, year, hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_ride);

        mDatabase = FirebaseDatabase.getInstance().getReference("rides");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.offer_dialog));

        calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dateET = (EditText) findViewById(R.id.date);
        dateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        timePickerDialog = new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);

        timeET = (EditText) findViewById(R.id.time);
        timeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.show();
            }
        });

        fromET = (AutoCompleteTextView) findViewById(R.id.from);
        toET = (AutoCompleteTextView) findViewById(R.id.to);
        costET = (EditText) findViewById(R.id.cost);
        seatsET = (EditText) findViewById(R.id.seats);

        pets = (CheckBox) findViewById(R.id.pets);
        smoking = (CheckBox) findViewById(R.id.smoking);

        day = month = year = hour = minute = -1;

        Button offerBtn = (Button) findViewById(R.id.offerBtn);
        offerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String from = fromET.getText().toString();
                String to = toET.getText().toString();
                String cost = costET.getText().toString();
                String seats = seatsET.getText().toString();

                if (from.isEmpty() || to.isEmpty() || cost.isEmpty() || seats.isEmpty() || year==-1 || month==-1 || day==-1 || hour==-1 || minute==-1)
                    Toast.makeText(OfferRideActivity.this, R.string.error_enter, Toast.LENGTH_SHORT).show();
                else {
                    progressDialog.show();
                    int costNum = Integer.parseInt(cost);
                    int seatsNum = Integer.parseInt(seats);
                    String formatedDate = day + "/" + month + "/" + year;
                    String formatedTime = hour + ":" + minute;

                    Ride ride = new Ride(from, to, formatedDate, formatedTime, costNum, seatsNum, pets.isChecked(), smoking.isChecked(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                    addRideInDatabase(ride);
                    progressDialog.dismiss();
                    Toast.makeText(OfferRideActivity.this, R.string.offer_successful, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OfferRideActivity.this, MyRidesActivity.class));
                    finish();
                }
            }
        });

        String[] cites = getResources().getStringArray(R.array.cites_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cites);
        fromET.setAdapter(adapter);
        toET.setAdapter(adapter);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        if ((i > calendar.get(Calendar.YEAR)) || (i == calendar.get(Calendar.YEAR) && i1 > calendar.get(Calendar.MONTH)) || (i == calendar.get(Calendar.YEAR) && i1 == calendar.get(Calendar.MONTH) && i2 > calendar.get(Calendar.DAY_OF_MONTH))) {
            dateET.setText(i2+"/"+(i1+1)+"/"+i);
            year = i;
            month = i1+1;
            day = i2;
        }
        else {
            Toast.makeText(this, R.string.error_date, Toast.LENGTH_SHORT).show();
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        // TODO Trenutno mora minimum sutradan da bude datum!
        if (true) {
            timeET.setText(i + ":" + i1);
            hour = i;
            minute = i1;
        }
        else{
            Toast.makeText(this, R.string.error_time, Toast.LENGTH_SHORT).show();
        }
    }

    private void addRideInDatabase(Ride newRide)
    {
        mDatabase.push().setValue(newRide);
    }
}
