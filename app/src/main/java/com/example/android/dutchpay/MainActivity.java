package com.example.android.dutchpay;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;
    private ProgressDialog mProgressDialog;

    private Button add_balance;
    private Button camera;
    private Button gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);

        // set the title as the user email
        if (mFirebaseUser != null) {
            setTitle(mFirebaseUser.getEmail());
        }
        else {
            startActivity(new Intent(this, LogInActivity.class));
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void addBalance(final int addAmount) {

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String me = mFirebaseUser.getUid();
                Map<String, Object> childUpdates = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.uid.equals(me)) {
                        childUpdates.put("/" + me + "/balance/", user.balance + addAmount);
                    }
                }
                mDatabaseRef.updateChildren(childUpdates);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ADD_BALANCE", "Adding to balance failed");
            }
        });
    }

    public void requestPayment() {

    }

    private void payTheRequest(final String friend, final int amount) {

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String me = mFirebaseUser.getUid();
                String friendUid;
                Map<String, Object> childUpdates = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.username.equals(friend)) {
                        friendUid = user.uid;
                        childUpdates.put("/" + friendUid + "/balance/", user.balance + amount);
                    }
                    if (user.uid.equals(me)) {
                        childUpdates.put("/" + me + "/balance/", user.balance - amount);

                        // check if it is possible to send the amount of money
                        if (user.balance - amount < 0) {

                            // dialog box
                            LayoutInflater mLayoutInflater = LayoutInflater.from(MainActivity.this);
                            View mPromptView = mLayoutInflater.inflate(R.layout.dialog, null);
                            AlertDialog.Builder alertDialogBox = new AlertDialog.Builder(MainActivity.this);
                            alertDialogBox.setView(mPromptView);

                            alertDialogBox.setCancelable(false)
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                            // create an alert dialog
                            AlertDialog alert = alertDialogBox.create();
                            alert.show();
                            mProgressDialog.dismiss();
                            return;
                        }
                    }
                }

                mProgressDialog.setMessage("Uploading...");
                mProgressDialog.show();

                mDatabaseRef.updateChildren(childUpdates);
                mProgressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("PAY_REQUEST", "pay request failed");
            }
        });

    }





    // custom log out method on the right top corner as a menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.log_out_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.log_out) {
            mFirebaseAuth.signOut();

            // bring the user back to the log in page
            Intent logInPage = new Intent(this, LogInActivity.class);
            startActivity(logInPage);
        }
        return true;
    }
    // helper function to toast a message
    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
