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
    boolean isReceive = true;

    InputStream in = null;
    OutputStream os = null;
    public clientSocketDemo(TextView textResponse) {
        this.textResponse = textResponse;
    }


    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                textResponse.setText("receive data from server:" + msg.what);
            } else if (msg.what == 5) {
//                textResponse.setText("connected with server!!!");
            } else if (msg.what == 6) {
                textResponse.setText("Receive command = stop");
            } else if (msg.what == 7) {
                textResponse.setText("not connected with server");
            } else if (msg.what == 8) {
                textResponse.setText("connected with server Again!!!!!!");
            }

//            else if (msg.what == 1) {
//                textResponse.setText("send data to server");
//            } else {
//                textResponse.setText("something wrong");
//            }
        }
    };


    public void tryConnect() {
        while (true) {
            try {
                //if you change the phone as a server, then you need to consider to change the IP address.
                client = new Socket("192.168.3.50", 12345);
                if (client != null && client.isConnected()) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
            client = new Socket("192.168.3.55", 12345);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //循环接收来自服务器的消息
        while (true) {
            try {
                myHandler.sendEmptyMessage(5);
                byte[] b = new byte[1024];
                in = client.getInputStream();
                int count = in.read(b);
                if (count <= 0) {
                    break;
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

                if (str.equals("1")) {
                    isReceive = true;
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            //send data to server

                            while (isReceive) {
                                try {
                                    //wait for 3 second
                                    sleep(1000);

                                    if (!client.isConnected()) {
                                        myHandler.sendEmptyMessage(7);
                                        try {
                                            os.close();
                                            in.close();
                                            client.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        //try to connect the server until connected
                                        myHandler.sendEmptyMessage(7);
                                        tryConnect();
                                        myHandler.sendEmptyMessage(8);
                                        isReceive = false;
                                        break;
                                    }
                                    myHandler.sendEmptyMessage(5);
                                    os = client.getOutputStream();
                                    //write the signal strength data
                                    String data = null;
                                    data = String.valueOf(getSignalStrength());
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
                        }
                    };
                    new Thread(runnable).start();
                } else {
                    myHandler.sendEmptyMessage(6);
                    isReceive = false;
                }

            } catch (IOException e) {
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
}