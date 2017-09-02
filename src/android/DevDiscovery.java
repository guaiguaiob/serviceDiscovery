package com.tagfans.plugin;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

public class DevDiscovery extends AsyncTask<JSONObject, Integer, HashSet<String>> {
    final static String TAG = "DevDiscovery";
    final static int MAX_TIME = 30;//seconds
    private Context ctx;
    private HashSet<String> addresses=null;

    public DevDiscovery(Context context) {
        ctx = context;
    }

    @Override
    protected HashSet<String> doInBackground(JSONObject[] params) {
        int searchTime = 3; //seconds
        String urn = "ssdp:all";

        addresses = new HashSet<String>();
        JSONObject options = params[0];
        try {
            if(options.has("urn")) {
                urn = "urn:"+options.getString("urn");
            }
            if(options.has("time")) {
                searchTime = options.getInt("time");
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, "parse params failed");
            return null;
        }
        if(searchTime > MAX_TIME)
            searchTime = MAX_TIME;

        WifiManager wifi = (WifiManager)ctx.getSystemService( ctx.getApplicationContext().WIFI_SERVICE );

        if(wifi != null) {

            WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
            lock.acquire();

            DatagramSocket socket = null;

            try {

                InetAddress group = InetAddress.getByName("239.255.255.250");
                int port = 1900;
                String query =
                        "M-SEARCH * HTTP/1.1\r\n" +
                                "HOST: 239.255.255.250:1900\r\n"+
                                "MAN: \"ssdp:discover\"\r\n"+
                                "MX: 1\r\n"+
                                "ST: "+urn+"\r\n"+
                                "\r\n";
                Log.d(TAG, query);
                socket = new DatagramSocket(port);
                socket.setReuseAddress(true);
                socket.setSoTimeout(200);

                DatagramPacket dgram = new DatagramPacket(query.getBytes(), query.length(),
                        group, port);
                socket.send(dgram);

                long time = System.currentTimeMillis();
                long curTime = System.currentTimeMillis();

                // Let's consider all the responses we can get in 1 second
                searchTime = searchTime*1000;
                Log.d(TAG, "search " + searchTime + "ms");
                while (true) {
                    DatagramPacket p = new DatagramPacket(new byte[12], 12);
                    try {
                        socket.receive(p);
                        Log.d(TAG, "got data");
                        String s = new String(p.getData(), 0, p.getLength());
                        if (s.toUpperCase().equals("HTTP/1.1 200")) {
                            addresses.add(p.getAddress().getHostAddress());
                        }
                    } catch(java.net.SocketTimeoutException e) {
                        //continue;
                    }

                    curTime = System.currentTimeMillis();
                    long diff = curTime - time;
                    Log.d(TAG, "diff : " + diff);
                    int percentage = (int)Math.ceil(diff*100/searchTime);
                    publishProgress(percentage);
                    if(diff >= searchTime) {
                        break;
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                socket.close();
            }
            lock.release();
        }
        return addresses;
    }

    public HashSet<String> getDevices() {
        return addresses;
    }
}
