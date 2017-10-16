package com.example.hang.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1234;
    TextView textView;
    EditText referencePosition, editTextAddress, editTextPort;
    WifiManager wifiManager;
    String sampleValue;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MyApp","I am here");
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

        referencePosition = (EditText) findViewById(R.id.referencePointName);
        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        textView = (TextView) findViewById(R.id.textview_status);
        Button button1 = (Button) findViewById(R.id.button_learn1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        FingerPrint fingerPrint = scanAP();
                        //send this fingerPrint to server
                        Client myClient = new Client(fingerPrint, editTextAddress.getText().toString(),
                                Integer.parseInt(editTextPort.getText().toString()), textView);
                        myClient.execute();
                    }
                };
                new Thread(runnable).start();
            }
        });
    }

    private FingerPrint scanAP() {
        //3 AP
        Map<String, List<Integer>> map = new HashMap<>();
//        for (int i = 0; i < 60; i++) {
        for (int i = 0; i < 5; i++) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            StringBuilder builder = new StringBuilder();
            builder.append("Index = " + i + "\n");
            for (int k = 0; k < 3; k++) {
                ScanResult oneAPScanResult = wifiList.get(k);
                String bssid = oneAPScanResult.BSSID;
                String networkName = oneAPScanResult.SSID;
                int rssi = oneAPScanResult.level;
                builder.append("NetworkName = " + networkName + "\n");
                builder.append("Mac address = " + bssid+ "\n");
                builder.append("Rssi = " + rssi+ "\n");
                builder.append("\n");

                if (map.containsKey(bssid)) {
                    map.get(bssid).add(rssi);
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(rssi);
                    map.put(bssid, list);
                }
            }
            sampleValue = new String(builder);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(sampleValue);
                }
            });
        }

        String referceName = referencePosition.getText().toString();
        return new FingerPrint(referceName, map);
    }

    //After scanning APs, send to server

//    double getDistance(Map<String, Integer> map1, Map<String, Integer> map2) {
//        int sum = 0;
//        for (String key : map1.keySet()) {
//            int strength1 = map1.get(key);
//            int strength2 = map2.get(key);
//            int diff = strength1-strength2;
//            sum += diff * diff;
//        }
//        return Math.sqrt(sum);
//    }
}
