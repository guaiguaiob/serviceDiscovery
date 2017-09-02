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


public class serviceDiscovery extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
         callbackContext.success(action);
        if (action.equals("getNetworkServices")) {
            callbackContext.success("getNetworkServices");
            JSONObject options = new JSONObject();
            try {
                options.put("urn", "tagfans-com:device:tbox:1");
                options.put("time", 10);
            } catch(Exception e) {
                e.printStackTrace();
              }
            DevDiscovery task = new DevDiscovery(cordova.getActivity()) {
                @Override
                protected void onProgressUpdate(Integer... values) {
                    callbackContext.success("OK");
                }

                @Override
                protected void onPostExecute(HashSet<String> devices) {
                    callbackContext.success("OK");
                }

            };
            task.execute(options);
        } 
        return true;
    }
}


    

