package com.github.re_storage.asumitrust;

import com.github.re_storage.asumitrust.manager.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners
  implements Listener {
  @EventHandler
  public void dataLoadOnJoin(PlayerJoinEvent event) {
    String uuid = event.getPlayer().getUniqueId().toString();
    DataManager.readAsync(uuid);
  }

  @EventHandler
  public void dataUnloadOnQuit(PlayerQuitEvent event) {
    String uuid = event.getPlayer().getUniqueId().toString();
    DataManager.unload(uuid, true);
  }
}
