package com.mcbans.firestar.mcbans.request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.ConfigurationManager;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;

public class JsonHandler {
    private final MCBans plugin;
    private final ActionLog log;
    private final ConfigurationManager config;

    public JsonHandler(MCBans plugin) {
        this.plugin = plugin;
        this.log = plugin.getLog();
        this.config = plugin.getConfigs();
    }

    public JSONObject get_data(String json_text) {
        try {
            return new JSONObject(json_text);
        } catch (JSONException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> mainRequest(HashMap<String, String> items) {
        HashMap<String, String> out = new HashMap<String, String>();
        String url_req = this.urlparse(items);
        String json_text = this.request_from_api(url_req);
        if (config.isDebug()){
            log.info("Requested: '" + url_req + "'");
            log.info("Converting response '" + json_text + "'");
        }
        if (json_text == null || json_text.length() <= 0){
            if (config.isDebug()) log.severe("Null Response! Please contact MCBans administrator!");
            out.clear();
            return out;
        }
        
        JSONObject output = this.get_data(json_text);
        if (output != null) {
            Iterator<String> i = output.keys();
            if (i != null) {
                while (i.hasNext()) {
                    String next = i.next();
                    try {
                        out.put(next, output.getString(next));
                    } catch (JSONException e) {
                        if (config.isDebug()) {
                            log.severe("JSON Error On Retrieval!");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return out;
    }

    public JSONObject hdl_jobj(HashMap<String, String> items) {
        String urlReq = urlparse(items);
        String jsonText = request_from_api(urlReq);
        return get_data(jsonText);
    }

    public String request_from_api(String data) {
        return request_from_api(data, plugin.apiServer);
    }

    public String request_from_api(String data, String server) {
        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        try {
            // check valid api key
            if (!config.isValidApiKey()){
                return "";
            }
            if (config.isDebug()) {
                log.info("Sending API request: '" + data + "'");
            }
            URL url = new URL("http://" + server + "/v3/" + config.getApiKey());
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(25000);
            conn.setReadTimeout(25000);
            conn.setDoOutput(true);
            
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            
            StringBuilder buf = new StringBuilder();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                buf.append(line);
            }
            
            if (plugin.getConfigs().isDebug()) {
                log.info("Result: " + buf.toString());
            }
            return buf.toString();
        } catch (Exception e) {
            if (config.isDebug()) {
                log.severe("Error fetching data!");
                e.printStackTrace();
            }
            return "";
        } finally {
            if (wr != null){
                try { wr.close(); } catch (Exception e) {}
            }
            if (rd != null){
                try { rd.close(); } catch (Exception e) {}
            }
        }
    }

    public String urlparse(HashMap<String, String> items) {
        String data = "";
        try {
            for (Map.Entry<String, String> entry : items.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                if(val!=null && !val.equals("")){
                	if (data.equals("")) {
                    	data = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
                	} else {
                		data += "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
                	}
                }
            }
        } catch (UnsupportedEncodingException e) {
            if (config.isDebug()) {
                e.printStackTrace();
            }
        }
        return data;
    }
}