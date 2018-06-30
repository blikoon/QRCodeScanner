package com.blikoon.qrcodescannerlibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

import com.blikoon.qrcodescanner.QrCodeActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private final String LOGTAG = "QRCScanner-MainActivity";
    private Button button;
    private CheckBox showFlashLight;
    private CheckBox showHeader;
    private CheckBox showText;
    private CheckBox showLaser;
    private CheckBox showCorners;
    private CheckBox vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button_start_scan);
        showFlashLight = (CheckBox) findViewById(R.id.checkbox_show_flash_light);
        showHeader = (CheckBox) findViewById(R.id.checkbox_show_header);
        showText = (CheckBox) findViewById(R.id.checkbox_show_text);
        showLaser = (CheckBox) findViewById(R.id.checkbox_show_laser);
        showCorners = (CheckBox) findViewById(R.id.checkbox_show_corners);
        vibrate = (CheckBox) findViewById(R.id.checkbox_vibrate);
        button.setOnClickListener(v -> {
            //Start the qr scan activity
            Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
            i.putExtra(QrCodeActivity.SHOW_FLASH_LIGHT, showFlashLight.isChecked());
            i.putExtra(QrCodeActivity.SHOW_HEADER, showHeader.isChecked());
            i.putExtra(QrCodeActivity.SHOW_TEXT, showText.isChecked());
            i.putExtra(QrCodeActivity.SHOW_LASER, showLaser.isChecked());
            i.putExtra(QrCodeActivity.SHOW_CORNERS, showCorners.isChecked());
            i.putExtra(QrCodeActivity.VIBRATE, vibrate.isChecked());
            startActivityForResult(i, REQUEST_CODE_QR_SCAN);
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            Log.d(LOGTAG, "COULD NOT GET A GOOD RESULT.");
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (result != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }
            return;

        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d(LOGTAG, "Have scan result in your app activity :" + result);
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Scan result");
            alertDialog.setMessage(result);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
            alertDialog.show();

        }
    }
}
