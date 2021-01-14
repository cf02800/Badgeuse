package com.lpro.badgeuse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST_CODE_SEND_SMS = 1;

    private static final String LOG_TAG = "AndroidExample";


    private EditText phoneForm;
    Location gps_loc;
    Location network_loc;
    Location final_loc;
    double longitude;
    double latitude;
    private Button sendButton;
    double latitudeTravail = 49.660865783691406;
    double longitudeTravail = 3.3454840183258057;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.phoneForm = (EditText) this.findViewById(R.id.editText_Numero);

        this.sendButton = (Button) this.findViewById(R.id.button_envoyer);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        try {

            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (gps_loc != null) {
            final_loc = gps_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else if (network_loc != null) {
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else {
            latitude = 0.0;
            longitude = 0.0;
        }

        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermissionAndSendSMS();
            }
        });
    }

    private void askPermissionAndSendSMS() {
        if (android.os.Build.VERSION.SDK_INT >=  android.os.Build.VERSION_CODES.M) { // 23

            // Vérifier permission d'envoyer des SMS
            int sendSmsPermisson = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS);

            if (sendSmsPermisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSION_REQUEST_CODE_SEND_SMS
                );
                return;
            }
        }
        this.sendSMS_by_smsManager();
    }
    private void sendSMS_by_smsManager()  {
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String Nom = preferences.getString("nom", "");
        String Telephone = preferences.getString("phoneNo", "");
        double distance = getDistanceBetweenTwoPoints(latitude,longitude,latitudeTravail,longitudeTravail);
        // Vérification
        if (distance == 1000) {
            Log.e( LOG_TAG,"ça ne marche pas");
            Toast.makeText(getApplicationContext(),"ça ne marche pas",
                    Toast.LENGTH_LONG).show();
        }
        else if (distance <= 500) {
            // Obtenir l'instance du manager SMS
            SmsManager smsManager = SmsManager.getDefault();
            // Envoyer le SMS
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            smsManager.sendTextMessage(Telephone,
                    null,
                    Nom + "\n" + time,
                    null,
                    null);

            Log.i( LOG_TAG,"Le message est bien envoyé !");
            Toast.makeText(getApplicationContext(),"Le message est bien envoyé !",
                    Toast.LENGTH_LONG).show();
        }
        else {
            Log.e( LOG_TAG,"T'es trop loin du lieu de travail !");
            Toast.makeText(getApplicationContext(),"T'es trop loin du lieu de travail !",
                    Toast.LENGTH_LONG).show();
        }
    }


    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE_SEND_SMS: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (SEND_SMS).
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i( LOG_TAG,"Permission granted!");
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    this.sendSMS_by_smsManager();
                }
                // Cancelled or denied.
                else {
                    Log.i( LOG_TAG,"Permission denied!");
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // When results returned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_PERMISSION_REQUEST_CODE_SEND_SMS) {
            if (resultCode == RESULT_OK) {
                // Do something with data (Result returned).
                Toast.makeText(this, "Action OK", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action canceled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public double getDistanceBetweenTwoPoints(double lat1, double lon1, double lat2, double lon2){
        if (lat1 == 0 && lon1 == 0) {
            return 1000;
        }
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }



}