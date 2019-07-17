package top.tsk;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.Arrays;

public class TskTidyChest extends JavaPlugin implements Listener {
  @Override
  public void onLoad() {
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveConfig();
    }
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDisable() {
    saveConfig();
  }

  @EventHandler
  public void tidyChest(InventoryClickEvent e) {
    if (e.getClickedInventory() == null) {
      return;
    }
    if (!e.getClick().equals(ClickType.MIDDLE)) {
      return;
    }
    if (e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE) && !TskUtils.IsItemEmpty(e.getCurrentItem())) {
      return;
    }
    if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
      return;
    }
    e.setCancelled(true);
    ItemStack[] itemStacks = e.getClickedInventory().getStorageContents();
    Arrays.sort(itemStacks, (o1, o2) -> {
      if (TskUtils.IsItemEmpty(o1) && TskUtils.IsItemEmpty(o2)) {
        return 0;
      }
      if (TskUtils.IsItemEmpty(o1)) {
        return 1;
      }
      if (TskUtils.IsItemEmpty(o2)) {
        return -1;
      }
      return o1.getType().toString().compareTo(o2.getType().toString());
    });
    e.getClickedInventory().setStorageContents(itemStacks);
    if (e.getWhoClicked() instanceof Player) {
      ((Player) e.getWhoClicked()).updateInventory();
    }
  }
}