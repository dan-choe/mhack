package com.example.android.dutchpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    // The URI of photo taken from gallery
    private Uri mUriPhotoTaken;

    // File of the photo taken with camera
    private File mFilePhotoTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //selectImage();
    }

    @Override
    public void onClick(View v) {
        if (v == add_balance) {
            addBalance();
        }

        if (v == access_camera) {
            accessCamera();
            //selectImage();
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
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                mFilePhotoTaken = File.createTempFile(
                        "IMG_",  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                if (mFilePhotoTaken != null) {
                    mUriPhotoTaken = FileProvider.getUriForFile(this,
                            "com.example.android.dutchpay.fileprovider",
                            mFilePhotoTaken);
                }
            } catch (IOException e) {
                //setInfo(e.getMessage());
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TAKE_PHOTO) {
            //if(resultCode == RESULT_OK) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                String fileName = createImageFromBitmap(imageBitmap);
                Intent intent = new Intent(getApplicationContext(), ConfirmActivity.class);
                intent.setData(Uri.fromFile(mFilePhotoTaken));
                startActivity(intent);
           // }
        }
        else if(requestCode == CHOOSE_GALLERY && data != null && data.getData() != null) {
            //if(resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    String fileName = createImageFromBitmap(imageBitmap);
                    Intent intent = new Intent(getApplicationContext(), ConfirmActivity.class);
                    intent.setData(imageUri);
                    startActivity(intent);
                } catch (IOException e) {
                }
           // }
        }
    }

    /*
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        if(requestCode == TAKE_PHOTO) {
            if(resultCode == RESULT_OK) {
                // If image is selected successfully, set the image URI and bitmap.
                mImageUri = data.getData();

                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                        mImageUri, getContentResolver());
                if (mBitmap != null) {
                    // Show the image on screen.
                    // ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    // imageView.setImageBitmap(mBitmap);

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
    */

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

}