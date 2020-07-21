package top.tsk;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.listeners.BlockBreakListener;
import top.tsk.listeners.ExtendVanillaGenerator;
import top.tsk.listeners.InteractListener;
import top.tsk.listeners.PortalCreateListener;

import java.io.IOException;
import java.util.List;
import java.util.Random;


public class TskResourceWorld extends JavaPlugin {

    public static final String resWorldName = "ResWorld";
    public static final String mainWorldName = "world";
    public static final int worldSize = 2000;

    private InteractListener interactListener = new InteractListener(this);
    private ExtendVanillaGenerator extendVanillaGenerator = new ExtendVanillaGenerator();
    private PortalCreateListener portalCreateListener = new PortalCreateListener();
    private BlockBreakListener blockBreakListener = new BlockBreakListener();

    private World resWorld = null;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(interactListener, this);
        getServer().getPluginManager().registerEvents(extendVanillaGenerator, this);
        getServer().getPluginManager().registerEvents(portalCreateListener, this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);

        getLogger().info("TskResourceWorld has been deployed!");

        //temp
        createResWorld();
    }

    /**
     * 方法 createResWorld
     * 建立新资源世界
     */
    public void createResWorld() {
        resWorld = Bukkit.getWorld(resWorldName);
        if (null != resWorld) return;

        WorldCreator worldCreator = new WorldCreator(resWorldName);
        worldCreator.type(WorldType.NORMAL);
        Bukkit.createWorld(worldCreator);

        //设定世界边界
        resWorld = Bukkit.getWorld(resWorldName);
        assert resWorld != null;

        Random rnd = new Random();
        //tmp
        Location location = new Location(resWorld, rnd.nextInt(14514) - 7257, 100, rnd.nextInt(14514) - 7257);
        resWorld.setSpawnLocation(location);

        WorldBorder worldBorder = resWorld.getWorldBorder();
        worldBorder.setCenter(location);
        worldBorder.setSize(2 * worldSize);
    }

    /**
     * 方法 destroyResWorld
     * 销毁当前资源世界
     */
    public void destroyResWorld() {
        resWorld = Bukkit.getWorld(resWorldName);
        if (null == resWorld) return;   //TODO: 非法调用，此处应当报错

        List<Player> playerList = resWorld.getPlayers();
        for (Player player : playerList) {
            player.damage(114514);  //玩家死亡后会回到传送进来的信标处
        }


        Bukkit.unloadWorld(resWorldName, true);
        try {
            FileUtils.deleteDirectory(resWorld.getWorldFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }

        resWorld = null;
    }

    private boolean isShulkerBox(Material material) {
        if (Material.BLACK_SHULKER_BOX == material) return true;
        if (Material.BLUE_SHULKER_BOX == material) return true;
        if (Material.BROWN_SHULKER_BOX == material) return true;
        if (Material.CYAN_SHULKER_BOX == material) return true;
        if (Material.GRAY_SHULKER_BOX == material) return true;
        if (Material.GREEN_SHULKER_BOX == material) return true;
        if (Material.LIGHT_BLUE_SHULKER_BOX == material) return true;
        if (Material.LIGHT_GRAY_SHULKER_BOX == material) return true;
        if (Material.LIME_SHULKER_BOX == material) return true;
        if (Material.MAGENTA_SHULKER_BOX == material) return true;
        if (Material.ORANGE_SHULKER_BOX == material) return true;
        if (Material.PINK_SHULKER_BOX == material) return true;
        if (Material.PURPLE_SHULKER_BOX == material) return true;
        if (Material.RED_SHULKER_BOX == material) return true;
        if (Material.SHULKER_BOX == material) return true;
        if (Material.WHITE_SHULKER_BOX == material) return true;
        return Material.YELLOW_SHULKER_BOX == material;
    }

    /**
     * 方法 joinResWorld
     * 玩家进入资源世界
     */
    public void joinResWorld(Player player) {
        if (null == resWorld) {
            player.sendMessage("资源世界尚未创建");
            return;
        }

        World world = player.getWorld();
        Location location = player.getLocation();
        PlayerInventory inventory = player.getInventory();

        ItemStack stack = inventory.getItemInOffHand();
        if (isShulkerBox(stack.getType())) {
            world.dropItem(location, stack);
            stack.setType(Material.AIR);
            inventory.setItemInOffHand(stack);
        }

        stack = inventory.getHelmet();
        if (null != stack) {
            if (isShulkerBox(stack.getType())) {
                world.dropItem(location, stack);
                stack.setType(Material.AIR);
                inventory.setHelmet(stack);
            }
        }

        ItemStack[] itemStacks = inventory.getStorageContents();
        for (ItemStack stack1 : itemStacks) {
            if (null != stack1) {
                world.dropItem(location, stack1);
                stack1.setType(Material.AIR);
            }
            inventory.setStorageContents(itemStacks);
        }

        Random rnd = new Random();
        while (true) {
            int x = resWorld.getSpawnLocation().getBlockX() + rnd.nextInt(worldSize) - worldSize / 2;
            int z = resWorld.getSpawnLocation().getBlockZ() + rnd.nextInt(worldSize) - worldSize / 2;
            int y = 255;
            for (; y > 0; --y) {
                Block tBlock = resWorld.getBlockAt(x, y, z);
                if (!tBlock.isEmpty() && !tBlock.isLiquid()) {
                    player.teleport(new Location(resWorld, x, y, z));
                    return;
                }
            }
        }
    }

    /**
     * 方法 quitResWorld
     * 玩家主动退出资源世界
     */
    public void quitResWorld(Player player) {
        Location tLoc = player.getBedSpawnLocation();
        if (null == tLoc) {
            World world = Bukkit.getWorld(mainWorldName);
            if (null == world) return;   //TODO: 主世界名字错误，应输出异常
            tLoc = world.getSpawnLocation();
        }

        player.teleport(tLoc);
    }

}