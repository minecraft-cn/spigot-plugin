package top.tsk.utils.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EventCaller implements Listener {


  @EventHandler
  public void CallEntityDamageByPlayerEvent(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player) {
      Bukkit.getPluginManager().callEvent(new EntityDamageByPlayerEvent(event, ((Player) event.getDamager())));
    } else {
      if (event.getDamager() instanceof Projectile) {
        if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
          Bukkit.getPluginManager().callEvent(new EntityDamageByPlayerEvent(event, ((Player) ((Projectile) event.getDamager()).getShooter())));
        }
      }
    }
  }
}
