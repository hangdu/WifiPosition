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
        InputStream in = null;
        try {
            client = new Socket("192.168.3.50", 12345);
            myHandler.sendEmptyMessage(5);
            byte[] b = new byte[1024];
            in = client.getInputStream();
            int count = in.read(b);
            byte temp[] = new byte[count];
            for (int i = 0; i < count; i++) {
                temp[i] = b[i];
            }
            String str = new String(temp);
            Message msg = new Message();
            msg.obj = str;
            msg.what = 0;
            myHandler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //send data to server
        OutputStream os = null;
        int times = 10;
        for (int i = 0; i <= times; i++) {
            try {
                //wait for 3 second
                sleep(1000);
                os = client.getOutputStream();
                //write the signal strength data
                String data = null;
                if (i == times) {
                    data = "end";
                } else {
                   data = String.valueOf(getSignalStrength());
                }
                System.out.print(data);
                os.write(data.getBytes());
                os.flush();
                myHandler.sendEmptyMessage(1);
            } catch (IOException e) {
                myHandler.sendEmptyMessage(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            os.close();
            in.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
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