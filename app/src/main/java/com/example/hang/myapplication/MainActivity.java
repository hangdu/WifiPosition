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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1234;
    TextView textView;
    EditText referencePosition, editTextAddress;
    WifiManager wifiManager;
    String sampleValue;
    Handler handler = new Handler();


    String learning = "LEARNING";
    String tracking = "TRACKING";

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
        textView = (TextView) findViewById(R.id.textview_status);
        Button button1 = (Button) findViewById(R.id.button_learn1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String referceName = referencePosition.getText().toString();
                        FingerPrint fingerPrint = scanAPForLearning(learning, referceName);
                        //send this fingerPrint to server
                        Client myClient = new Client(fingerPrint, editTextAddress.getText().toString(), 12345, textView);
                        myClient.execute();
                    }
                };
                new Thread(runnable).start();
            }
        });

        Button trackButton = (Button) findViewById(R.id.track);
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        FingerPrint fingerPrint = scanAPForTracking(tracking);
                        //send this fingerPrint to server
                        Client myClient = new Client(fingerPrint, editTextAddress.getText().toString(), 12345, textView);
                        myClient.execute();
                    }
                };
                new Thread(runnable).start();
            }
        });
    }

    private FingerPrint scanAPForTracking(String goal) {
        //3 AP
        Map<String, List<Integer>> map = new HashMap<>();

        //desending sort
        Comparator<ScanResult> comp = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                return t1.level - scanResult.level;
            }
        };

        List<ScanResult> wifiList1 = wifiManager.getScanResults();
        Collections.sort(wifiList1, comp);


        List<ScanResult> wifiList = wifiManager.getScanResults();
        Collections.sort(wifiList, comp);
        StringBuilder builder = new StringBuilder();

        for (int k = 0; k < 3; k++) {
            ScanResult oneAPScanResult = wifiList.get(k);
            String bssid = oneAPScanResult.BSSID;
            String networkName = oneAPScanResult.SSID;
            int rssi = oneAPScanResult.level;
            builder.append("NetworkName = " + networkName + "\n");
            builder.append("Mac address = " + bssid+ "\n");
            builder.append("Rssi = " + rssi+ "\n");
            builder.append("\n");

            if (!map.containsKey(bssid)) {
                List<Integer> list = new ArrayList<>();
                map.put(bssid, list);
            }
            map.get(bssid).add(rssi);
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
        return new FingerPrint(goal, null, map);
    }

    private FingerPrint scanAPForLearning(String goal, String referceName) {
        //3 AP
        Map<String, List<Integer>> map = new HashMap<>();

        //desending sort
        Comparator<ScanResult> comp = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                return t1.level - scanResult.level;
            }
        };

        List<ScanResult> wifiList1 = wifiManager.getScanResults();
        Collections.sort(wifiList1, comp);
        for (int k = 0; k < 3; k++) {
            ScanResult oneAPScanResult = wifiList1.get(k);
            String bssid = oneAPScanResult.BSSID;
            List<Integer> list = new ArrayList<>();
            map.put(bssid, list);
        }
        for (int i = 0; i < 30; i++) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            Collections.sort(wifiList, comp);
            StringBuilder builder = new StringBuilder();
            builder.append("Index = " + i + "\n");

            int count = 0;
            for (int k = 0; k < wifiList.size(); k++) {
                ScanResult oneAPScanResult = wifiList.get(k);
                String bssid = oneAPScanResult.BSSID;
                String networkName = oneAPScanResult.SSID;
                int rssi = oneAPScanResult.level;
                builder.append("NetworkName = " + networkName + "\n");
                builder.append("Mac address = " + bssid+ "\n");
                builder.append("Rssi = " + rssi+ "\n");
                builder.append("\n");

                if (count < 3) {
                    if (map.containsKey(bssid)) {
                        map.get(bssid).add(rssi);
                        count++;
                    }
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
        return new FingerPrint(goal, referceName, map);
    }
}
