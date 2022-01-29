package com.mcbans.domain.models.client;

import java.io.Serializable;

public class Player implements Serializable {
  String name;
  long playerId;
  String uuid;

  public Player() {
  }

  public Player(long playerId, String name, String uuid) {
    this.name = name;
    this.playerId = playerId;
    this.uuid = uuid;
  }

  public Player(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getPlayerId() {
    return playerId;
  }

  public void setPlayerId(long playerId) {
    this.playerId = playerId;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
