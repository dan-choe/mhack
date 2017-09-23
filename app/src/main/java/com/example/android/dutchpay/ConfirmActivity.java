package com.example.android.dutchpay;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Hyunmin Jeong on 9/23/2017.
 */

public class ConfirmActivity1 extends AppCompatActivity implements View.OnClickListener {
    private ImageView receipt_image;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_amount);

        Bitmap bitmap = getIntent().getParcelableExtra("BitmapImage");

        receipt_image = (ImageView)findViewById(R.id.receipt_image);
        receipt_image.setImageBitmap(bitmap);
    }

    public void onClick(View v) {

    }
}
