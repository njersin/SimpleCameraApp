package com.example.mt1556ys.simplecameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button takePictureButton;
    ImageView cameraPicture;

    final String filename = "temp_photo.jpg";
    Uri imageFileUri;

    private static final int TAKE_PICTURE_REQUEST = 0;
    private static final String PICTURE_TO_DISPLAY = "picture has been taken";
    boolean pictureToDisplay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            pictureToDisplay = savedInstanceState.getBoolean(PICTURE_TO_DISPLAY, false);
        }

        setContentView(R.layout.activity_main);

        cameraPicture = (ImageView) findViewById(R.id.camera_picture);

        takePictureButton = (Button) findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File file = new File(Environment.getExternalStorageDirectory(), filename);
                imageFileUri = Uri.fromFile(file);

                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);

                if (pictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    startActivityForResult(pictureIntent, TAKE_PICTURE_REQUEST);
                } else {
                    Toast.makeText(MainActivity.this, "No camera available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == TAKE_PICTURE_REQUEST) {
            pictureToDisplay = true;

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            imageFileUri = Uri.fromFile(file);
            mediaScanIntent.setData(imageFileUri);
            sendBroadcast(mediaScanIntent);

        } else {
            pictureToDisplay = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && pictureToDisplay) {
            Bitmap image = scaleBitmap();
            cameraPicture.setImageBitmap(image);
        }
    }

    Bitmap scaleBitmap() {
        // * Scale picture taken to fit into the ImageView */
        //Step 1: what size is the ImageView?
        int imageViewHeight = cameraPicture.getHeight();
        int imageViewWidth = cameraPicture.getWidth();

        //Step 2: decode file to find out how large the image is.
        // BitmapFactory is used to create bitmaps from pixels in a file.
        // Many options and settings, so use a BitmapFactory.Options object to store our desired settings.
        // Set the inJustDecodeBounds flag to true,
        // which means just the *information about* the picture is decoded and stored in bOptions
        // Not all of the pixels have to be read and stored here.
        // When we've done this, we can query bOptions to find out the original picture's height and width.
        BitmapFactory.Options bOptions = new BitmapFactory.Options();
        bOptions.inJustDecodeBounds = true;

        File file = new File(Environment.getExternalStorageDirectory(), filename);
        Uri imageFileUri = Uri.fromFile(file);
        String photoFilePath = imageFileUri.getPath();

        BitmapFactory.decodeFile(photoFilePath, bOptions);

        //What size is the picture?
        int pictureHeight = bOptions.outHeight;
        int pictureWidth = bOptions.outWidth;

        //Step 3. Can use the original size and target size to calculate scale factor
        int scaleFactor = Math.min(pictureHeight / imageViewHeight, pictureWidth / imageViewWidth);

        //Step 4. Decode the image file into a new bitmap, scaled to fit the ImageView
        bOptions.inJustDecodeBounds = false;   //now we want to get a bitmap
        bOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath, bOptions);
        return bitmap;

    }

    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putBoolean(PICTURE_TO_DISPLAY, pictureToDisplay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
