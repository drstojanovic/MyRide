package com.example.ugre.myride.custom_adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ugre.myride.R;
import com.example.ugre.myride.custom_classes.Ride;
import com.example.ugre.myride.custom_classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ugre9 on 03/09/2017.
 */

public class RideListAdapter extends ArrayAdapter<Ride>{

    private ArrayList<Ride> ridesSet;
    private ViewHolder viewHolder;
    private Context context;

    private static class ViewHolder {
        TextView tvTime;
        TextView tvCost;
        TextView tvSeats;
        ImageView imPets;
        ImageView imSmoke;
    }

    public RideListAdapter(ArrayList<Ride> dataSet, Context context) {
        super(context, R.layout.ride_item, dataSet);
        this.context = context;
        this.ridesSet = dataSet;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.ride_item, parent, false);

            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.time);
            viewHolder.tvCost = (TextView) convertView.findViewById(R.id.cost);
            viewHolder.tvSeats = (TextView) convertView.findViewById(R.id.seats);
            viewHolder.imPets = (ImageView) convertView.findViewById(R.id.pets);
            viewHolder.imSmoke = (ImageView) convertView.findViewById(R.id.smoking);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Ride ride = ridesSet.get(position);

        viewHolder.tvTime.setText(ride.getTime());
        viewHolder.tvCost.setText(String.valueOf(ride.getCost()));
        viewHolder.tvSeats.setText(String.valueOf(ride.getSeats_taken()) + "/" + String.valueOf(ride.getSeats()));

        if (!ride.isPets())
            viewHolder.imPets.setVisibility(View.INVISIBLE);
        if (!ride.isSmoking())
            viewHolder.imSmoke.setVisibility(View.INVISIBLE);

        // Return the completed view to render on screen
        return convertView;
    }
}
