package com.example.android.dutchpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.android.dutchpay.helper.*;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;
    private ProgressDialog mProgressDialog;

    private Button add_balance;
    private Button access_camera;
    private Button access_gallery;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_GALLERY = 2;

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
        setContentView(R.layout.activity_main);

        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);

        add_balance = (Button)findViewById(R.id.add_balance);
        add_balance.setOnClickListener(this);
        access_camera = (Button)findViewById(R.id.access_camera);
        access_camera.setOnClickListener(this);
        access_gallery = (Button)findViewById(R.id.access_gallery);
        access_gallery.setOnClickListener(this);

        mEditText = (EditText)findViewById(R.id.editText);

        // set the title as the user email
        if (mFirebaseUser != null) {
            setTitle(mFirebaseUser.getEmail());
        }
        else {
            startActivity(new Intent(this, LogInActivity.class));
        }

        if (getString(R.string.subscription_key).startsWith("Please")) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == add_balance) {
            addBalance();
        }

        if (v == access_camera) {
            //accessCamera();
            selectImage();
        }
        if (v == access_gallery) {
            accessGallery();
        }
    }

    public void selectImage() {
        Intent takePictureIntent = new Intent(MainActivity.this, com.example.android.dutchpay.helper.SelectImageActivity.class);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        }
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

    public void addBalance() {
        //Intent addBalance = new Intent(getApplicationContext(), BalanceActivity.class);
        //startActivity(BalanceActivity);
    }
    public void accessCamera() {
        dispatchTakePictureIntent();
    }
    public void accessGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_GALLERY);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        if(requestCode == TAKE_PHOTO) {
            if(resultCode == RESULT_OK) {
                /*Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                Intent intent = new Intent(getApplicationContext(), ConfirmActivity.class);
                intent.putExtra("BitmapImage", imageBitmap);
                startActivity(intent);*/

                /*
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                String fileName = createImageFromBitmap(imageBitmap);
                Intent intent = new Intent(getApplicationContext(), ConfirmActivity.class);
                startActivity(intent);
                */

                // If image is selected successfully, set the image URI and bitmap.
                mImageUri = data.getData();

                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                        mImageUri, getContentResolver());
                if (mBitmap != null) {
                    // Show the image on screen.
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(mBitmap);

                    // Add detection log.
                    Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                            + "x" + mBitmap.getHeight());

                    doRecognize();
                }
            }
        }
        else if(requestCode == CHOOSE_GALLERY && data != null && data.getData() != null) {
            if(resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    String fileName = createImageFromBitmap(imageBitmap);
                    Intent intent = new Intent(getApplicationContext(), ConfirmActivity.class);
                    startActivity(intent);
                } catch (IOException e) {
                }
            }
        }
    }

    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "receiptImage";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    public void doRecognize() {
        //mButtonSelectImage.setEnabled(false);
        mEditText.setText("Analyzing...");

        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            mEditText.setText("Error encountered. Exception is: " + e.toString());
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

                mEditText.setText(String.valueOf(max));
            }
            access_camera.setEnabled(true);
        }
    }
}