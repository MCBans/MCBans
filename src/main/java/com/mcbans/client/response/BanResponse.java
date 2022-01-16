package com.mcbans.client.response;

import com.mcbans.domain.models.client.Ban;

import java.util.List;
import java.util.stream.Collectors;

public class BanResponse {
  private String uuid = "";
  private String name = "";
  private List<Ban> bans;
  private double reputation;
  private Ban ban;
  private boolean mcbansStaff;

  public BanResponse(String uuid, String name, List<Ban> bans, double reputation, Ban ban, boolean mcbansStaff) {
    this.uuid = uuid;
    this.name = name;
    this.bans = bans;
    this.reputation = reputation;
    this.ban = ban;
    this.mcbansStaff = mcbansStaff;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public List<Ban> getBans() {
    return bans;
  }

  public void setBans(List<Ban> bans) {
    this.bans = bans;
  }

  public double getReputation() {
    return reputation;
  }

  public void setReputation(double reputation) {
    this.reputation = reputation;
  }

  public Ban getBan() {
    return ban;
  }

  public void setBan(Ban ban) {
    this.ban = ban;
  }

  public boolean isMCBansStaff() {
    return mcbansStaff;
  }

  public void setMCBansStaff(boolean mcbansStaff) {
    this.mcbansStaff = mcbansStaff;
  }

  @Override
  public String toString() {
    String banList = "";
    if (bans.size() > 0) {
      banList = bans.stream().map(ban ->
        "\n    " + ban.getReason() +
          "\n      id: " + ban.getId() +
          ((ban.getServer()!=null)?"\n      server: " + ban.getServer().getAddress():"") +
          ((ban.getAdmin()!=null)?"\n      admin: " + ban.getAdmin().getName() +" ( "+ban.getAdmin().getUuid()+" )":"") +
          "\n      repLoss: " + ban.getReputation()
      ).collect(Collectors.joining());
    }
    return "\n  Name: " + name + "\n  UUID: " + uuid + (isMCBansStaff() ? "\n  This is a MCBans Staff" : "") + "\n  Reputation: " + this.getReputation() + "\n  Bans:" + banList + ((ban != null) ? "\n  Ban Status:\n    Type: " + ban.getType() + "\n    Reason: " + ban.getReason() : "");
  }
}
