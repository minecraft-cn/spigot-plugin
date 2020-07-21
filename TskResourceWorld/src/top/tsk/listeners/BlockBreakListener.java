/**
 * projectName: TskResourceWorld
 * fileName: BlockBreakListener.java
 * packageName: top.tsk.listeners
 * buildDate: 2020-07-21 23:15
 */
package top.tsk.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * top.tsk.listeners.BlockBreakListener
 * 方块破坏监听
 **/
public class BlockBreakListener implements Listener {

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (Material.BEACON == block.getType()) {
            event.setDropItems(false);
        }
    }
}