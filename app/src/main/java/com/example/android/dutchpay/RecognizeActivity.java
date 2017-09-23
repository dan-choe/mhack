package com.example.android.dutchpay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.example.android.dutchpay.helper.ImageHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class RecognizeActivity extends ActionBarActivity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private Button mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    // The edit to show status and result.
    private EditText mEditText;

    private VisionServiceClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }

        mButtonSelectImage = (Button)findViewById(R.id.buttonSelectImage);
        mEditText = (EditText)findViewById(R.id.editTextResult);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doRecognize() {
        mButtonSelectImage.setEnabled(false);
        mEditText.setText("Analyzing...");

        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            mEditText.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        Intent intent;
        intent = new Intent(RecognizeActivity.this, com.example.android.dutchpay.helper.SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.receipt_image);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doRecognize();
                    }
                }
                break;
            default:
                break;
        }
    }




    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
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

        public doRequest() {
        }

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
            // Display based on error existence

            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                float f_Type = 0;
                String result = "";
                ArrayList<Float> listofAmount = new ArrayList<Float>();
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
                Collections.sort(listofAmount);
                float max = listofAmount.get(listofAmount.size()-1).floatValue();
                listofAmount.remove(listofAmount.size()-1);
                if(listofAmount.get(listofAmount.size()-1).floatValue() == max) {
                    listofAmount.remove(listofAmount.size() - 1);
                }
                /*
                Iterator itr = listofAmount.iterator();
                while (itr.hasNext()){
                    float x = (float) itr.next();
                    if (x == max)
                        listofAmount.remove(x);
                }
                */
                double minBound, maxBound;

                boolean isTotal = true;
                if (tempSum != max){
                    minBound = (tempSum / 3) - ((tempSum / 3) * 0.20);
                    maxBound = (tempSum / 3) + ((tempSum / 3) * 0.20);
                    System.out.println(tempSum + " 1) minBound = " + minBound + " maxBound = " + maxBound);
                    if (minBound > max || maxBound < max){
                        isTotal = false;
                        minBound = (tempSum / 2) - ((tempSum / 2) * 0.20);
                        maxBound = (tempSum / 2) + ((tempSum / 2) * 0.20);
                        System.out.println(tempSum + " 2) minBound = " + minBound + " maxBound = " + maxBound);
                        if (minBound <= max && maxBound >= max){
                            isTotal = true;
                        }
                    }
                }

                if (isTotal){
                    System.out.println("Successfully found Total amount = " + max);
                }else{
                    System.out.println("Failed to find total amount = " + max);
                }
            }
            mButtonSelectImage.setEnabled(true);
        }
    }
}
