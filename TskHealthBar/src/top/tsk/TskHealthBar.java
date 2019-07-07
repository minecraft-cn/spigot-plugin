package top.tsk;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.utils.TskUtils;
import top.tsk.utils.event.EntityDamageByPlayerEvent;
import top.tsk.utils.event.EventCaller;

public class TskHealthBar extends JavaPlugin implements Listener {

  HealthBarManager healthBarManager = new HealthBarManager(this);

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(new EventCaller(), this);
    getServer().getPluginManager().registerEvents(this, this);

    getServer().getPluginManager().registerEvents(healthBarManager, this);
    getServer().getPluginManager().registerEvents(healthBarManager, this);
    getServer().getPluginManager().registerEvents(healthBarManager, this);
    getServer().getPluginManager().registerEvents(healthBarManager, this);
    healthBarManager.removeBarFromPlayerForTimeout();
  }


  @EventHandler
  public void damageByPlayer(EntityDamageByPlayerEvent event) {
    Entity entity = event.getEvent().getEntity();
    if (entity instanceof Boss) {
      return;
    }

    if (!healthBarManager.HealthBarList.containsKey(entity.getUniqueId())) {
      healthBarManager.HealthBarList.put(entity.getUniqueId(), new HealthBarView(healthBarManager.createBossBar(entity)));
    }

    if (!healthBarManager.HealthBarList.get(entity.getUniqueId()).timeoutList.containsKey(event.getPlayer().getName())) {
      healthBarManager.HealthBarList.get(entity.getUniqueId()).bossBar.addPlayer(event.getPlayer());
      healthBarManager.HealthBarList.get(entity.getUniqueId()).timeoutList.put(event.getPlayer().getName(), HealthBarManager.TIMEOUT);
    }
  }

  @EventHandler
  public void damageHealth(EntityDamageEvent event) {
    Entity entity = event.getEntity();

    if (!(entity instanceof Damageable)) {
      return;
    }
    if (!(entity instanceof Attributable)) {
      return;
    }

    double health = ((Damageable) entity).getHealth();
    health = health - event.getFinalDamage();
    double maxHealth = TskUtils.GetMaxHealth(((Attributable) entity));
    if ((health < 0)) {
      health = 0;
    }

    Bukkit.getPluginManager().callEvent(new HealthChangeEvent(((Damageable) entity), health / maxHealth));
  }

  @EventHandler
  public void removeBarOnDeath(EntityDeathEvent event) {
    Entity entity = event.getEntity();


    Bukkit.getPluginManager().callEvent(new HealthChangeEvent(((Damageable) entity), 0));
  }

  @EventHandler
  public void healHealth(EntityRegainHealthEvent event) {
    Entity entity = event.getEntity();

    if (!(entity instanceof Damageable)) {
      return;
    }
    if (!(entity instanceof Attributable)) {
      return;
    }

    double health = ((Damageable) entity).getHealth();
    health = health + event.getAmount();
    double maxHealth = TskUtils.GetMaxHealth(((Attributable) entity));
    if (health > maxHealth) {
      health = maxHealth;
    }

    Bukkit.getPluginManager().callEvent(new HealthChangeEvent(((Damageable) entity), health / maxHealth));
  }
}
