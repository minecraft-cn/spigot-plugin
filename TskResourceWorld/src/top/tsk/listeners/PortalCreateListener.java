/**
 * projectName: TskResourceWorld
 * fileName: PortalCreateListener.java
 * packageName: top.tsk.listeners
 * buildDate: 2020-07-21 21:56
 */
package top.tsk.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import top.tsk.TskResourceWorld;

/**
 * listeners.PortalCreateListener
 * 传送门创建监听
 * 此处只能处理地狱门而不能处理末地门
 * 但是末地门是单向的，所以不用理会
 **/
public class PortalCreateListener implements Listener {

    @EventHandler
    public void portalCreate(PortalCreateEvent event) {
        if (TskResourceWorld.resWorldName.equals(event.getWorld().getName())) {
            event.setCancelled(true);
        }
    }
}