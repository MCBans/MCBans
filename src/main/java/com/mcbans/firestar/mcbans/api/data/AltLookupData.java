package com.mcbans.firestar.mcbans.api.data;

import java.util.HashMap;

import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.util.Util;
//import java.util.concurrent.ConcurrentHashMap;

public class AltLookupData {
    private String playerName;

    private int altCount;
    HashMap<String, Double> altMap = new HashMap<String, Double>();
    
    public AltLookupData(final String playerName, final JSONObject response) throws JSONException, NullPointerException{
        if (playerName == null || response == null) return;

        this.playerName = playerName;
        
        if (Util.isInteger(response.getString("altListCount"))){
            this.altCount = Integer.parseInt(response.getString("altListCount"));
        }

        String altList[] = response.getString("altList").split(",");
        String repList[] = response.getString("repList").split(",");
        
        if (altList.length != repList.length){
            return;
        }
        
        altMap.clear();
        for (int i = 0; i < altList.length; i++){
            String repStr = repList[i].trim();
            if (!Util.isDouble(repStr)) continue;
            altMap.put(altList[i].trim(), Double.parseDouble(repStr));
        }
    }
    
    public String getPlayerName(){
        return this.playerName;
    }
    
    public int getAltCount(){
        return this.altCount; //or this.altMap.size()
    }
    public HashMap<String, Double> getAltMap(){
        return this.altMap;
    }
}