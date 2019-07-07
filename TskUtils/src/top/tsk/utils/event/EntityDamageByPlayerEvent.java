package top.tsk.utils.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByPlayerEvent extends Event {

  private static final HandlerList handlerList = new HandlerList();
  private EntityDamageByEntityEvent event;
  private Player player;

  EntityDamageByPlayerEvent(EntityDamageByEntityEvent event, Player player) {
    this.event = event;
    this.player = player;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  public EntityDamageByEntityEvent getEvent() {
    return event;
  }

  public Player getPlayer() {
    return player;
  }
}
