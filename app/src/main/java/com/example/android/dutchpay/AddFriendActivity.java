package com.example.android.dutchpay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by Hyunmin Jeong on 9/23/2017.
 */

public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener {
    private Button search_friend;
    private EditText e;
    private DatabaseReference mDatabaseRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        search_friend = (Button)findViewById(R.id.search_friend);
        search_friend.setOnClickListener(this);
        e = (EditText)findViewById(R.id.friend_email);
    }

    public void onClick(View v) {
        if(v == search_friend) {
            searchFriend(e.getText().toString());
        }
    }

    public void searchFriend(String email) {
        Query user = mDatabaseRef.child("email").equalTo("dongjun2@illinois.edu");
        user = mDatabaseRef.child("email").equalTo(email);
        user = mDatabaseRef.child("email").equalTo(email);
    }
}