package com.tagfans.plugin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import java.util.ArrayList;
import java.util.HashSet;

public class serviceDiscovery extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("getNetworkServices")) {
            JSONObject options = new JSONObject();
            try {
                options.put("urn", "tagfans-com:device:tbox:1");
                options.put("time", 10);
            } catch(Exception e) {
                e.printStackTrace();
              }
            DevDiscovery task = new DevDiscovery(MainActivity.this) {
                @Override
                protected void onProgressUpdate(Integer... values) {
                    callbackContext.success(this.getDevices());
                }

                @Override
                protected void onPostExecute(HashSet<String> devices) {
                    callbackContext.success(devices);
                }

            };
            task.execute(options);
        } else {
            return false;
        }
    }
}


    

