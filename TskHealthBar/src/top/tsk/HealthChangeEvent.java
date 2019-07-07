package top.tsk;

import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HealthChangeEvent extends Event {

  private static final HandlerList handlerList = new HandlerList();

  private Damageable damageable;
  private double progress;

  public HealthChangeEvent(Damageable damageable, double progress) {
    this.damageable = damageable;
    this.progress = progress;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  public Damageable getDamageable() {
    return damageable;
  }


  public double getProgress() {
    return progress;
  }
}
