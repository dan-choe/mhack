package com.example.android.dutchpay;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity{

    private RecyclerView mRecyclerView;
    private DatabaseReference mDataReference;
    private List<String> checkedFriends;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.friend_list);

        // TODO: here change it to friend's list
        mDataReference = FirebaseDatabase.getInstance().getReference().child("Users");

        // Recycler
        mRecyclerView = (RecyclerView) findViewById(R.id.friendRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        checkedFriends = new ArrayList<>();

    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        View thisView;
        Switch addSw;
        EditText email;
        EditText userN;

        public FriendViewHolder(View itemView, View thisView, Switch addSw, EditText email, EditText userN) {
            super(itemView);
            this.thisView = thisView;
            this.addSw = addSw;
            this.email = email;
            this.userN = userN;
        }

        public void setThisView(View thisView) {
            this.thisView = thisView;
        }

        public void setAddSw(Switch addSw) {
            this.addSw = addSw;
        }

        public void setEmail(String email) {
            EditText post_email = (EditText) thisView.findViewById(R.id.row_email);
            post_email.setText(email);
        }

        public void setUsern(String usern) {
            EditText post_userN = (EditText) thisView.findViewById(R.id.row_usern);
            post_userN.setText(usern);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<User, FriendViewHolder> mFirebaseRecAdapter = new FirebaseRecyclerAdapter<User, FriendViewHolder>(
                User.class,
                R.layout.single_friend,
                FriendViewHolder.class,
                mDataReference
        ) {
            @Override
            protected void populateViewHolder(FriendViewHolder viewHolder, final User model, int position) {

                viewHolder.setEmail("Email: " + model.getEmail());
                viewHolder.setUsern("Username: " + model.getUsername());


                Switch sButton = (Switch) findViewById(R.id.row_add);

                //Set a CheckedChange Listener for Switch Button
                sButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(CompoundButton cb, boolean on){
                        if (on) {
                            checkedFriends.add(model.getUid());
                        }
                        else {
                            checkedFriends.remove(model.getUid());
                        }
                    }
                });
            }
        };
        mRecyclerView.setAdapter(mFirebaseRecAdapter);
    }

}
