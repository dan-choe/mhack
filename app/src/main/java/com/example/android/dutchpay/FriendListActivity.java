package com.example.android.dutchpay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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



public class FriendListActivity extends AppCompatActivity implements View.OnClickListener{

    private List<String> nFriends = new ArrayList<>();
    private List<String> frindsUID = new ArrayList<>();
    private DatabaseReference mDataReference;
    private FirebaseUser mFirebaseUser;
    private ListView lvCheckBox;
    private Button btnCheckAll, btnClearALl;
    private Button confirm_friends;
    private String[] arr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_friend);

        btnCheckAll = (Button)findViewById(R.id.btnCheckAll);
        btnClearALl = (Button)findViewById(R.id.btnClearAll);
        confirm_friends = (Button)findViewById(R.id.confirm_friends);
        confirm_friends.setOnClickListener(this);

        lvCheckBox = (ListView)findViewById(R.id.lvCheckBox);
        lvCheckBox.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final FriendListActivity _this = this;

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDataReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String myUid = mFirebaseUser.getUid();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getUid().equals(myUid)) {
                        nFriends = user.getFriendList();
                    }
                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (nFriends.contains(user.getEmail()) && !user.getUid().equals(myUid)) {
                        frindsUID.add(user.getUid());
                    }
                }

                arr = new String[nFriends.size() - 1];
                int ii = 0;
                for (int i = 0; i < nFriends.size(); i++) {
                    if (!nFriends.get(i).equals(mFirebaseUser.getEmail())) {
                        arr[ii] = nFriends.get(i);
                        ii++;
                    }
                }
                lvCheckBox.setAdapter(new ArrayAdapter<String>(_this, R.layout.single_friend, R.id.textView, arr));

                btnCheckAll.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View arg0) {
                        for(int i=0 ; i < lvCheckBox.getAdapter().getCount(); i++) {
                            lvCheckBox.setItemChecked(i, true);
                        }
                    }
                });

                btnClearALl.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        for(int i=0 ; i < lvCheckBox.getAdapter().getCount(); i++) {
                            lvCheckBox.setItemChecked(i, false);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void onClick(View v) {
        if (v == confirm_friends) {
            MainActivity m= new MainActivity();
            m.requestPayment(frindsUID, ConfirmActivity.get_TOTAL_AMOUNT());
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}

