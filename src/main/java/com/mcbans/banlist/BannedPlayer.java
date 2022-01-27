package com.mcbans.banlist;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class BannedPlayer implements Serializable {
  Long banId=null;
  String playerName=null;
  String playerUUID=null;
  String reason=null;
  String server=null;
  String admin=null;
  String adminUUID=null;
  Date date=null;
  Long expires=null;
  String type=null;
  boolean banned = true;

  public BannedPlayer() {
  }


  public BannedPlayer(Long banId, String type, String playerName, String reason, String server, String admin, Date date, Long expires) {
    this.type = type;
    this.banId = banId;
    this.playerName = playerName;
    this.reason = reason;
    this.server = server;
    this.admin = admin;
    this.date = date;
    this.expires = expires;
  }

  public BannedPlayer(Long banId, String type, String playerName, String playerUUID, String reason, String server, String admin, String adminUUID, Date date, Long expires) {
    this.type = type;
    this.banId = banId;
    this.playerName = playerName;
    this.playerUUID = playerUUID;
    this.reason = reason;
    this.server = server;
    this.admin = admin;
    this.adminUUID = adminUUID;
    this.date = date;
    this.expires = expires;
  }

  public BannedPlayer(String type, String playerName, String playerUUID, String reason, String admin, String adminUUID, Date date, Long expires) {
    this.type = type;
    this.playerName = playerName;
    this.playerUUID = playerUUID;
    this.reason = reason;
    this.admin = admin;
    this.adminUUID = adminUUID;
    this.date = date;
    this.expires = expires;
  }

  public BannedPlayer(Long banId, String type, String playerName, String playerUUID, String reason, String server, String admin, String adminUUID, Date date) {
    this.type = type;
    this.banId = banId;
    this.playerName = playerName;
    this.playerUUID = playerUUID;
    this.reason = reason;
    this.server = server;
    this.admin = admin;
    this.adminUUID = adminUUID;
    this.date = date;
  }

  public Long getBanId() {
    return banId;
  }

  public void setBanId(Long banId) {
    this.banId = banId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getPlayerUUID() {
    return playerUUID;
  }

  public void setPlayerUUID(String playerUUID) {
    this.playerUUID = playerUUID;
  }

  public String getAdminUUID() {
    return adminUUID;
  }

  public void setAdminUUID(String adminUUID) {
    this.adminUUID = adminUUID;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getAdmin() {
    return admin;
  }

  public void setAdmin(String admin) {
    this.admin = admin;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Long getExpires() {
    return expires;
  }

  public void setExpires(Long expires) {
    this.expires = expires;
  }

  public boolean isBanned() {
    return banned;
  }

  public void setBanned(boolean banned) {
    this.banned = banned;
  }
}
