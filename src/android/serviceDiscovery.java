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


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class serviceDiscovery extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("getNetworkServices")) {
                JSONObject options = new JSONObject();
                try {
                    options.put("urn", "tagfans-com:device:tbox:1");
                    options.put("time", 10);
                } catch(Exception e) {
                    return false;
                }


                DevDiscovery task = new DevDiscovery(cordova.getActivity()) {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
						callbackContext.success(showResult(this.getDevices()));
                    }

                    @Override
                    protected void onPostExecute(HashSet<String> devices) {
						callbackContext.success(showResult(devices));
                    }

                };
                task.execute(options);
				
        } 
        return true;
    }
	
	private String showResult(HashSet<String> devices) {
        String res = "";
        if(devices!=null && devices.size()>=0) {
            for(String dev:devices) {
                res = (res) ? res+","+dev : dev; 
            }
        }     
		return res;
    }

}


    

