package com.tagfans.plugin;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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
        JSONObject options = params[0];
        String protocol = "ssdp";

        if (options.has("protocol")) {
            try {
                String prot = options.getString("protocol");
                if ("ssdp".equalsIgnoreCase(prot)) {
                    protocol = "ssdp";
                } else if ("ip-scan".equalsIgnoreCase(prot)) {
                    protocol = "ip-scan";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        addresses = new HashSet<String>();

        if (protocol.equalsIgnoreCase("ip-scan")) {
            return doScan(options);
        } else {
            return doSSDP(options);
        }
    }
    private HashSet<String> doScan(JSONObject options) {
        WifiManager wifiMgr = (WifiManager)ctx.getSystemService( ctx.getApplicationContext().WIFI_SERVICE );
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        Log.d(TAG, ipAddress);
        String ip3 = ipAddress.substring(0, ipAddress.lastIndexOf('.')+1);
        Log.d(TAG, ip3);

        for(int i=0; i<256; i++) {
            String testIp = ip3+i;
            Socket socket=new Socket();
            try {
                socket.connect(new InetSocketAddress(testIp,14444), 50);
                socket.setSoTimeout(1000);

                DataOutputStream output = null;
                DataInputStream input = null;

                output = new DataOutputStream( socket.getOutputStream() );


                OutputStream outstream = socket.getOutputStream();
                PrintWriter out = new PrintWriter(outstream);
                String toSend = "{\"cmd\":\"ping\"}";
                out.print(toSend);
                out.flush();

                InputStream instrem = socket.getInputStream();
                StringBuilder sb = new StringBuilder();

                int c;

                while ( (( c = instrem.read() ) >= 0) && (c != 0x0a /* <LF> */) ) {
                    if ( c != 0x0d /* <CR> */ ) {
                        sb.append( (char)c );
                    } else {
                        // Ignore <CR>.
                    }
                    Log.d(TAG, sb.toString());
                }
                JSONObject jo = new JSONObject(sb.toString());
                if("pong".equals(jo.getString("result")))
                    addresses.add(testIp);
                socket.close();
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d(TAG, testIp + " failed");
            }
            int percentage = (int)Math.ceil(i*100/255);
            Log.d(TAG, ""+percentage);
            publishProgress(percentage);
        }

        Log.d(TAG, ip3);

        return addresses;
    }
    private HashSet<String> doSSDP(JSONObject options) {
        int searchTime = 3; //seconds
        String urn = "urn:tagfans-com:device:tbox:1";

        if(options.has("time")) {
            try {
                searchTime = options.getInt("time");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(searchTime > MAX_TIME)
            searchTime = MAX_TIME;

        WifiManager wifiMgr = (WifiManager)ctx.getSystemService( ctx.getApplicationContext().WIFI_SERVICE );
        if(wifiMgr != null) {

            WifiManager.MulticastLock lock = wifiMgr.createMulticastLock("The Lock");
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
