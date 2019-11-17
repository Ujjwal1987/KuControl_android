package com.kucontrol.chase.kucontrol;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class scan_activity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mscannerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mscannerview = new ZXingScannerView(this);
        if(ActivityCompat.checkSelfPermission(scan_activity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            SharedPreferences sharedPreferences = this.getSharedPreferences("QrCode", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Permission","NO");
            editor.commit();
            onBackPressed();
        }else {
            setContentView(mscannerview);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mscannerview.setResultHandler(this); // Register ourselves as a handler for scan results.
        mscannerview.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mscannerview.stopCamera();           // Stop camera on pause
    }


    public void handleResult(Result rawResult) {
        // Do something with the result here
        // Log.v("tag", rawResult.getText()); // Prints scan results
        // Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        /*Register register = new Register();
        register.scanresult.setText(rawResult.getText());*/
        SharedPreferences sharedPreferences = this.getSharedPreferences("QrCode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Code",rawResult.getText());
        editor.commit();
        onBackPressed();

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}
