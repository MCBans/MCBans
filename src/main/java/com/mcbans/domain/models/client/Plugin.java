package com.mcbans.domain.models.client;

import java.io.Serializable;
import java.util.List;

public class Plugin implements Serializable {
  String name;
  String version;
  List<String> authors;
  List<String> permissions;

  public Plugin() {
  }

  public Plugin(String name, String version, List<String> authors, List<String> permissions) {
    this.name = name;
    this.version = version;
    this.authors = authors;
    this.permissions = permissions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }
}
