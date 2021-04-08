package top.tsk.world.world;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.ResourceWorld;

public abstract class ResWorldBaseWrapper implements Listener, ResWorld {

    public final String resWorldName;
    public final String mainWorldName;
    public final int worldDuration;
    public final int worldSize;

    ResWorldBaseWrapper(JavaPlugin plugin) {
        this.resWorldName = plugin.getConfig().getString("resworldname", "resource_world");
        this.mainWorldName = plugin.getConfig().getString("mainworldname", "world");
        this.worldSize = plugin.getConfig().getInt("worldsize", 2048);
        this.worldDuration = 20 * plugin.getConfig().getInt("worldduration", 2 * 24 * 60 * 60);
    }

    //============Listener==============================================================================================

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (null == block) return;

        if (!player.getWorld().getName().equals(resWorldName))
            return;

        //在资源世界中禁止使用末影箱和床

        if (Material.ENDER_CHEST == block.getType()) {  //末影箱
            event.setCancelled(true);
            block.setType(Material.AIR, true);
            player.getWorld().strikeLightning(player.getLocation());
        }

        if (isBed(block.getType())) {   //床
            event.setCancelled(true);
            block.setType(Material.AIR, true);
            player.getWorld().strikeLightning(player.getLocation());
        }
    }

    @EventHandler
    public void portalCreate(PortalCreateEvent event) {
        //禁止传送门建立
        if (event.getWorld().getName().equals(resWorldName)) {
            event.setCancelled(true);
        }
    }

    //============Tick==================================================================================================

    private int timer;

    @Override
    public boolean onTick() {
        this.timer += 1;
        return this.timer <= worldDuration;
    }

    //============Override==============================================================================================

    @Override
    public World getWorld() {
        return Bukkit.getWorld(resWorldName);
    }

    @Override
    public boolean isJoinable() {
        return this.worldDuration - this.timer >= 10 * 60 * 20;
    }

    @Override
    public WorldCreator getWorldCreator() {
        WorldCreator worldCreator = new WorldCreator(this.resWorldName);
        worldCreator.type(WorldType.NORMAL);
        worldCreator.generateStructures(false);
        return worldCreator;
    }

    @Override
    public void built(World world) {
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(2 * this.worldSize);
    }

    @Override
    public void deploy(boolean isBuild) {
        if (isBuild) {
            this.timer = 0;
        } else {
            this.timer = ResourceWorld.getStorage().getInt("worldtimer", 0);
        }
    }

    @Override
    public void undeploy() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        PortalCreateEvent.getHandlerList().unregister(this);
    }

    @Override
    public void save() {
        ResourceWorld.getStorage().setData("worldtimer", this.timer);
    }

    //============Others================================================================================================

    private boolean isBed(Material material) {
        if (Material.BLACK_BED == material) return true;
        if (Material.BLUE_BED == material) return true;
        if (Material.BROWN_BED == material) return true;
        if (Material.CYAN_BED == material) return true;
        if (Material.GRAY_BED == material) return true;
        if (Material.GREEN_BED == material) return true;
        if (Material.LIGHT_BLUE_BED == material) return true;
        if (Material.LIGHT_GRAY_BED == material) return true;
        if (Material.LIME_BED == material) return true;
        if (Material.MAGENTA_BED == material) return true;
        if (Material.ORANGE_BED == material) return true;
        if (Material.PINK_BED == material) return true;
        if (Material.PURPLE_BED == material) return true;
        if (Material.RED_BED == material) return true;
        if (Material.WHITE_BED == material) return true;
        return Material.YELLOW_BED == material;
    }
}