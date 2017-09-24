package com.example.android.dutchpay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hyunmin Jeong on 9/23/2017.
 */

public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener {
    private Button search_friend;
    private EditText e;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabaseUserRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUserRef = mDatabaseRef.child(mFirebaseUser.getUid());

        search_friend = (Button)findViewById(R.id.search_friend);
        search_friend.setOnClickListener(this);
        e = (EditText)findViewById(R.id.friend_email);
    }

    public void onClick(View v) {
        if(v == search_friend) {
            searchFriend(e.getText().toString());
        }
    }

    public void searchFriend(final String friendEmail) {
        final String myEmail = mFirebaseUser.getEmail();
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String myUid = null, friendUid = null;
                List<String> myFL = new ArrayList<>();
                List<String> friendFL = new ArrayList<>();
                for (DataSnapshot datasnapshot : dataSnapshot.getChildren()) {
                    User user = datasnapshot.getValue(User.class);
                    if (user.getEmail().equals(friendEmail)) {
                        friendUid = user.getUid();
                        friendFL = user.getFriendList();
                    }
                    if (user.getEmail().equals(myEmail)) {
                        myUid = user.getUid();
                        myFL = user.getFriendList();
                    }
                }
                if(myUid == null || friendUid == null)
                    return;
                Map<String, Object> userUpdates = new HashMap<String, Object>();
                if(!friendFL.contains(myEmail))
                    friendFL.add(myEmail);
                if(!myFL.contains(friendEmail))
                    myFL.add(friendEmail);
                userUpdates.put(friendUid + "/friendList", friendFL);
                userUpdates.put(myUid + "/friendList", myFL);
                mDatabaseRef.updateChildren(userUpdates);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}