package com.example.ugre.myride.custom_adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ugre.myride.R;
import com.example.ugre.myride.custom_classes.MyRideInfo;
import com.example.ugre.myride.custom_classes.Ride;
import com.example.ugre.myride.custom_classes.RideInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Created by ugre9 on 10/09/2017.
 */

public class RideRecyclerAdapter extends RecyclerView.Adapter<RideRecyclerAdapter.RideViewHolder>{

    private ArrayList<Ride> mRides;
    private Activity mActivity;
    private FirebaseUser mUser;

    public RideRecyclerAdapter(ArrayList<Ride> myDataSet, Activity activity)
    {
        mRides = myDataSet;
        mActivity = activity;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public RideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_recycle_row, parent, false);

        // set the view's size, margins, paddings and layout parameters...


        return new RideViewHolder(cl);
    }

    @Override
    public void onBindViewHolder(RideViewHolder holder, int position) {

        final Ride ride = mRides.get(position);

        holder.fromTV.setText(ride.getFrom());
        holder.toTV.setText(ride.getTo());
        holder.dateTV.setText(ride.getDate());
        holder.timeTV.setText(ride.getTime());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ride.getUid().equalsIgnoreCase(mUser.getUid()))
                {
                    Intent intent = new Intent(mActivity, MyRideInfo.class);
                    intent.putExtra("ride_id", ride.getId());
                    mActivity.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(mActivity, RideInfo.class);
                    intent.putExtra("user_id", ride.getUid());
                    intent.putExtra("ride_id", ride.getId());
                    mActivity.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRides.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {

        public TextView fromTV;
        public TextView toTV;
        public TextView dateTV;
        public TextView timeTV;

        public RideViewHolder(View itemView) {
            super(itemView);

            fromTV = (TextView) itemView.findViewById(R.id.from);
            toTV = (TextView) itemView.findViewById(R.id.to);
            dateTV = (TextView) itemView.findViewById(R.id.date);
            timeTV = (TextView) itemView.findViewById(R.id.time);
        }
    }
}
