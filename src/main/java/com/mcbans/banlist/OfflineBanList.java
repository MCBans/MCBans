package com.mcbans.banlist;

import com.mcbans.plugin.MCBans;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OfflineBanList {
  MCBans plugin;
  Map<String, BannedPlayer> bannedPlayers = new HashMap<>();
  public OfflineBanList(MCBans plugin) throws IOException, ClassNotFoundException {
    File bannedPlayerFile = new File(plugin.getDataFolder(), "banned_players.db");
    if(bannedPlayerFile.exists()){
      FileInputStream fileIn = new FileInputStream(bannedPlayerFile);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      bannedPlayers = (Map<String, BannedPlayer>) in.readObject();
      in.close();
      fileIn.close();
    }
    //System.out.println(bannedPlayers.size() + " banned players.");
    this.plugin = plugin;
  }

  public BannedPlayer get(String uuid){
    return bannedPlayers.getOrDefault(uuid, null);
  }

  public BannedPlayer getByPlayerName(String playerName){
    for(Map.Entry<String, BannedPlayer> bp: bannedPlayers.entrySet()){
      if(bp.getValue().getPlayerName()!=null && bp.getValue().getPlayerName().equalsIgnoreCase(playerName)){
        return bp.getValue();
      }
    }
    return null;
  }

  public boolean isBanned(String uuid){
    BannedPlayer bannedPlayer = bannedPlayers.getOrDefault(uuid, null);
    if(bannedPlayer!=null){
      return bannedPlayer.isBanned();
    }
    return false;
  }

  public BannedPlayer get(UUID uuid){
    return bannedPlayers.getOrDefault(uuid.toString().replaceAll("-", "").toLowerCase(), null);
  }

  public boolean isBanned(UUID uuid){
    BannedPlayer bannedPlayer = bannedPlayers.getOrDefault(uuid.toString().replaceAll("-", "").toLowerCase(), null);
    if(bannedPlayer!=null){
      return bannedPlayer.isBanned();
    }
    return false;
  }

  public void addBan(UUID uuid, BannedPlayer bannedPlayer){
    bannedPlayers.put(uuid.toString().replaceAll("-", "").toLowerCase(), bannedPlayer);
  }

  public void addBan(String uuid, BannedPlayer bannedPlayer){
    bannedPlayers.put(uuid, bannedPlayer);
  }

  public void remove(UUID uuid){
    bannedPlayers.remove(uuid.toString().replaceAll("-", "").toLowerCase());
  }

  public void remove(String uuid){
    bannedPlayers.remove(uuid);
  }
  public void clear(){
    bannedPlayers.clear();
  }

  public List<BannedPlayer> unSynced(){
    return bannedPlayers.entrySet().stream().filter(b->b.getValue().getBanId()==null || !b.getValue().isBanned()).map(b->b.getValue()).collect(Collectors.toList());
  }

  public void save() throws IOException {
    File bannedPlayerFile = new File(plugin.getDataFolder(), "banned_players.db");
    if(bannedPlayerFile.exists()){
      bannedPlayerFile.delete();
    }
    FileOutputStream fileOut = new FileOutputStream(bannedPlayerFile);
    ObjectOutputStream out = new ObjectOutputStream(fileOut);
    out.writeObject(bannedPlayers);
    out.close();
    fileOut.close();
  }
}
