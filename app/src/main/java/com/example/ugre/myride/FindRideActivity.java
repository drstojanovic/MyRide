package com.example.ugre.myride;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.ugre.myride.custom_adapters.RideListAdapter;
import com.example.ugre.myride.custom_classes.Ride;
import com.example.ugre.myride.custom_classes.RideInfo;
import com.example.ugre.myride.custom_services.FirebaseNotificationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class FindRideActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Calendar calendar;
    private RideListAdapter adapter;
    private ArrayList<Ride> allRidesList, myRidesList;

    private EditText dateET, timeET, costET, seatsET;
    private AutoCompleteTextView fromET, toET;
    private TextView nothingFoundTV;
    private CheckBox pets, smoking;
    private ProgressBar progressBar;
    private AlertDialog.Builder alertDialog;
    private ListView listView;
    private Spinner timeSpinner;

    private int day, month, year, hour, minute, plus_minus;
    private Ride searchedRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_ride);

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

        nothingFoundTV = (TextView) findViewById(R.id.nothingFoundTV);
        nothingFoundTV.setText(Html.fromHtml(getString(R.string.nothing_found)));

        pets = (CheckBox) findViewById(R.id.pets);
        smoking = (CheckBox) findViewById(R.id.smoking);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        day = month = year = hour = minute = -1;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        allRidesList = new ArrayList<>();
        myRidesList = new ArrayList<>();

        listView = (ListView) findViewById(R.id.rideList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FindRideActivity.this, RideInfo.class);
                intent.putExtra("user_id", myRidesList.get(i).getUid());
                intent.putExtra("ride_id", myRidesList.get(i).getId());
                startActivity(intent);
            }
        });

        String[] cites = getResources().getStringArray(R.array.cites_array);
        ArrayAdapter<String> citiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cites);
        fromET.setAdapter(citiesAdapter);
        toET.setAdapter(citiesAdapter);

        Button findBtn = (Button) findViewById(R.id.findBtn);
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String from = fromET.getText().toString();
                String to = toET.getText().toString();
                String cost = costET.getText().toString();
                String seats = seatsET.getText().toString();

                if (from.isEmpty() || to.isEmpty() || year==-1 || month==-1 || day==-1 || hour==-1 || minute==-1)
                    Toast.makeText(FindRideActivity.this, R.string.error_enter, Toast.LENGTH_SHORT).show();
                else {
                    int costNum = 1000000, seatsNum = 1;

                    if (!cost.isEmpty())
                        costNum = Integer.parseInt(cost);
                    if (!seats.isEmpty())
                        seatsNum = Integer.parseInt(seats);

                    String formatedDate = day + "/" + month + "/" + year;
                    String formatedTime = hour + ":" + minute;

                    filterRides(from, to, formatedDate, formatedTime, costNum, seatsNum, pets.isChecked(), smoking.isChecked());

                    if (myRidesList.size() > 0) {
                        nothingFoundTV.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        adapter = new RideListAdapter(myRidesList, FindRideActivity.this);
                        listView.setAdapter(adapter);
                    }
                    else {
                        listView.setVisibility(View.INVISIBLE);
                        nothingFoundTV.setVisibility(View.VISIBLE);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });

        mDatabase.child("rides").orderByChild("time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                ride.setId(dataSnapshot.getKey());
                if (isDateExpired(ride.getDate())) {
                    dataSnapshot.getRef().removeValue();
                    mDatabase.child("ride_members").child(dataSnapshot.getKey()).removeValue();
                }
                else
                    allRidesList.add(ride);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                ride.setId(dataSnapshot.getKey());
                int old_index = allRidesList.indexOf(ride);
                allRidesList.set(old_index, ride);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                ride.setId(dataSnapshot.getKey());
                allRidesList.remove(ride);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        FirebaseNotificationService.getInstance().startRideSearch(searchedRide);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        FirebaseNotificationService.getInstance().stopRideSearch();
                        break;
                }
            }
        };

        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Are you sure?").setPositiveButton("Yes", dialogOnClickListener)
                .setNegativeButton("No", dialogOnClickListener);
        nothingFoundTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });

        timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
        timeSpinner.setOnItemSelectedListener(this);
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
        timeET.setText(i + ":" + i1);
        hour = i;
        minute = i1;
    }

    private void filterRides(String from, String to, String date, String time, int cost_max, int seats_min, boolean pets, boolean smoking)
    {
        searchedRide = new Ride(from,to,date,time,cost_max,seats_min,pets,smoking,mUser.getUid());

        myRidesList.clear();
        for (int i = 0; i < allRidesList.size(); i++)
        {
            Ride ride = allRidesList.get(i);

            // skip if ride is mine
            if (ride.getUid().equalsIgnoreCase(mUser.getUid()))
                continue;

            boolean fromB = ride.getFrom().equalsIgnoreCase(from);
            boolean toB = ride.getTo().equalsIgnoreCase(to);
            boolean dateB = ride.getDate().equalsIgnoreCase(date);
            boolean petB = ride.isPets() == pets;
            boolean smokeB = ride.isSmoking() == smoking;

            String[] strings = ride.getTime().split(":");
            int ride_hour = Integer.parseInt(strings[0]);
            boolean timeB = Math.abs(ride_hour - hour) <= plus_minus;

            if (fromB && toB && dateB && timeB && ride.getCost() <= cost_max && ride.getSeats()-ride.getSeats_taken() >= seats_min && petB && smokeB)
                myRidesList.add(ride);
        }
    }

    private boolean isDateExpired(String ride_date)
    {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            Date date_ride = format.parse(ride_date);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -2);
            Date date_expired = calendar.getTime();

            return (date_ride.compareTo(date_expired) < 0);
        }
        catch (ParseException e)
        {
            Log.e("FindRideActivity", e.getMessage());
            return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        plus_minus = Integer.parseInt(adapterView.getItemAtPosition(i).toString().substring(0,1));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
