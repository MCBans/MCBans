package com.mcbans.firestar.mcbans.request;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

class webHandler extends Thread {
	private String apiKey="";
	private String requestData="";
	public webHandler(String api_key, String data){
		apiKey=api_key;
		requestData=data;
	}
	public void run() {
		try {
			URL url = new URL("http://72.10.39.172/"+apiKey);
    	    URLConnection conn = url.openConnection();
    	    conn.setConnectTimeout(8000);
    	    conn.setReadTimeout(15000);
    	    conn.setDoOutput(true);
    	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    	    wr.write(requestData);
    	    wr.flush();
    	    wr.close();
		} catch (Exception e) {
			e.printStackTrace();
    	}
	}
}