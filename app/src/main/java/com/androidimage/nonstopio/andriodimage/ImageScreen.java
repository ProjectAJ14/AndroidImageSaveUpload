package com.androidimage.nonstopio.andriodimage;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.util.UUID.randomUUID;

public class ImageScreen extends AppCompatActivity {

    ImageView image_View;
    Button get_image, save_image, open_image;
    TextView base64_text;
    Context context;
    private static final int PICK_FROM_CAMERA = 1888;
    private static final int SELECT_PICTURE = 1;
    private static final int PERMISSIONS_MULTIPLE_REQUEST = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    Bitmap imageBitmap;

    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        image_View = findViewById(R.id.image_View);
        get_image = findViewById(R.id.get_image);
        save_image = findViewById(R.id.save_image);
        open_image = findViewById(R.id.open_image);
        base64_text = findViewById(R.id.base64_text);


        get_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(ImageScreen.this);
                Toast.makeText(context, "Getting", Toast.LENGTH_SHORT).show();

            }
        });

        save_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                image_View.setImageBitmap(null);


                File path = Environment.getExternalStorageDirectory();


                File dir = new File(path + "/APPNAME/");

                if (!dir.exists())
                    dir.mkdir();

                imagePath = randomUUID() + ".png";

                File file = new File(dir, imagePath);
                try {
                    OutputStream outputStream = new FileOutputStream(file);


                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream);


                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

            }
        });

        open_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {

                    File path = Environment.getExternalStorageDirectory();
                    File f = new File(path + "/APPNAME/", imagePath);
                    imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                    image_View.setImageBitmap(imageBitmap);
                    base64_text.setText(encodeTobase64(imageBitmap));

                    //Call API


                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(context, "Opened", Toast.LENGTH_SHORT).show();


            }
        });


    }


    public static String encodeTobase64(Bitmap image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final boolean compress = image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        byteArrayOutputStream.close();

        return imageEncoded;
    }


    public void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    PERMISSIONS_MULTIPLE_REQUEST

            );


        } else {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    verifyStoragePermissions(ImageScreen.this);
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent cameraIntent = new
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                            SELECT_PICTURE);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }

        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {


            if (requestCode == SELECT_PICTURE) {
                Uri imageURI = data.getData();
                Bitmap bitmap = null;


                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (bitmap != null) {


                    // TODO: 5/6/18 Added Image compression

                    imageBitmap = bitmap;
                    image_View.setImageBitmap(imageBitmap);


                }
            }

            if (requestCode == PICK_FROM_CAMERA) {

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {

                    imageBitmap = bitmap;
                    image_View.setImageBitmap(imageBitmap);

                }
            }
        }

    }

}
