package com.example.hang.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1234;
    TextView textView;
    Map<String, Integer> pos1Finger;
    Map<String, Integer> pos2Finger;
    WifiManager wifiManager;
    int scanTimesForOnePoint = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MyApp","I am here");
        pos1Finger = new HashMap<>();
        pos2Finger = new HashMap<>();

        //Check for Android M runtime permissions
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
            }
        }

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
// Level of a Scan Result
//        List<ScanResult> wifiList = wifiManager.getScanResults();
//        for (ScanResult scanResult : wifiList) {
//            int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
//            String bssid = scanResult.BSSID;
//            String networkName = scanResult.SSID;
//            int rssi = scanResult.level;
//            System.out.println("ScanResult BSSID is " + bssid);
//            System.out.println("ScanResult rssi is " + rssi);
//            System.out.println("ScanResult networkName is " + networkName);
//        }


        textView = (TextView) findViewById(R.id.textview_result);
        Button button1 = (Button) findViewById(R.id.button_learn1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                textView.setText("Button1 is clicked");
                pos1Finger.clear();
                List<ScanResult> wifiList = wifiManager.getScanResults();
                System.out.println("Scanning result for the first position");
                StringBuilder builder = new StringBuilder();
                builder.append("In total AP number = " + wifiList.size() +  "\n");
                builder.append("\n");
                for (ScanResult scanResult : wifiList) {
                    String bssid = scanResult.BSSID;
                    String networkName = scanResult.SSID;
                    int rssi = scanResult.level;
                    System.out.println("NetworkName = " + networkName);
                    System.out.println("Mac address = " + bssid);
                    System.out.println("Rssi = " + rssi);

                    builder.append("NetworkName = " + networkName + "\n");
                    builder.append("Mac address = " + bssid+ "\n");
                    builder.append("Rssi = " + rssi+ "\n");
                    builder.append("\n");

                    pos1Finger.put(bssid, rssi);
                }
                textView.setText(builder.toString());
            }
        });


        Button button2 = (Button) findViewById(R.id.button_learn2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                textView.setText("Button2 is clicked");

                pos2Finger.clear();
                List<ScanResult> wifiList = wifiManager.getScanResults();
                System.out.println("Scanning result for the first position");
                StringBuilder builder = new StringBuilder();
                builder.append("In total AP number = " + wifiList.size() +  "\n");
                builder.append("\n");
                for (ScanResult scanResult : wifiList) {
                    String bssid = scanResult.BSSID;
                    String networkName = scanResult.SSID;
                    int rssi = scanResult.level;
                    System.out.println("NetworkName = " + networkName);
                    System.out.println("Mac address = " + bssid);
                    System.out.println("Rssi = " + rssi);

                    builder.append("NetworkName = " + networkName + "\n");
                    builder.append("Mac address = " + bssid+ "\n");
                    builder.append("Rssi = " + rssi+ "\n");
                    builder.append("\n");

                    pos2Finger.put(bssid, rssi);
                }
                textView.setText(builder.toString());
            }
        });
    }
}
