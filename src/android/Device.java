package com.tagfans.plugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Device {
    private String ip;
    private Integer port;
    private String name;
    private String usn;
    private String cacheControl;
    private Map<String, String> extras = new HashMap<>();

    public Device() {
    }

    public Device(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        return this.ip;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void addExtra(String key, String value) {
        this.extras.put(key, value);
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }

    public JSONObject toJSON() throws JSONException {

        JSONObject extraJson = new JSONObject(this.extras);
        JSONObject jo = new JSONObject();
        jo.put("ip", ip);
        jo.put("port", port);
        jo.put("extras", extraJson);

        return jo;
    }
}
