package com.example.android.dutchpay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;

/**
 * Created by Hyunmin Jeong on 9/23/2017.
 */

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView receipt_image;
    private Button cancel_button;
    private Button check_button;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_amount);
        try{
            Bitmap bitmap = BitmapFactory.decodeStream(this.openFileInput("receiptImage"));
            receipt_image = (ImageView) findViewById(R.id.receipt_image);
            receipt_image.setImageBitmap(bitmap);
        }catch (FileNotFoundException e) {
        }

        cancel_button = (Button)findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(this);
        check_button = (Button)findViewById(R.id.check_button);
        check_button.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == cancel_button) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        if (v == check_button) {

        }
    }
}
