package com.example.ugre.myride.custom_adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ugre.myride.R;
import com.example.ugre.myride.custom_classes.Notification;
import com.example.ugre.myride.custom_classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by ugre9 on 13/09/2017.
 */

public class MemberRecyclerAdapter extends RecyclerView.Adapter<MemberRecyclerAdapter.MemberViewHolder>{

    private ArrayList<User> usersList;
    private String ride_id;
    private int ride_seats_taken;
    private DatabaseReference rideMembersDB, usersDB;

    public MemberRecyclerAdapter(ArrayList<User> users, String ride, int seats_taken)
    {
        usersList = users;
        ride_id = ride;
        ride_seats_taken = seats_taken;
        rideMembersDB = FirebaseDatabase.getInstance().getReference("ride_members").child(ride);
        usersDB = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create new view
        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_recycle_row, parent, false);

        return new MemberViewHolder(cl);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        final User user = usersList.get(position);

        holder.nameTV.setText(user.getName());
        holder.ageTV.setText("Age: " + user.getAge());
        holder.rankTV.setText("Stars: " + user.getRank());
        holder.avatarIV.setImageBitmap(user.getPhoto());

        switch (user.getRide_member())
        {
            case 1:
                holder.ratingCl.setVisibility(View.INVISIBLE);
                holder.buttonsCl.setVisibility(View.VISIBLE);
                holder.acceptIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ride_seats_taken++;
                        FirebaseDatabase.getInstance().getReference("rides").child(ride_id).child("seats_taken").setValue(ride_seats_taken);
                        rideMembersDB.child(user.getUid()).setValue(2);
                        Notification.sendNotification(user.getUid(), ride_id, FirebaseAuth.getInstance().getCurrentUser().getUid(), 2);
                    }
                });
                holder.declineIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rideMembersDB.child(user.getUid()).setValue(3);
                        Notification.sendNotification(user.getUid(), ride_id, FirebaseAuth.getInstance().getCurrentUser().getUid(), 3);
                    }
                });
                break;
            case 2:
                holder.buttonsCl.setVisibility(View.INVISIBLE);
                holder.ratingCl.setVisibility(View.VISIBLE);
                holder.ratingBar.setRating(0);
                holder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        double newRank = user.getRank();
                        if (newRank == 0)
                            newRank = v;
                        else
                            newRank = (double)Math.round((newRank+v)/2 * 100) / 100d;
                        usersDB.child(user.getUid()).child("rank").setValue(newRank);
                        rideMembersDB.child(user.getUid()).setValue(5);
                    }
                });
                break;
            case 4:
                holder.buttonsCl.setVisibility(View.INVISIBLE);
                holder.ratingCl.setVisibility(View.VISIBLE);
                holder.ratingBar.setRating(0);
                holder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        double newRank = user.getRank();
                        if (newRank == 0)
                            newRank = v;
                        else
                            newRank = (double)Math.round((newRank+v)/2 * 100) / 100d;
                        usersDB.child(user.getUid()).child("rank").setValue(newRank);
                        rideMembersDB.child(user.getUid()).setValue(6);
                    }
                });
                break;
            default:
                holder.ratingCl.setVisibility(View.INVISIBLE);
                holder.buttonsCl.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout buttonsCl, ratingCl;
        public ImageView avatarIV, acceptIV, declineIV;
        public TextView nameTV;
        public TextView ageTV;
        public TextView rankTV;
        public RatingBar ratingBar;

        public MemberViewHolder(View itemView) {
            super(itemView);

            buttonsCl = (ConstraintLayout) itemView.findViewById(R.id.buttonsCL);
            ratingCl = (ConstraintLayout) itemView.findViewById(R.id.ratingCL);
            avatarIV = (ImageView) itemView.findViewById(R.id.avatar);
            acceptIV = (ImageView) itemView.findViewById(R.id.acceptBtn);
            declineIV = (ImageView) itemView.findViewById(R.id.declineBtn);
            nameTV = (TextView) itemView.findViewById(R.id.name);
            ageTV = (TextView) itemView.findViewById(R.id.age);
            rankTV = (TextView) itemView.findViewById(R.id.rank);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
        }
    }
}
