package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;


import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class serverChoose implements Runnable {
	private final BukkitInterface MCBans;
	public serverChoose(BukkitInterface p){
		MCBans = p;
	}
	@Override
	public void run() {
		MCBans.notSelectedServer = true;
		MCBans.log("Looking for fastest api server!");
		long d = 99999;
		for(String server : MCBans.apiServers.split(",")){
			try{
				long pingTime = (System.currentTimeMillis());
				JsonHandler webHandle = new JsonHandler( MCBans );
				HashMap<String, String> items = new HashMap<String, String>();
				items.put( "exec", "check" );
				String urlReq = webHandle.urlparse(items);
				String jsonText = webHandle.request_from_api(urlReq,server);
				if(jsonText.equals("up")){
					long ft = ((System.currentTimeMillis())-pingTime);
					if(d>ft){
						d = ft;
						MCBans.apiServer = server;
						MCBans.log("API Server found: "+server+" :: response time: "+ft);
					}
				}
			}catch(IllegalArgumentException e){
			}catch(NullPointerException e){
			}
		}
		MCBans.log("Fastest server selected: "+MCBans.apiServer+" :: response time: "+d);
		MCBans.notSelectedServer = false;
	}
}