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

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView receipt_image;

    private Button ok_btn;
    private Button discard_btn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_amount);
        try{
            Bitmap bitmap = BitmapFactory.decodeStream(this.openFileInput("receiptImage"));
            receipt_image = (ImageView) findViewById(R.id.receipt_image);
            receipt_image.setImageBitmap(bitmap);
        }catch (FileNotFoundException e) {
        }
        ok_btn = (Button)findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(this);

        discard_btn = (Button)findViewById(R.id.discard_btn);
        discard_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == ok_btn) {
            startActivity(new Intent(this, FriendListActivity.class));
        }
        if (v == discard_btn ) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
