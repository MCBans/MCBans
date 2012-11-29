package com.mcbans.firestar.mcbans.api.data;

import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.util.Util;

public class BanLookupData {
    //final static DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private int banID;

    private String name;
    private String server;
    private String admin;
    private String reason;
    private String date;
    //private Date date = null;
    private double lostRep = 0;
    private String type;

    public BanLookupData(final int banID, final JSONObject response) throws JSONException, NullPointerException{
        if (banID < 0 || response == null) return;

        this.banID = banID;

        this.name = response.getString("player");
        this.admin = response.getString("admin");
        this.reason = response.getString("reason");
        this.server = response.getString("server");
        this.date = response.getString("date");
        this.type = response.getString("type");

        if (Util.isDouble(response.getString("reploss"))){
            this.lostRep = Double.parseDouble(response.getString("reploss"));
        }
        /*
        try {
            this.date = format.parse(this.dateStr);
        }catch (ParseException ex){
            this.date = null;
        }
         */
    }

    public int getBanID(){
        return this.banID;
    }

    public String getPlayerName(){
        return this.name;
    }
    public String getServer(){
        return this.server;
    }
    public String getAdminName(){
        return this.admin;
    }
    public String getReason(){
        return this.reason;
    }
    public String getDate(){
        return this.date;
    }
    public double getLostRep(){
        return this.lostRep;
    }
    public String getType(){
        return this.type;
    }
}
