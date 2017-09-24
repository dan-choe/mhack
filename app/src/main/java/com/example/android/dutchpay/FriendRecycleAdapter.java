package com.example.android.dutchpay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class FriendRecycleAdapter extends RecyclerView.Adapter<FriendRecycleAdapter.ViewHolder>{


    private DatabaseReference mDataReference;
    private List<String> checkedFriends;
    private List<User> nFriends;


    public FriendRecycleAdapter(List<User> friends) {
        this.nFriends = friends;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View movieListItem = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.friend_list, parent, false);
        return new ViewHolder(movieListItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final User newU = nFriends.get(position);
        holder.email.setText("Email: " + newU.getEmail());
        holder.addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    checkedFriends.add(newU.getUid());
                }
                else {
                    checkedFriends.remove(newU.getUid());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return nFriends.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView email;
        public CheckBox addB;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            email = (TextView) itemView.findViewById(R.id.row_email);
            addB = (CheckBox) itemView.findViewById(R.id.row_add);
        }
    }
}