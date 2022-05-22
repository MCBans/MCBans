package com.mcbans.plugin.actions;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;
import org.bukkit.BanList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class UnbanSync {
  private final MCBans plugin;
  private final ActionLog log;
  public UnbanSync(MCBans plugin){
    this.plugin = plugin;
    this.log = plugin.getLog();
  }
  public void handle(InputStream is, OutputStream os) throws IOException, TooLargeException {
    int numberOfUnbans = ReadFromInputStream.readInt(is, false);
    for(int i=0;i<numberOfUnbans;i++){
      String uuid = ReadFromInputStream.readString(is, 32, false);
      plugin.getOfflineBanList().remove(uuid);
      WriteToOutputStream.writeBoolean(os,true);
    }
    plugin.getOfflineBanList().save();
  }
}
