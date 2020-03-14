package com.example.qr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanQR extends AppCompatActivity implements View.OnClickListener {

    EditText fname, lname, dob, address;

    Button scanQR;
    //qr code scanner object
    private IntentIntegrator qrScan;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        //intializing scan object
        qrScan = new IntentIntegrator(this);
        findViewsById();
    }


    private void findViewsById() {
        fname = findViewById(R.id.scanfname1);
        lname = findViewById(R.id.scanlname1);

        dob = findViewById(R.id.scandob1);
        //dob.setInputType(InputType.TYPE_NULL);

        address = findViewById(R.id.scanaddress1);
        scanQR = findViewById(R.id.scanqr);

        imageView = findViewById(R.id.scanimageview);

        scanQR.setOnClickListener(ScanQR.this);

    }


    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //setting values to textviews
                    fname.setText(obj.getString("firstname"));
                    lname.setText(obj.getString("lastname"));
                    dob.setText(obj.getString("dob"));
                    address.setText(obj.getString("address"));
                    String base64Image = obj.getString("image");
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageView.setImageBitmap(decodedByte);

//                    byte [] encodeByte=Base64.decode(base64Image.getBytes(),Base64.DEFAULT);
//                    BitmapFactory.Options options=new BitmapFactory.Options();
//
//                    Bitmap image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length,options);
//
//
//                    if(image.getHeight() <= 400 && image.getWidth() <= 400){
//                        return ;
//                    }
//                    image = Bitmap.createScaledBitmap(image, 400, 400, false);
//
//                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
//                    image.compress(Bitmap.CompressFormat.PNG,100, baos);
//
//                    byte [] b=baos.toByteArray();
//                    System.gc();
                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        //initiating the qr code scan
        qrScan.initiateScan();
    }


}
