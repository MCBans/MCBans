package com.mcbans.client;

import com.mcbans.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InventorySyncClient extends Client{
  public InventorySyncClient(Client c) {
    super(c);
  }
  public InventorySyncClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public static InventorySyncClient cast(Client client) {
    return new InventorySyncClient(client);
  }

  public byte[] convert(ItemStack[] itemStack) throws IOException {
    return ObjectSerializer.serializeUsingBukkit(Arrays.asList(itemStack));
  }
  public ItemStack[] convert(byte[] itemStack) throws IOException, ClassNotFoundException {
    return (ItemStack[]) ObjectSerializer.deserializeUsingBukkit(itemStack).toArray();
  }

  public void save(Player player, List<ItemStack[]> itemStacks) throws IOException, TooLargeException {
    sendCommand(ServerMCBansCommands.SavePlayerInventory);
    WriteToOutputStream.writeString(getOutputStream(), player.getUniqueId().toString().toLowerCase().replaceAll("-",""));
    WriteToOutputStream.writeByteArray(getOutputStream(), ObjectSerializer.serializeUsingBukkit(itemStacks));
    getOutputStream().flush();
    Command c = getCommand(getInputStream());
    switch (c.getCommand()){
      case 126:
        System.out.println("saved player inventory!");
        break;
      case 124:
        String errorMessage = ReadFromInputStream.readString(getInputStream(), 255);
        System.out.println(errorMessage);
        break;
      default:
        System.out.println("command "+c.getCommand());
        break;
    }
  }
  public List<ItemStack[]> get(Player player) throws IOException, TooLargeException, ClassNotFoundException {
    sendCommand(ServerMCBansCommands.GetPlayerInventory);
    WriteToOutputStream.writeString(getOutputStream(), player.getUniqueId().toString().toLowerCase().replaceAll("-",""));
    Command c = getCommand(getInputStream());
    switch (c.getCommand()){
      case 50:
        int length = Long.valueOf(ReadFromInputStream.readLong(getInputStream())).intValue();
        return ObjectSerializer.deserializeUsingBukkit(ReadFromInputStream.readByteData(getInputStream(), length));
      case 124:
        String errorMessage = ReadFromInputStream.readString(getInputStream(), 255);
        System.out.println(errorMessage);
        break;
      default:
        System.out.println("command "+c.getCommand());
        break;
    }
    return null;
  }
}
