package com.example.hang.myapplication;

/**
 * Created by hang on 10/16/17.
 */

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class clientSocketDemo extends Thread {
    String dstAddress;
    int dstPort;
    String response = "";
    TextView textResponse;
    FingerPrint fingerPrint;
    Socket client = null;
    public clientSocketDemo(TextView textResponse) {
        this.textResponse = textResponse;
    }


    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                textResponse.setText("receive data from server:" + msg.what);
            } else if (msg.what == 5) {
                textResponse.setText("connected with server!!!");
            }

//            else if (msg.what == 1) {
//                textResponse.setText("send data to server");
//            } else {
//                textResponse.setText("something wrong");
//            }
        }
    };

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        while (true) {
            try {
                client = new Socket("192.168.3.50", 12345);

                myHandler.sendEmptyMessage(5);
                byte[] b = new byte[1024];
                InputStream in = client.getInputStream();
                int count = in.read(b);
                if (count < 0) {
                    in.close();
                    client.close();
                    continue;
                }
                byte temp[] = new byte[count];
                for (int i = 0; i < count; i++) {
                    temp[i] = b[i];
                }
                String str = new String(temp);
                Message msg = new Message();
                msg.obj = str;
                msg.what = 0;
                myHandler.sendMessage(msg);

                OutputStream os = client.getOutputStream();


                //write the signal strength data

                String data = String.valueOf(getSignalStrength());
                os.write(data.getBytes());
                os.flush();
                myHandler.sendEmptyMessage(1);

                in.close();
                os.close();
                client.close();

            } catch (IOException e) {
                myHandler.sendEmptyMessage(2);
            }
        }
    }


    int getSignalStrength() {
        WifiManager wifiManager = (WifiManager) MainApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> wifiList = wifiManager.getScanResults();
        if (wifiList.size() == 0) {
            return 100;
        }

        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult scanResult = wifiList.get(i);
            String macAddress = scanResult.BSSID;
            if (macAddress.equals("d0:ff:98:81:46:f8")) {
                return scanResult.level;
            }
        }
        return 101;
    }







//    @Override
//    protected String doInBackground(Void... arg0) {
//        Socket socket = null;
//        try {
//            socket = new Socket(dstAddress, dstPort);
//            //send something here
//            Gson gson = new Gson();
//            String s = gson.toJson(fingerPrint);
//            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            dataOutputStream.writeBytes(s);
//            dataOutputStream.flush();
//
//            //You cannot close output stream. Because if you close here, it also means that you close socket here.
//            //Then you will get exception when you do socket.getInputStream();
////            dataOutputStream.close();
//
//            //waiting for receiving information from server
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            InputStream inputStream = socket.getInputStream();
//			/*
//             * notice: inputStream.read() will block if no data return
//			 */
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                byteArrayOutputStream.write(buffer, 0, bytesRead);
//                response += byteArrayOutputStream.toString("UTF-8");
//            }
//
//            dataOutputStream.close();
//            inputStream.close();
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            response = "UnknownHostException: " + e.toString();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            response = "IOException: " + e.toString();
//        } finally {
//            if (socket != null) {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//        return response;
//    }


}