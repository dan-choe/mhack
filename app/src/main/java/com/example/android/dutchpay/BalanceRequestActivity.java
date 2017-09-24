package com.example.android.dutchpay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Hyunmin Jeong on 9/23/2017.
 */

public class BalanceRequestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button addBalance_yes;
    private double addedBalance;
    private EditText e;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_balance);

        addBalance_yes = (Button)findViewById(R.id.addBalance_yes);
        addBalance_yes.setOnClickListener(this);
        e = (EditText) findViewById(R.id.added_balance);
    }

    public void onClick(View v) {
        if (v == addBalance_yes) {
            if(e.getText().toString().equals(""))
                addedBalance = 0;
            else
                addedBalance = Double.parseDouble(e.getText().toString());
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("add_balance", addedBalance);
            startActivity(intent);
        }
    }
}
