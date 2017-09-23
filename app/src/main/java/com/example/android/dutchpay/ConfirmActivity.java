package com.example.android.dutchpay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;

/**
 * Created by Hyunmin Jeong on 9/23/2017.
 */

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView receipt_image;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_amount);
        try{
            Bitmap bitmap = BitmapFactory.decodeStream(this.openFileInput("receiptImage"));
            receipt_image = (ImageView) findViewById(R.id.receipt_image);
            receipt_image.setImageBitmap(bitmap);
        }catch (FileNotFoundException e) {
        }
    }

    public void onClick(View v) {

    }
}
