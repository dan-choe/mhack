package com.example.android.dutchpay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.net.Uri;

import com.example.android.dutchpay.helper.ImageHelper;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.projectoxford.vision.VisionServiceClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView receipt_image;
    private Button cancel_button;
    private Button check_button;

    protected Bitmap bitmap;
    private VisionServiceClient client;
    private EditText mTotalText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_amount);

        if (client == null)
            client = new VisionServiceRestClient(getString(R.string.subscription_key));

        try {
            bitmap = BitmapFactory.decodeStream(this.openFileInput("receiptImage"));
            receipt_image = (ImageView) findViewById(R.id.receipt_image);
            receipt_image.setImageBitmap(bitmap);

            if (bitmap != null)
                doRecognize();

        } catch (FileNotFoundException e) {
        }
        mTotalText = (EditText) findViewById(R.id.total);
        cancel_button = (Button) findViewById(R.id.discard_btn);
        cancel_button.setOnClickListener(this);
        check_button = (Button) findViewById(R.id.discard_btn);
        check_button.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == cancel_button) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        if (v == check_button) {

        }
    }

    public void doRecognize() {
        try {
            new doRequest().execute();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (e != null) {
                this.e = null;
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                float f_Type = 0;
                String result = "";
                ArrayList<Float> listofAmount = new ArrayList<>();
                float tempSum = 0f;

                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        result = "";
                        for (Word word : line.words) {
                            result += word.text + " ";
                        }
                        try {
                            f_Type = Float.valueOf(result.trim()).floatValue();
                            if (f_Type != Math.ceil(f_Type)) { // amount
                                listofAmount.add(f_Type);
                                tempSum += f_Type;
                                //System.out.println("float f = " + f_Type);
                            }
                        } catch (NumberFormatException nfe) {
                            //mEditText.setText("1\n");
                        }
                    }
                }
                float max = 0f;
                Collections.sort(listofAmount);
                if (listofAmount.size() > 1) {
                    max = listofAmount.get(listofAmount.size() - 1).floatValue();
                    listofAmount.remove(listofAmount.size() - 1);
                    if (listofAmount.get(listofAmount.size() - 1).floatValue() == max) {
                        listofAmount.remove(listofAmount.size() - 1);
                    }
                }

                double minBound, maxBound;
                boolean isTotal = true;
                if (tempSum != max) {
                    minBound = (tempSum / 3) - ((tempSum / 3) * 0.20);
                    maxBound = (tempSum / 3) + ((tempSum / 3) * 0.20);
                    System.out.println(tempSum + " 1) minBound = " + minBound + " maxBound = " + maxBound);
                    if (minBound > max || maxBound < max) {
                        isTotal = false;
                        minBound = (tempSum / 2) - ((tempSum / 2) * 0.20);
                        maxBound = (tempSum / 2) + ((tempSum / 2) * 0.20);
                        System.out.println(tempSum + " 2) minBound = " + minBound + " maxBound = " + maxBound);
                        if (minBound <= max && maxBound >= max) {
                            isTotal = true;
                        }
                    }
                }

                if (isTotal) {
                    System.out.println("Successfully found Total amount = " + max+"\n\n");
                } else {
                    System.out.println("Failed to find total amount = " + max+"\n\n");
                }

                mTotalText.setText(String.valueOf(max));
            }
        }
    }
}