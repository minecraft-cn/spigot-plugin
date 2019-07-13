package top.tsk;

import com.sun.istack.internal.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.*;

public class TskFunBug extends JavaPlugin implements Listener {
  public boolean CreeperNoBlock = true;
  public boolean ShowExp = true;
  public boolean RideAny = true;
  public boolean LeashAny = false;
  public boolean TransferBuffToNonPlayer = true;
  public boolean HatEverything = true;
  public boolean DropPotionWhenKilledByPlayer = true;
  public boolean DropEquipmentForPoorPlayer = true;
  public boolean OverMaxEnchLevelToX = true;
  public boolean MoreExp = true;
  public boolean UpgradeOnKill = true;

  @Override
  public void onLoad() {
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveConfig();
    }
  }

  @Override
  public void onEnable() {
    TskUtils.LoadObject(this);
    getServer().getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDisable() {
    TskUtils.SaveObject(this);
  }

  @EventHandler
  public void MoreExp(EntityDeathEvent e) {
    if (!MoreExp) {
      return;
    }
    if (!(e.getEntity() instanceof Mob)) {
      return;
    }
    if (((Mob) e.getEntity()).getTarget() == null) {
      return;
    }

    int i = Double.valueOf(TskUtils.GetMaxHealth(e.getEntity())).intValue() / 10;
    e.setDroppedExp(e.getDroppedExp() + TskUtils.RandomInt(0, i));
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
    if (!ShowExp) {
      return;
    }
    event.getPlayer().sendMessage(String.format("Exp + %s%d", ChatColor.GREEN, event.getAmount()));
  }

  @EventHandler
  public void RideAny(PlayerInteractEntityEvent e) {
    if (!RideAny) {
      return;
    }
    ItemStack itemMainHand = e.getPlayer().getEquipment().getItemInMainHand();
    if (TskUtils.IsItemEmpty(itemMainHand)) {
      return;
    }
    if (itemMainHand.getType().equals(Material.SADDLE)) {
      e.setCancelled(true);
      e.getRightClicked().addPassenger(e.getPlayer());
    }
  }

  List<LivingEntity> getEntityLeashed(@NotNull Entity entity) {
    List<LivingEntity> livingEntities = new LinkedList<>();
    for (Entity entity1 : entity.getWorld().getEntities()) {
      if (entity1 instanceof LivingEntity) {
        try {
          if (((LivingEntity) entity1).getLeashHolder().equals(entity)) {
            livingEntities.add((LivingEntity) entity1);
          }
        } catch (IllegalStateException e) {
        }
      }
    }
    return livingEntities;
  }

  @EventHandler
  public void LeashAny(PlayerInteractEntityEvent e) {
    if (!LeashAny) {
      return;
    }

    ItemStack itemMainHand = e.getPlayer().getEquipment().getItemInMainHand();
    List<LivingEntity> entityLeashed = getEntityLeashed(e.getPlayer());

    // leash one
    if (!TskUtils.IsItemEmpty(itemMainHand) &&
      itemMainHand.getType().equals(Material.LEAD) &&
      e.getRightClicked() instanceof LivingEntity &&
      !entityLeashed.contains(e.getRightClicked())) {
      e.setCancelled(true);

      ((LivingEntity) e.getRightClicked()).setLeashHolder(e.getPlayer());
      itemMainHand.setAmount(itemMainHand.getAmount() - 1);
      e.getPlayer().getEquipment().setItemInMainHand(itemMainHand);
      return;
    }

    // take one
    if (e.getRightClicked() instanceof LivingEntity &&
      !((LivingEntity) e.getRightClicked()).getLeashHolder().isEmpty() &&
      !((LivingEntity) e.getRightClicked()).getLeashHolder().equals(e.getPlayer())) {
      e.setCancelled(true);
      ((LivingEntity) e.getRightClicked()).setLeashHolder(e.getPlayer());
      return;
    }


    // give all
    if ((TskUtils.IsItemEmpty(itemMainHand) || !TskUtils.IsItemEmpty(itemMainHand) && !itemMainHand.getType().equals(Material.LEAD)) &&
//      e.getRightClicked() instanceof LivingEntity &&
      !entityLeashed.isEmpty() && !entityLeashed.contains(e.getRightClicked())) {
      e.setCancelled(true);
      for (LivingEntity livingEntity : entityLeashed) {
        livingEntity.setLeashHolder(e.getRightClicked());
      }
      return;
    }

    // take all
    if (e.getRightClicked() instanceof LivingEntity &&
      !((LivingEntity) e.getRightClicked()).getLeashHolder().isEmpty() &&
      !((LivingEntity) e.getRightClicked()).getLeashHolder().equals(e.getPlayer())) {
      e.setCancelled(true);
      ((LivingEntity) e.getRightClicked()).setLeashHolder(e.getPlayer());
      return;
    }
  }

  Collection<PotionEffect> combinePotion(Collection<PotionEffect> p1, Collection<PotionEffect> p2) {
    Collection<PotionEffect> result = new LinkedList<>();
    Iterator<PotionEffect> iterator1 = p1.iterator();
    Iterator<PotionEffect> iterator2 = p2.iterator();
    while (iterator1.hasNext()) {
      PotionEffect potionEffect1 = iterator1.next();
      while (iterator2.hasNext()) {
        PotionEffect potionEffect2 = iterator2.next();
        if (potionEffect2.getType().equals(potionEffect1.getType())) {
          int amplifier;
          long duration;
          if (potionEffect1.getAmplifier() == potionEffect2.getAmplifier()) {
            amplifier = potionEffect1.getAmplifier() + 1;
            if (amplifier > 10) {
              amplifier = 10;
            }
            duration = potionEffect1.getDuration() + potionEffect2.getDuration();
            if (duration > Integer.MAX_VALUE) {
              duration = Integer.MAX_VALUE;
            }
          } else {
            amplifier = potionEffect1.getAmplifier() > potionEffect2.getAmplifier() ? potionEffect1.getAmplifier() : potionEffect2.getAmplifier();
            duration = potionEffect1.getDuration() > potionEffect2.getDuration() ? potionEffect1.getDuration() : potionEffect2.getDuration();
          }
          result.add(new PotionEffect(potionEffect1.getType(), ((int) duration), amplifier));
          iterator1.remove();
          iterator2.remove();
          break;
        }
      }
    }
    result.addAll(p1);
    result.addAll(p2);
    return result;
  }

  @EventHandler
  public void TransferBuffToNonPlayer(EntityDeathEvent event) {
    if (!TransferBuffToNonPlayer) {
      return;
    }
    if (event.getEntity().getKiller() != null) {
      return;
    }
    if (event.getEntity().getActivePotionEffects().isEmpty()) {
      return;
    }
    LivingEntity entity = event.getEntity();
    Entity killer = TskUtils.GetKiller(entity);
    if (killer == null) {
      return;
    }
    if (!(killer instanceof LivingEntity)) {
      return;
    }
    LivingEntity livingEntity = (LivingEntity) killer;
    if (livingEntity.getActivePotionEffects().isEmpty()) {
      livingEntity.addPotionEffects(entity.getActivePotionEffects());
    } else {
      Collection<PotionEffect> potionEffects = combinePotion(entity.getActivePotionEffects(), livingEntity.getActivePotionEffects());
      for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
        livingEntity.removePotionEffect(potionEffect.getType());
      }
      livingEntity.addPotionEffects(potionEffects);
    }

    livingEntity.setRemoveWhenFarAway(false);

    for (PotionEffect potionEffect : entity.getActivePotionEffects()) {
      entity.removePotionEffect(potionEffect.getType());
    }

    TskUtils.AddMaxHealth(livingEntity, TskUtils.GetMaxHealth(entity));
    TskUtils.Heal(livingEntity);
  }

  @EventHandler
  void HatEverything(InventoryClickEvent e) {
    if (!HatEverything) {
      return;
    }
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
  public void DropPotionWhenKilledByPlayer(EntityDeathEvent event) {
    if (!DropPotionWhenKilledByPlayer) {
      return;
    }
    if (event.getEntity() instanceof Player) {
      return;
    }
    if (event.getEntity().getKiller() == null) {
      return;
    }
    if (event.getEntity().getActivePotionEffects().isEmpty()) {
      return;
    }
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

  @EventHandler
  public void DropEquipmentForPoorPlayer(EntityDeathEvent event) {
    if (!DropEquipmentForPoorPlayer) {
      return;
    }
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

  @EventHandler
  public void OverMaxEnchLevelToX(PrepareAnvilEvent event) {
    if (!OverMaxEnchLevelToX) {
      return;
    }

    event.getInventory().setMaximumRepairCost(Integer.MAX_VALUE);

    ItemStack itemStack1 = event.getInventory().getItem(0);
    if (TskUtils.IsItemEmpty(itemStack1)) {
      return;
    }
    ItemStack itemStack2 = event.getInventory().getItem(1);
    if (TskUtils.IsItemEmpty(itemStack2)) {
      return;
    }
    ItemStack result = event.getResult();
    if (TskUtils.IsItemEmpty(result)) {
      return;
    }

    Map<Enchantment, Integer> enchants1 = TskUtils.GetEnchants(itemStack1);
    if (enchants1.isEmpty()) {
      return;
    }
    Map<Enchantment, Integer> enchants2 = TskUtils.GetEnchants(itemStack2);
    if (enchants2.isEmpty()) {
      return;
    }

    int extraCost = 0;
    Map<Enchantment, Integer> enchants3 = new HashMap<>();
    Iterator<Map.Entry<Enchantment, Integer>> iterator1 = enchants1.entrySet().iterator();
    while (iterator1.hasNext()) {
      Map.Entry<Enchantment, Integer> entry1 = iterator1.next();
      if (enchants2.containsKey(entry1.getKey())) {
        int level;
        if (entry1.getValue().equals(enchants2.get(entry1.getKey()))) {
          level = entry1.getValue() + 1;
          if (level > 10) {
            level = 10;
          }
          extraCost = extraCost + level;
        } else {
          level = entry1.getValue() > enchants2.get(entry1.getKey()) ? entry1.getValue() : enchants2.get(entry1.getKey());
        }
        iterator1.remove();
        enchants2.remove(entry1.getKey());
        enchants3.put(entry1.getKey(), level);
      }
    }


    for (Map.Entry<Enchantment, Integer> entry : enchants1.entrySet()) {
      enchants3.put(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<Enchantment, Integer> entry : enchants2.entrySet()) {
      enchants3.put(entry.getKey(), entry.getValue());
    }

    ItemMeta resultItemMeta = result.getItemMeta();
    if (resultItemMeta instanceof EnchantmentStorageMeta) {
      for (Map.Entry<Enchantment, Integer> entry : enchants3.entrySet()) {
        ((EnchantmentStorageMeta) resultItemMeta).addStoredEnchant(entry.getKey(), entry.getValue(), true);
      }
      result.setItemMeta(resultItemMeta);
    } else {
      result.addUnsafeEnchantments(enchants3);
    }

    event.setResult(result);
    event.getInventory().setRepairCost(event.getInventory().getRepairCost() + extraCost);

    if (event.getInventory().getRepairCost() >= 40) {
      event.getView().getPlayer().sendMessage(String.format("将花费经验%s%d%s级", ChatColor.GREEN, event.getInventory().getRepairCost(), ChatColor.RESET));
    }
  }
//
//  String textures = "textures";
//
//  String texturesValue = "eyJ0aW1lc3RhbXAiOjE1NjI1MDUzNDY3MzYsInByb2ZpbGVJZCI6IjY3NDg3MTE2ODQxYzQ4MjI4YjJjNWFiMDRmMjBiYTZhIiwicHJvZmlsZU5hbWUiOiJMdXB1c1RleGFzIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U5Yjg0MDkzNTIzYzk1Y2U4YTY1MDMxMGE0MmQ5NDU3ZDNhN2VkOGY3YzNhNTc3MTM0MWUzNjUzOTFhMDg0MTAiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==";
//
//  @EventHandler
//  public void setSkin(PlayerJoinEvent event) {
//    System.out.println(event.getPlayer().getAddress());
//    CraftPlayer craftPlayer = (CraftPlayer) event.getPlayer();
////    craftPlayer.getProfile().getProperties().put(textures, new Property(textures, texturesValue));
//    for (Field field : craftPlayer.getClass().getFields()) {
//      System.out.println(field.getName() + "\t" + field.getType());
//    }
//  }

  @EventHandler
  public void UpgradeOnKill(EntityDeathEvent event) {
    if (!UpgradeOnKill) {
      return;
    }

    // if killed by player, return
    if (event.getEntity().getKiller() != null) {
      return;
    }

    LivingEntity entity = event.getEntity();
    Entity killer = TskUtils.GetKiller(entity);
    if (killer == null) {
      return;
    }
    if (!(killer instanceof LivingEntity)) {
      return;
    }

    LivingEntity livingEntity = (LivingEntity) killer;
    TskUtils.AddMaxHealth(livingEntity, TskUtils.GetMaxHealth(entity));
    TskUtils.Heal(livingEntity);
    livingEntity.setRemoveWhenFarAway(false);
  }
}


