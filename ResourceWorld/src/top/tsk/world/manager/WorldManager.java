package top.tsk.world.manager;

import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import top.tsk.ResourceWorld;
import top.tsk.world.world.ResWorld;

import java.io.IOException;

abstract public class WorldManager {

    protected final int resRebuildDuration;

    private ResWorld resWorld;
    protected BukkitTask tickTask;
    private JavaPlugin plugin;

    int worldCode;

    public WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.resRebuildDuration = 20 * plugin.getConfig().getInt("rebuildduration", 24 * 60 * 60);
    }

    //============Build&Destroy=========================================================================================

    private boolean isWorldRun;

    private boolean loadWorld(boolean isBuild) {
        if (isBuild) {
            this.resWorld = getNextResWorldQue();
        } else {
            this.resWorld = getCurResWorld();
        }

        resWorld.deploy(isBuild);

        if (resWorld instanceof Listener) {
            plugin.getServer().getPluginManager().registerEvents((Listener) resWorld, plugin);
        }

        World world = Bukkit.createWorld(resWorld.getWorldCreator());
        if (null == world) {
            Bukkit.getLogger().warning("[ResourceWorld]A serious error prevented the creation of the resource world");
            Bukkit.getLogger().warning("[ResourceWorld]The system temporal logic of the resource world has been suspended");
            resWorld.undeploy();
            return false;
        }

        resWorld.built(world);

        return true;
    }

    protected void destroyWorld() {
        resWorld.undeploy();

        World world = resWorld.getWorld();
        Bukkit.unloadWorld(world, true);

        this.isWorldRun = false;

        try {
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean buildWorld() {
        if (this.isWorldRun) {
            Bukkit.getLogger().warning("[ResourceWorld]Build a world when another world is running.");
            return false;
        }

        this.worldCode += 1;
        if (loadWorld(true)) {
            this.isWorldRun = true;
            return true;
        }

        return false;
    }

    //====================Interfaces====================

    public int getWorldCode() {
        return worldCode;
    }

    public String getWorldInfo() {
        if (isWorldRun)
            return resWorld.info();
        else
            return "No World is Running";
    }

    public boolean rebuildWorldFromCommand() {
        if (this.isWorldRun)
            destroyWorld();

        return buildWorld();
    }

    public boolean destroyWorldFromCommand() {
        if (this.isWorldRun) {
            destroyWorld();
            return true;
        }

        return false;
    }

    public boolean playerJoinFromCommand(Player player) {
        if (isPlayerInResWorld(player))
            return false;
        playerJoin(player, player.getLocation());
        return true;
    }

    public boolean playerQuitFromCommand(Player player) {
        if (!isPlayerInResWorld(player))
            return false;
        playerQuit(player);
        return true;
    }

    public void deploy(JavaPlugin plugin) {
        //读取存储的数据
        this.worldCode = ResourceWorld.getStorage().getInt("worldcode", 0);
        this.isWorldRun = ResourceWorld.getStorage().getBoolean("isworldrun", false);

        if (isWorldRun) loadWorld(false);

        this.tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                onTick();
            }
        }.runTaskTimer(plugin, 0, 1);

        if (this instanceof Listener)
            plugin.getServer().getPluginManager().registerEvents((Listener) this, plugin);
    }

    public void save() {
        ResourceWorld.getStorage().setData("worldcode", this.worldCode);
        ResourceWorld.getStorage().setData("isworldrun", this.isWorldRun);

        resWorld.save();
    }

    //============interface=============================================================================================

    protected ResWorld getResWorld() {
        return this.resWorld;
    }

    protected boolean isWorldRun() {
        return this.isWorldRun;
    }

    abstract protected void onTick();

    abstract protected void playerJoin(Player player, Location backLoc);

    abstract protected void playerQuit(Player player);

    abstract protected boolean isPlayerInResWorld(Player player);

    abstract protected ResWorld getNextResWorldQue();

    abstract protected ResWorld getCurResWorld();

}
