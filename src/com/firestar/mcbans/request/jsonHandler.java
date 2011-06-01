package com.firestar.mcbans.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unchecked")
public class jsonHandler{
	private String apiKey="";
	public jsonHandler(String api_key){
		apiKey=api_key;
	}
	public HashMap<String,String> mainRequest(HashMap<String,String> items){
		HashMap<String,String> out= new HashMap<String,String>();
		String url_req=this.urlparse(items);
		String json_text=this.request_from_api(url_req);
		JSONObject output=p.mcb_json.get_data(json_text);
		if(output!=null){
			
			Iterator<String> i = output.keys();
			if(i!=null){
				while(i.hasNext())
				{
				    String next = i.next();
				    try {
						out.put(next, output.getString(next));
					} catch (JSONException e) {
						System.out.println("mcbans error");
					}
				}
			}
		}
		return out;
	}
	public JSONObject get_data(String json_text){
	    try {
			JSONObject json = new JSONObject(json_text);
			return json;
		} catch (JSONException e) {
			
		}
		return null;
	}
	public String urlparse(HashMap<String,String> items){
		String data = "";
		try {
			for ( Map.Entry<String, String> entry : items.entrySet() ){
				String key = entry.getKey();
				String val = entry.getValue();
				if(data.equals("")){
					data = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				}else{
					data += "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				}
			}
		} catch (UnsupportedEncodingException e) {
			if(p.isdebug){
				p.mcb_handler.log(e.toString());
			}
		}
		return data;
	}
}