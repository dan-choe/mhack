package com.example.android.dutchpay;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText emailAddressText;
    private EditText emailPasswordText;
    private EditText userIdText;

    private Button registerButton;
    private Button logInButton;

    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        emailAddressText = (EditText)findViewById(R.id.emailEt);
        emailPasswordText = (EditText)findViewById(R.id.passwordEt);
        userIdText = (EditText)findViewById(R.id.userID);

        registerButton = (Button)findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);

        logInButton = (Button)findViewById(R.id.logInButton);
        logInButton.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View view) {
        if (view == registerButton) {
            firebaseRegister();
        }

        if (view == logInButton) {
            finish();
        }
    }

    private void firebaseRegister() {
        final String userName = userIdText.getText().toString();
        final String emailAddress = emailAddressText.getText().toString();
        final String emailPassword = emailPasswordText.getText().toString();

        // check for valid inputs
        if (!checkForValidInputs(emailAddress, emailPassword)) {
            return;
        }

        // show the progress dialog
        mProgressDialog.setMessage("Registering...");
        mProgressDialog.show();

        // create a new user account
        mFirebaseAuth.createUserWithEmailAndPassword(emailAddress, emailPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            toastMessage("Registration was successful!");

                            String userId = mFirebaseAuth.getCurrentUser().getUid();
                            DatabaseReference currentUser = mDatabaseReference.child(userId);
                            currentUser.child("username").setValue(userName);
                            currentUser.child("email").setValue(emailAddress);
                            currentUser.child("password").setValue(emailPassword);
                            currentUser.child("uid").setValue(userId);
                            List<String> friendList = new ArrayList<>();
                            friendList.add(emailAddress);
                            currentUser.child("friendList").setValue(friendList);
                            currentUser.child("balance").setValue(0);
                            currentUser.child("change").setValue(0);
                            currentUser.child("changeBy").setValue("");
                            // switch activity using intent
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else {
                            toastMessage("Registration was unsuccessful!");
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private boolean checkForValidInputs(String emailAddress, String emailPassword) {

        // check for valid inputs
        if (TextUtils.isEmpty(emailAddress)) {
            toastMessage("Email address field is empty.");
            return false;
        }
        if (TextUtils.isEmpty(emailPassword)) {
            toastMessage("Email password field is empty.");
            return false;
        }
        if (!isValidEmail(emailAddress)) {
            toastMessage("Email address is not valid.");
            return false;
        }
        if (!isValidPassword(emailPassword)) {
            toastMessage("Email password should at least be six characters long.");
            return false;
        }
        return true;
    }

    // email validation borrowed from StackOverflow
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password != null && password.length() >= 6) {
            return true;
        }
        else {
            return false;
        }
    }

    // helper function to toast a message
    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
