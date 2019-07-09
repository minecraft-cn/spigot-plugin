package top.tsk;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.tsk.utils.TskUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

class HealthBarView {
  BossBar bossBar;
  Map<String, Integer> timeoutList;

  public HealthBarView(BossBar bossBar) {
    this.bossBar = bossBar;
    timeoutList = new HashMap<>();
  }
}

public class HealthBarManager implements Listener {
  Map<UUID, HealthBarView> HealthBarList;
  Plugin plugin;
  static final int TIMEOUT = 3;

  public HealthBarManager(Plugin plugin) {
    this.plugin = plugin;
    HealthBarList = new HashMap<>();
  }

  public void removeBarFromPlayerForTimeout() {
    new BukkitRunnable() {
      @Override
      public void run() {
        for (Map.Entry<UUID, HealthBarView> entry : HealthBarList.entrySet()) {
          Iterator<Map.Entry<String, Integer>> entryIterator = entry.getValue().timeoutList.entrySet().iterator();
          while (entryIterator.hasNext()) {
            Map.Entry<String, Integer> integerEntry = entryIterator.next();
            integerEntry.setValue(integerEntry.getValue() - 1);
            if (integerEntry.getValue() <= 0) {
              Player player = Bukkit.getPlayer(integerEntry.getKey());
              if (player != null) {
                entry.getValue().bossBar.removePlayer(player);
              }
              entryIterator.remove();
            }
          }
        }
      }
    }.runTaskTimer(plugin, 0, 20 * 1);
  }

  public static BossBar createBossBar(Entity entity) {
    BarColor barColor = BarColor.WHITE;
    if (entity instanceof Monster) {
      barColor = BarColor.RED;
    }
    if (entity instanceof Animals) {
      barColor = BarColor.GREEN;
    }
    if (entity instanceof Player) {
      barColor = BarColor.YELLOW;
    }
    if (entity instanceof WaterMob) {
      barColor = BarColor.BLUE;
    }
    if (entity instanceof Villager) {
      barColor = BarColor.PINK;
    }
    BarStyle barStyle = BarStyle.SOLID;
    if (entity instanceof Attributable) {
      Attributable attributable = (Attributable) entity;
      switch (Double.valueOf(TskUtils.GetMaxHealth(attributable)).intValue()) {
        case 6:
          barStyle = BarStyle.SEGMENTED_6;
          break;
        case 10:
          barStyle = BarStyle.SEGMENTED_10;
          break;
        case 12:
          barStyle = BarStyle.SEGMENTED_12;
          break;
        case 20:
          barStyle = BarStyle.SEGMENTED_20;
          break;
      }
    }
    BossBar bossBar = Bukkit.createBossBar("", barColor, barStyle, BarFlag.CREATE_FOG);
    bossBar.removeFlag(BarFlag.CREATE_FOG);
    return bossBar;
  }

  @EventHandler
  public void updateBarForPlayer(HealthChangeEvent event) {
    Damageable damageable = event.getDamageable();
    if (!HealthBarList.containsKey(damageable.getUniqueId())) {
      return;
    }
    if (event.getProgress() <= 0) {
      HealthBarList.get(damageable.getUniqueId()).bossBar.removeAll();
      HealthBarList.remove(damageable.getUniqueId());
      return;
    }
    if (HealthBarList.get(damageable.getUniqueId()).bossBar.getProgress() != event.getProgress()) {
      HealthBarList.get(damageable.getUniqueId()).bossBar.setProgress(event.getProgress());
    }
    for (Map.Entry<String, Integer> entry : HealthBarList.get(damageable.getUniqueId()).timeoutList.entrySet()) {
      entry.setValue(TIMEOUT);
    }
  }
}
