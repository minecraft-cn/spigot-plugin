/**
 * projectName: TskResourceWorld
 * fileName: InteractListener.java
 * packageName: top.tsk.listeners
 * buildDate: 2020-07-21 15:12
 */
package top.tsk.listeners;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import top.tsk.TskResourceWorld;

/**
 * listeners.InteractListener
 * 交互监听
 * 监听玩家右键
 **/
public class InteractListener implements Listener {

    private TskResourceWorld resWorld;

    /**
     * 构造
     */
    public InteractListener(TskResourceWorld resWorld) {
        this.resWorld = resWorld;
    }

    private boolean isBed(Material material) {
        if(Material.BLACK_BED == material) return true;
        if(Material.BLUE_BED == material) return true;
        if(Material.BROWN_BED == material) return true;
        if(Material.CYAN_BED == material) return true;
        if(Material.GRAY_BED == material) return true;
        if(Material.GREEN_BED == material) return true;
        if(Material.LIGHT_BLUE_BED == material) return true;
        if(Material.LIGHT_GRAY_BED == material) return true;
        if(Material.LIME_BED == material) return true;
        if(Material.MAGENTA_BED == material) return true;
        if(Material.ORANGE_BED == material) return true;
        if(Material.PINK_BED == material) return true;
        if(Material.PURPLE_BED == material) return true;
        if(Material.RED_BED == material) return true;
        if(Material.WHITE_BED== material) return true;
        return Material.YELLOW_BED == material;
    }

    /**
     * 监听器 playerInteract
     * 响应玩家交互事件
     */
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (null == block) return;

        if (Material.BEACON == block.getType()) { //玩家交互的方块是信标
            if (player.getLocation().add(0, -1, 0).getBlock().getType().equals(Material.BEACON)) {  //玩家站在信标上方
                event.setCancelled(true);

                if(TskResourceWorld.resWorldName.equals(world.getName())) {
                    resWorld.quitResWorld(player);  //在资源世界中则离开世界
                } else {
                    if (player.getLevel() >= 15) {
                        player.setLevel(player.getLevel() - 15);
                        //扣经验

                        player.sendMessage("扣费成功"); //temp

                        resWorld.joinResWorld(player);

                    } else {
                        player.sendMessage("进入资源世界需花费15级经验等级");
                    }
                }
            }
        }

        if(!TskResourceWorld.resWorldName.equals(world.getName())) return;

        //在资源世界中禁止使用末影箱和床

        if (Material.ENDER_CHEST == block.getType()) {  //末影箱
            event.setCancelled(true);
            block.setType(Material.AIR);
            player.getWorld().strikeLightning(player.getLocation());
        }

        if (isBed(block.getType())) {   //床
            event.setCancelled(true);
            block.setType(Material.AIR);
            player.getWorld().strikeLightning(player.getLocation());
        }
    }
}