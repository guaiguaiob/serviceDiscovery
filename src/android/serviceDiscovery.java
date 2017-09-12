package com.tagfans.plugin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.text.TextUtils;
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
					options.put("protocol", "ip-scan");
					options.put("threads", 10);
                } catch(Exception e) {
                    return false;
                }


                DevDiscovery task = new DevDiscovery(cordova.getActivity()) {
                    @Override
                    protected void onPostExecute(HashSet<String> devices) {
						callbackContext.success(TextUtils.join(",", devices));
                    }
                };
                task.execute(options);
				
        } 
        return true;
    }
	

}


    



