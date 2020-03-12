package com.example.qr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText fname, lname, dob, address;

    DatePickerDialog fromDatePickerDialog;

    SimpleDateFormat dateFormatter;

    Button selectImage,scanQR,generateQR;

    CircleImageView imageView;
    ImageView qrImage;
    String fnameHolder, lnameHolder, dobHolder, addressHolder;

    //keep track of camera capture intent
    final int CAMERA_CAPTURE = 1;

    final int PICK_IMAGE_REQUEST = 2;
    //keep track of cropping intent
    final int PIC_CROP = 3;
    //captured picture uri

    File storeDirectory,f;
    String ConvertImage;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String IMAGE_DIRECTORY = "/mosip/users";
    private static final String QR_DIRECTORY = "/mosip/qr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storeDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!storeDirectory.exists()) {
            storeDirectory.mkdirs();
        }

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        findViewsById();

        setDateTimeField();

    }

    private void findViewsById() {
        fname = findViewById(R.id.fname1);
        lname = findViewById(R.id.lname1);

        dob = findViewById(R.id.dob1);
        //dob.setInputType(InputType.TYPE_NULL);
        dob.setOnClickListener(this);
        address = findViewById(R.id.address1);
        selectImage = findViewById(R.id.selectimage1);
        selectImage.setOnClickListener(this);

        imageView = findViewById(R.id.imageview);

        scanQR = findViewById(R.id.scanQR);
        scanQR.setOnClickListener(this);

        generateQR = findViewById(R.id.generateQR);
        generateQR.setOnClickListener(this);

        qrImage = findViewById(R.id.qrimage);
    }

    private void setDateTimeField() {

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dob.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public boolean isEmpty() {
        fnameHolder = fname.getText().toString();
        lnameHolder = lname.getText().toString();
        dobHolder = dob.getText().toString();
        addressHolder = address.getText().toString();

        int flag = 0;
        if (fnameHolder.matches("")) {
            fname.setError("please enter first name");
            flag=1;
        }

        if (lnameHolder.matches("")) {
            lname.setError("please enter last name");
            flag=1;
        }

//        if (dobHolder.matches("")) {
//            dob.setError("please select dob");
//        }

        if (addressHolder.matches("")) {
            address.setError("please enter address");
            flag=1;
        }

        if(flag==1)
            return  true;
        else
            return false;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean readaccess = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeaccess = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (readaccess && writeaccess && cameraAccepted)
                    {

                    }
                    else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to all the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Log.d("data value = ", String.valueOf(data));

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE) {

                //create instance of File with same name we created before to get image from storage
                File file = new File(Environment.getExternalStorageDirectory()+ IMAGE_DIRECTORY +File.separator +  "img.jpg");

                Uri cameraPicUri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.mosip.fileprovider", file);

                Bitmap cameraPic = null;
                try {
                    cameraPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraPicUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(cameraPic);
                cropImage(cameraPicUri);
                //Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_LONG).show();
            }

        } if(requestCode == PICK_IMAGE_REQUEST) {
            if(data!=null) {
                Uri gallerypicUri = data.getData();
                Bitmap galleryPic = null;
                try {
                    galleryPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), gallerypicUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(galleryPic);
                cropImage(gallerypicUri);
                //compressImage(gallerypicUri.toString());
            }
        }

        else if (resultCode!= RESULT_CANCELED && requestCode == PIC_CROP) {

            Uri croppedPicUri = data.getData();
            cropImage(croppedPicUri);

            Log.d("croppedPic Uri = ", String.valueOf(croppedPicUri));
            if ( data!=null) {
                //get the cropped bitmap
                Bitmap croppedPic = null;
                try {
                    croppedPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedPicUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Log.d("photo = ", croppedPic.toString());
                imageView.setImageBitmap(croppedPic);
                compressImage(croppedPicUri.toString());
                //saveImage(croppedPic);
            }
        }
    }


    private void cropImage(Uri picUri1)
    {
        Log.d("crop uri :",picUri1.toString());
        try{
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri1, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);

            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);

        } catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Your device doesn't support the crop action!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImage(Bitmap myBitmap)  {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 64, bytes);
        imageView.setImageBitmap(myBitmap);
        byte[] byteArrayVar = bytes.toByteArray();

        ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);
        Log.d("Base 64", ConvertImage);

        try {
            f = new File(storeDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved to : " + f.getAbsolutePath());
            Toast.makeText(this, "Image Saved to : "+ f.getAbsolutePath() , Toast.LENGTH_LONG).show();
            return f.getAbsolutePath();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }


    public String saveQR(Bitmap myBitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        byte[] byteArrayVar = bytes.toByteArray();

        String  ConvertQR = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + QR_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();

            Toast.makeText(this, "QR Saved to : "+ f.getAbsolutePath() , Toast.LENGTH_LONG).show();
            return f.getAbsolutePath();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }


    public void compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

//      write the compressed bitmap at the destination specified by filename.
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        byte[] byteArrayVar = bytes.toByteArray();

        ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);
        Log.d("Base 64", ConvertImage);

        imageView.setImageBitmap(scaledBitmap);

        saveImage(scaledBitmap);
    }


    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dob1:
                fromDatePickerDialog.show();
                break;
            case R.id.selectimage1:
                if (checkPermission()) {
                    AlertDialog.Builder pictureDialog = new AlertDialog.Builder(MainActivity.this);
                    pictureDialog.setTitle("Select Photo From");
                    String[] pictureDialogItems = {
                            "Camera",
                            "Gallery",
                            "Remove Photo"};
                    pictureDialog.setItems(pictureDialogItems,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            try {

                                                File file = new File(Environment.getExternalStorageDirectory()+ IMAGE_DIRECTORY +File.separator  +  "img.jpg");

                                                Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(),
                                                        "com.example.mosip.fileprovider", file);

                                                //use standard intent to capture an image
                                                Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");

                                                /*create instance of File with name img.jpg*/
                                                /*put uri as extra in intent object*/
                                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                                                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                                /*start activity for result pass intent as argument and request code */
                                                startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
                                                Log.d("outputFileUri",outputFileUri.toString());
                                            }
                                            catch(ActivityNotFoundException anfe){
                                                //display an error message
                                                String errorMessage = "Your device doesn't support capturing images!";
                                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case 1:
                                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                            // Start the Intent
                                            galleryIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                            /*start activity for result pass intent as argument and request code */
                                            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                                            break;
//                                            Intent intent = new Intent();
//                                            intent.setType("image/*");
//                                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                                            startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
                                        case 2:
                                            imageView.setImageBitmap(null);
                                            break;
                                    }
                                }
                            });
                    pictureDialog.show();
                }
                else {
                    requestPermission();
                }
                break;
            case R.id.scanQR:
                Intent intent = new Intent(MainActivity.this, ScanQR.class);
                startActivity(intent);
                break;
            case R.id.generateQR:
                if(!isEmpty()) {
                    JSONObject json_data = new JSONObject();
                    try {
                        json_data.put("firstname", fnameHolder);
                        json_data.put("lastname", lnameHolder);
                        json_data.put("dob", dobHolder);
                        json_data.put("address", addressHolder);
                        json_data.put("image", ConvertImage);

                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        Log.d("json data", String.valueOf(json_data));
                        BitMatrix bitMatrix = multiFormatWriter.encode(String.valueOf(json_data), BarcodeFormat.QR_CODE, 400, 400);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        saveQR(bitmap);
                        qrImage.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
