package com.example.android.dutchpay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
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
        setContentView(R.layout.friend_list);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDataReference = FirebaseDatabase.getInstance().getReference().child("Users");

        nFriendAdapter = new FriendRecycleAdapter(nFriends);
//        mRecyclerView = (RecyclerView) findViewById(R.id.friendrecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(nFriendAdapter);

        prepareNfriendsData();
    }

    private void prepareNfriendsData() {

        mDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String myUid = mFirebaseUser.getUid();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getUid().equals(myUid)) {
                        for (String e : user.getFriendList()) {
                            User temp = new User();
                            temp.setEmail(e);
                            nFriends.add(temp);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        nFriendAdapter.notifyDataSetChanged();
    }
}

