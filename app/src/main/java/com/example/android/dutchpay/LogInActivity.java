package com.example.android.dutchpay;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailAddress;
    private EditText emailPassword;

    private Button logButton;
    private Button regButton;

    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.log_in);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);

        if (mFirebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        emailAddress = (EditText)findViewById(R.id.emailAddressEdit);
        emailPassword = (EditText)findViewById(R.id.emailPasswordEdit);

        logButton = (Button)findViewById(R.id.logButton);
        logButton.setOnClickListener(this);

        regButton = (Button)findViewById(R.id.regButton);
        regButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == logButton) {
            firebaseLogIn();
        }
        if (v == regButton) {
            finish();
        }
    }

    private void firebaseLogIn() {

        String address = emailAddress.getText().toString();
        String password = emailPassword.getText().toString();

        // check for valid inputs
        if (TextUtils.isEmpty(address)) {
            toastMessage("Email address field is empty.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            toastMessage("Email password field is empty.");
            return;
        }

        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.show();

        mFirebaseAuth.signInWithEmailAndPassword(address, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Intent dutchPay = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(dutchPay);
                        }
                        else {
                            toastMessage("Invalid Inputs or Need to make an account.");
                        }
                    }
                });
    }

    // helper function to toast a message
    public void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
