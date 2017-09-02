package com.tagfans.plugin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import java.util.ArrayList;
import java.util.HashSet;

import android.net.wifi.WifiManager;
import android.util.Log;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class serviceDiscovery extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("getNetworkServices")) {
            String urn = "urn:tagfans-com:device:tbox:1";
            int searchTime = 10;     
        //    HashSet<String> addresses=null;
            Context ctx = cordova.getActivity();
          //  addresses = new HashSet<String>();
 

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
                   
                    socket = new DatagramSocket(port);
                    socket.setReuseAddress(true);
                    socket.setSoTimeout(200);

                    DatagramPacket dgram = new DatagramPacket(query.getBytes(), query.length(),
                            group, port);
                    socket.send(dgram);

                    long time = System.currentTimeMillis();
                    long curTime = System.currentTimeMillis();
      
                    searchTime = searchTime*1000;

                    while (true) {
                        DatagramPacket p = new DatagramPacket(new byte[12], 12);
                        try {
                            socket.receive(p);
                            String s = new String(p.getData(), 0, p.getLength());
                            if (s.toUpperCase().equals("HTTP/1.1 200")) {
                                callbackContext.success(p.getAddress().getHostAddress());
                                //addresses.add(p.getAddress().getHostAddress());
                            }
                        } catch(java.net.SocketTimeoutException e) {
                            //continue;
                        }

                        curTime = System.currentTimeMillis();
                        long diff = curTime - time;

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

        } 
        return true;
    }
}


    

