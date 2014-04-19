package com.mcbans.firestar.mcbans.api.data;

import java.util.ArrayList;
import java.util.List;

import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.util.Util;

public class PlayerLookupData {
    private String name;

    private int total = 0;
    private double reputation = 10.0D;

    private List<String> global = new ArrayList<String>();
    private List<String> local = new ArrayList<String>();
    private List<String> other = new ArrayList<String>();

    public PlayerLookupData(final String name, final JSONObject response) throws JSONException, NullPointerException{
        if (name == null || response == null) return;

        this.name = name;
        if (response.has("player")){
        	this.name = response.getString("player");
        }
        if (Util.isInteger(response.getString("total").trim())){
            total = Integer.parseInt(response.getString("total").trim());
        }

        if (Util.isDouble(response.getString("reputation").trim())){
            reputation = Double.parseDouble(response.getString("reputation").trim());
        }

        if (response.getJSONArray("global").length() > 0) {
            for (int v = 0; v < response.getJSONArray("global").length(); v++) {
                global.add(response.getJSONArray("global").getString(v));
            }
        }
        if (response.getJSONArray("local").length() > 0) {
            for (int v = 0; v < response.getJSONArray("local").length(); v++) {
                local.add(response.getJSONArray("local").getString(v));
            }
        }
        if (response.getJSONArray("other").length() > 0) {
            for (int v = 0; v < response.getJSONArray("other").length(); v++) {
                other.add(response.getJSONArray("other").getString(v));
            }
        }
    }

    public String getPlayerName(){
        return this.name;
    }

    public int getTotal(){
        return this.total;
    }
    public double getReputation(){
        return this.reputation;
    }

    public List<String> getGlobals(){
        return this.global;
    }
    public List<String> getLocals(){
        return this.local;
    }
    public List<String> getOthers(){
        return this.other;
    }
}
