package com.example.android.dutchpay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FriendListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<User> nFriends = new ArrayList<>();
    private FriendRecycleAdapter nFriendAdapter;
    private DatabaseReference mDataReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.friendrecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // get nFriends here
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mFirebaseUser.getUid());

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                for (String e : u.getFriendList()) {
                    User temp = new User();
                    temp.setEmail(e);
                    nFriends.add(temp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                toastMessage("You have not added anyone in your friend's list or Error Occurred");
            }
        };

        nFriendAdapter = new FriendRecycleAdapter(nFriends);
        mRecyclerView.setAdapter(nFriendAdapter);
    }

    // helper function to toast a message
    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

