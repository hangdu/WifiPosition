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
import android.util.Log;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
//import java.util.logging.Logger;

import static android.content.ContentValues.TAG;

public class clientSocketDemo {
    String dstAddress;
    int dstPort;
    String response = "";
    TextView textResponse;
    FingerPrint fingerPrint;
    Socket mSocket = null;
//    boolean isReceive = true;

    InputStream in = null;
    OutputStream os = null;

    String SOCKET_HOST = "192.168.3.50";
    int SOCKET_PORT = 12345;
    DataOutputStream mDataOutputStream;
    private SocketReadThread mReadThread;
    private static final long HEART_BEAT_RATE = 4 * 1000;
    private long sendTime = 0L;
//    private long signalStrengthSendTime = 0L;

    public clientSocketDemo(TextView textResponse) {
        this.textResponse = textResponse;
//        connectToServer();
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
                textResponse.setText("connected with server!!!!!!");
            } else if (msg.what == 9) {
                textResponse.setText("Read from server");
            } else if (msg.what == 10) {
                textResponse.setText("Trying to connect to server but failed");
            } else if (msg.what == 12) {
                textResponse.setText("send heartBeat");
            }
        }
    };


    private Runnable mHeartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {//每隔4秒检测一次
                boolean isSuccess = sendHeartBeatMsg("");
                if (!isSuccess) {
                    Log.i(TAG, "连接已断开，正在重连……");
                    myHandler.sendEmptyMessage(7);
                    myHandler.removeCallbacks(mHeartBeatRunnable);// 移除线程，重连时保证该线程已停止上次调用时的工作
                    mReadThread.release();//释放SocketReadThread线程资源
                    releaseLastSocket();
                    connectToServer();// 再次调用connectToServer方法，连接服务端
                }
            }
            myHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };


    /**
     * 连接服务端
     */
    public void connectToServer() {
        Thread connectThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mSocket = new Socket();
                    mSocket.connect(new InetSocketAddress(SOCKET_HOST, SOCKET_PORT));
                    Log.e(TAG, "连接成功  " + SOCKET_HOST);
                    myHandler.sendEmptyMessage(8);
                    mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                    // 开启线程负责读取服务端数据
                    mReadThread = new SocketReadThread();
                    mReadThread.start();

                    // 心跳检测，检测socket是否连接
                    myHandler.postDelayed(mHeartBeatRunnable, HEART_BEAT_RATE);

                } catch (IOException e) {
                    myHandler.sendEmptyMessage(10);
                    Log.e(TAG, "连接失败  ");
                    e.printStackTrace();
                }
            }
        });
        connectThread.start();
    }



    /**
     * 发送心跳包
     *
     * @param msg
     * @return
     */
    public boolean sendHeartBeatMsg(String msg) {
        if (null == mSocket) {
            return false;
        }
        try {
            if (!mSocket.isClosed() && !mSocket.isOutputShutdown()) {
                String message = "heartbeat";
//                mDataOutputStream.write(message.getBytes());
                mDataOutputStream.writeUTF(message);
                mDataOutputStream.flush();
                myHandler.sendEmptyMessage(12);
                sendTime = System.currentTimeMillis();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    /**
     * 断开连接
     *
     */
    private void releaseLastSocket() {
        try {
            if (null != mSocket) {
                if (!mSocket.isClosed()) {
                    mSocket.close();
                }
            }
            mSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    void handleStringMsg(String data) {
//        if (data.equals(1)) {
//
//        }
//    }

//    public class SocketWriteThread extends Thread {
//        private static final String TAG = "SocketWriteThread";
//        private volatile boolean mStopThread = false;
//
//        public void relase() {
//            mStopThread = true;
//        }
//
//    }




    public class SocketReadThread extends Thread {
        private static final String TAG = "SocketReadThread";
        private volatile boolean mStopThread = false;

        public void release() {
            mStopThread = true;
            releaseLastSocket();
        }

        @Override
        public void run() {
            DataInputStream mInputStream = null;
            try {
                mInputStream = new DataInputStream(mSocket.getInputStream());
                Logger.d(TAG, "SocketThread running!");
                while (!mStopThread) {
                    String resultStr = mInputStream.readUTF();
//                    handleStringMsg(resultStr);
                    myHandler.sendEmptyMessage(9);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mInputStream != null) {
                    try {
                        mInputStream.close();
                        mInputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    @Override
//    public void run() {
//        // TODO Auto-generated method stub
//        super.run();
//        try {
//            client = new Socket("192.168.3.55", 12345);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //循环接收来自服务器的消息
//        while (true) {
//            try {
//                myHandler.sendEmptyMessage(5);
//                byte[] b = new byte[1024];
//                in = client.getInputStream();
//                int count = in.read(b);
//                if (count <= 0) {
//                    break;
//                }
//                byte temp[] = new byte[count];
//                for (int i = 0; i < count; i++) {
//                    temp[i] = b[i];
//                }
//                String str = new String(temp);
//                Message msg = new Message();
//                msg.obj = str;
//                msg.what = 0;
//                myHandler.sendMessage(msg);
//
//                if (str.equals("1")) {
//                    isReceive = true;
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            //send data to server
//
//                            while (isReceive) {
//                                try {
//                                    //wait for 3 second
//                                    sleep(1000);
//
//                                    if (!client.isConnected()) {
//                                        myHandler.sendEmptyMessage(7);
//                                        try {
//                                            os.close();
//                                            in.close();
//                                            client.close();
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        //try to connect the server until connected
//                                        myHandler.sendEmptyMessage(7);
////                                        tryConnect();
//                                        myHandler.sendEmptyMessage(8);
//                                        isReceive = false;
//                                        break;
//                                    }
//                                    myHandler.sendEmptyMessage(5);
//                                    os = client.getOutputStream();
//                                    //write the signal strength data
//                                    String data = null;
//                                    data = String.valueOf(getSignalStrength());
//                                    System.out.print(data);
//                                    os.write(data.getBytes());
//                                    os.flush();
//                                    myHandler.sendEmptyMessage(1);
//                                } catch (IOException e) {
//                                    myHandler.sendEmptyMessage(2);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    };
//                    new Thread(runnable).start();
//                } else {
//                    myHandler.sendEmptyMessage(6);
//                    isReceive = false;
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            os.close();
//            in.close();
//            client.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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


    //    private Runnable sendSignalStrengthRunnable = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//
//
//
//        }
//    }


}