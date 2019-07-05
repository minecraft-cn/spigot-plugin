package top.tsk;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.Collection;

public class TskFunBug extends JavaPlugin implements Listener {
  boolean CreeperNoBlock = true;
  boolean PlayerExpChangeEvent = true;

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
  public void CreeperNoBlock(EntityExplodeEvent e) {
    if (!CreeperNoBlock) {
      return;
    }
    if (e.getEntityType().equals(EntityType.CREEPER)) {
      e.blockList().clear();
    }
  }

  @EventHandler
  public void ShowExp(PlayerExpChangeEvent event) {
    if (!PlayerExpChangeEvent) {
      return;
    }
    event.getPlayer().sendMessage(String.format("Exp + %s%d", ChatColor.GREEN, event.getAmount()));
  }

  @EventHandler
  public void RideAny(PlayerInteractEntityEvent e) {
    ItemStack itemMainHand = e.getPlayer().getEquipment().getItemInMainHand();
    if (TskUtils.IsItemEmpty(itemMainHand)) {
      return;
    }
    if (itemMainHand.getType().equals(Material.SADDLE)) {
      e.setCancelled(true);
      e.getRightClicked().addPassenger(e.getPlayer());
    }
  }

  @EventHandler
  public void LeashAny(PlayerInteractEntityEvent e) {
    ItemStack itemMainHand = e.getPlayer().getEquipment().getItemInMainHand();
    if (TskUtils.IsItemEmpty(itemMainHand)) {
      return;
    }
    if (itemMainHand.getType().equals(Material.LEAD)) {
      if (e.getRightClicked() instanceof LivingEntity) {
        e.setCancelled(true);
        ((LivingEntity) e.getRightClicked()).setLeashHolder(e.getPlayer());
      }
    }
  }

  @EventHandler
  public void TransferBuffToMonster(PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
      Entity damager = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
      if (damager instanceof LivingEntity) {
        if (player.getActivePotionEffects().isEmpty()) {
          return;
        }
        Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
        ((LivingEntity) damager).addPotionEffects(potionEffects);
        ((LivingEntity) damager).setRemoveWhenFarAway(false);
        for (PotionEffect potionEffect : potionEffects) {
          player.removePotionEffect(potionEffect.getType());
        }
      }
    }
  }

  @EventHandler
  void HatEverything(InventoryClickEvent e) {
    if (e.getClickedInventory() == null) {
      return;
    }
    if (
      e.getClickedInventory().getType().equals(InventoryType.PLAYER) &&
        e.getSlotType().equals(InventoryType.SlotType.ARMOR) &&
        e.getSlot() == 39 &&
        e.getRawSlot() == 5 &&
        e.getClick().equals(ClickType.LEFT) &&
        !TskUtils.IsItemEmpty(e.getCursor())
    ) {
      e.setCancelled(true);
      ItemStack currentItem = e.getCurrentItem();
      e.setCurrentItem(e.getCursor());
      e.getWhoClicked().getOpenInventory().setCursor(currentItem);
    }
  }

  @EventHandler
  public void DropPotionOnDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() != null) {
      // 再加一个条件，比如玩家需要有【炼药】附魔或者手执火焰棒
      // 写到这里，突然想到，需要考虑玩家体验。你说以一种什么样的方式，玩家会玩得比较舒服，而不是喷【这是什么脑残设计？】
      Collection<PotionEffect> activePotionEffects = event.getEntity().getActivePotionEffects();
      for (PotionEffect effect : activePotionEffects) {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionMeta potionMeta = ((PotionMeta) itemStack.getItemMeta());
        potionMeta.addCustomEffect(effect, true);
        potionMeta.setColor(effect.getType().getColor());
//                potionMeta.setDisplayName(ChatColor.RESET + effect.getType().getName());
        itemStack.setItemMeta(potionMeta);
        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack);
      }
    }
  }

  @EventHandler
  public void DropEquipmentForPoorPlayer(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Monster)) {
      return;
    }
    Monster monster = (Monster) event.getEntity();
    Player player = monster.getKiller();
    if (player == null) {
      return;
    }
    EntityEquipment playerEquipment = player.getEquipment();
    if (playerEquipment == null) {
      return;
    }
    if (monster.getEquipment() == null) {
      return;
    }
    if (TskUtils.IsItemEmpty(playerEquipment.getItemInMainHand())
      && !event.getDrops().contains(monster.getEquipment().getItemInMainHand())) {
      event.getDrops().add(monster.getEquipment().getItemInMainHand());
    }
    if (TskUtils.IsItemEmpty(playerEquipment.getItemInOffHand())
      && !event.getDrops().contains(monster.getEquipment().getItemInOffHand())) {
      event.getDrops().add(monster.getEquipment().getItemInOffHand());
    }
    if (TskUtils.IsItemEmpty(playerEquipment.getBoots())
      && !event.getDrops().contains(monster.getEquipment().getBoots())) {
      event.getDrops().add(monster.getEquipment().getBoots());
    }
    if (TskUtils.IsItemEmpty(playerEquipment.getLeggings())
      && !event.getDrops().contains(monster.getEquipment().getLeggings())) {
      event.getDrops().add(monster.getEquipment().getLeggings());
    }
    if (TskUtils.IsItemEmpty(playerEquipment.getChestplate())
      && !event.getDrops().contains(monster.getEquipment().getChestplate())) {
      event.getDrops().add(monster.getEquipment().getChestplate());
    }
    if (TskUtils.IsItemEmpty(playerEquipment.getHelmet())
      && !event.getDrops().contains(monster.getEquipment().getHelmet())) {
      event.getDrops().add(monster.getEquipment().getHelmet());
    }
  }
}
    
