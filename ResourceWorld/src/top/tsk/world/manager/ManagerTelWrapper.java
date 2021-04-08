package top.tsk.world.manager;

import net.minecraft.server.v1_16_R3.MathHelper;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBeacon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import top.tsk.ResourceWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class ManagerTelWrapper extends ManagerBaseWrapper implements Listener {

    protected final int telDelay;
    protected final String mainWorldName;
    protected final String resWorldName;

    public ManagerTelWrapper(JavaPlugin plugin) {
        super(plugin);
        this.telDelay = 20 * plugin.getConfig().getInt("teldelay", 5);
        this.resWorldName = plugin.getConfig().getString("resworldname", "resource_world");
        this.mainWorldName = plugin.getConfig().getString("mainworldname", "world");

        this.states = new HashMap<>();
    }

    @Override
    public void save() {
        super.save();

        for (Map.Entry<String, PlayerState> it : this.states.entrySet()) {
            it.getValue().save(it.getKey());
        }
    }

    //============Listener==============================================================================================

    //模块上来讲，此方法做的是监听玩家在信标上方行走的事件
    @EventHandler
    public void listenerPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerState state = getPlayerState(player.getName());

        //世界限定
        if (!player.getWorld().getName().equals(mainWorldName) && !player.getWorld().getName().equals(resWorldName)) {
            state.isInBeacon = false;
            return;
        }

        //特殊状态：飞行、潜行、滑翔、水中、乘交通工具
        if (player.isFlying() || player.isSneaking() || player.isGliding() || player.isInWater() || player.isInsideVehicle()) {
            state.isInBeacon = false;
            return;
        }

        World world = player.getWorld();
        Location location = player.getLocation();
        if (state.x != location.getBlockX() || state.z != location.getBlockZ()) {
            //通过对玩家位置信息的记录，做一定的优化
            state.x = location.getBlockX();
            state.z = location.getBlockZ();

            //TODO: 这一段有个前提条件是世界高度为 0~255，但是在1.17这个条件不成立了，届时注意修改
            int y = location.getBlockY() - 1;
            Block block = world.getBlockAt(state.x, y, state.z);

            //向下找到首个非空气方块
            while (y >= 0 && block.getType().equals(Material.AIR)) {
                y -= 1;
                block = world.getBlockAt(state.x, y, state.z);
            }

            //若下方全为空气，此逻辑也正常运行
            if (block.getType().equals(Material.BEACON)) {
                Beacon beacon = new CraftBeacon(block);
                state.isInBeacon = true;
                if (0 == beacon.getTier()) {
                    state.isInBeacon = false;
                } else {
                    y = location.getBlockY();
                    while (y <= 255) {
                        block = world.getBlockAt(state.x, y, state.z);
                        if (!block.getType().equals(Material.AIR)) {
                            state.isInBeacon = false;
                            break;
                        }
                        y += 1;
                    }
                }
            } else {
                state.isInBeacon = false;
            }

            //设置玩家状态中的信息
            if (state.isInBeacon) {
                state.beaconX = state.x;
                state.beaconZ = state.z;
            }
        }
    }

    @EventHandler
    public void listenerPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerState state = getPlayerState(player.getName());

        //TODO: 希望这样能够避免和玩家死亡有关的各种问题，但目前可靠性缺乏验证
        state.isInBeacon = false;
    }

    @EventHandler
    public void listenerPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerState state = loadPlayerState(player.getName());

        state.isInBeacon = false;
    }

    @EventHandler
    public void listenerPlayerQuit(PlayerQuitEvent event) {
        //TODO: 此处可能存在风险，下线和掉线的处理可能不同，需要测试当玩家存在丢包卡顿的情况
        unloadPlayerState(event.getPlayer().getName());
    }

    @EventHandler
    public void listenerPlayerRespawnWhenJoin(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        //这里由于暂时不清楚这个事件与Join事件的调用顺序，用这种方法处理了
        PlayerState state = loadPlayerState(player.getName());
        if (-1 != state.curWorldCode) {
            if (getWorldCode() != state.curWorldCode || !isWorldRun()) {
                Location loc = event.getSpawnLocation();
                World world = Bukkit.getWorld(mainWorldName);
                if (null == world) {
                    Bukkit.getLogger().warning("[ResourceWorld]Plz change \"mainworldname\" in the config to such one in server.properties");
                    return;
                }

                player.getInventory().clear();

                loc.setWorld(Bukkit.getWorld(mainWorldName));
                //TODO: 建立在世界高度为 0 ~ 255 的基础上
                loc.setY(-128.0);

                event.setSpawnLocation(loc);
            }
        }
    }

    @EventHandler
    public void listenerPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerState state = getPlayerState(player.getName());

        if (-1 != state.curWorldCode) {
            event.setRespawnLocation(state.spawnLocInMainWorld);
            onQuitResWorld(player);
        }
    }

    //============Join&Quit=============================================================================================

    @Override
    protected void playerJoin(Player player, Location backLoc) {
        PlayerState state = getPlayerState(player.getName());
        state.spawnLocInMainWorld = backLoc;
        state.curWorldCode = getWorldCode();

        player.teleport(getCurResWorld().getSpawnLocation(player));
    }

    @Override
    protected void playerQuit(Player player) {
        PlayerState state = getPlayerState(player.getName());
        onQuitResWorld(player);
        player.teleport(state.spawnLocInMainWorld);
    }

    @Override
    protected boolean isPlayerInResWorld(Player player) {
        return this.worldCode == getPlayerState(player.getName()).curWorldCode;
    }

    protected void onQuitResWorld(Player player) {
        getPlayerState(player.getName()).curWorldCode = -1;
    }

    private void playerJoinFromBeacon(Player player, int x, int z) {
        x -= 1;
        World world = player.getWorld();

        //TODO: 前提条件是世界高度为 0~255，但是在1.17这个条件不成立了，届时注意修改
        int y = 255;
        Block block = world.getBlockAt(x, y, z);

        while (y > 0 && block.isPassable()) {
            y -= 1;
            block = world.getBlockAt(x, y, z);
        }

        if (0 == y) {
            playerJoin(player, world.getSpawnLocation());
        } else {
            playerJoin(player, new Location(world, x, y + 1, z));
        }
    }

    private void playerQuitFromBeacon(Player player) {
        playerQuit(player);
    }

    private void playerQuitFromDestroy(Player player) {
        Location loc = player.getLocation();
        World world = Bukkit.getWorld(mainWorldName);
        if (null == world) {
            Bukkit.getLogger().warning("[ResourceWorld]Plz change \"mainworldname\" in the config to such one in server.properties");
            return;
        }

        player.getInventory().clear();

        loc.setWorld(world);
        loc.setY(-128.0);
        player.teleport(loc);
    }

    @Override
    protected void destroyWorld() {
        World world = getResWorld().getWorld();
        if (null == world) {
            Bukkit.getLogger().warning("[ResourceWorld]World instance miss when destroy.");
            return;
        }

        //TODO: 此处可能导致问题，玩家传送走可能需要一定的时间，所以考虑提前一些踢掉，这里就不需要处理东西了
        List<Player> playerList = world.getPlayers();
        for (Player player : playerList) {
            playerQuitFromDestroy(player);
        }

        super.destroyWorld();
    }

    //============PlayerState===========================================================================================

    protected static class PlayerState {
        int timer = -1;

        //==============flag================
        boolean isInBeacon = false;

        //==============data================
        int x = 0, z = 0;
        int beaconX = 0, beaconZ = 0;
        int curWorldCode = -1; //-1为主世界，其余从0开始
        Location spawnLocInMainWorld = null;

        public void reset() {
            this.isInBeacon = false;
            this.timer = -20 * 3;    //三秒冷却
            this.x = this.z = this.beaconX = this.beaconZ = 0;
        }

        public void save(String playerName) {
            ResourceWorld.getStorage().setPlayerData(playerName, "timer", this.timer);
            ResourceWorld.getStorage().setPlayerData(playerName, "beaconx", this.beaconX);
            ResourceWorld.getStorage().setPlayerData(playerName, "beaconz", this.beaconZ);
            ResourceWorld.getStorage().setPlayerData(playerName, "curworldcode", this.curWorldCode);
            ResourceWorld.getStorage().setPlayerData(playerName, "spawnlocinmainworld", this.spawnLocInMainWorld);
        }

        public void load(String playerName) {
            this.timer =  ResourceWorld.getStorage().getPlayerInt(playerName, "timer", -1);
            this.beaconX = ResourceWorld.getStorage().getPlayerInt(playerName, "beaconx", 0);
            this.beaconZ = ResourceWorld.getStorage().getPlayerInt(playerName, "beaconz", 0);
            this.curWorldCode = ResourceWorld.getStorage().getPlayerInt(playerName, "curworldcode", -1);
            this.spawnLocInMainWorld = ResourceWorld.getStorage().getPlayerLocation(playerName, "spawnlocinmainworld", null);
        }
    }

    HashMap<String, PlayerState> states;

    protected final PlayerState loadPlayerState(String name) {
        if (this.states.containsKey(name))
            return this.states.get(name);

        PlayerState state = new PlayerState();
        state.load(name);
        this.states.put(name, state);
        return state;
    }

    protected final void unloadPlayerState(String name) {
        assert states.containsKey(name);

        getPlayerState(name).save(name);
        this.states.remove(name);
    }

    protected final PlayerState getPlayerState(String name) {
        if (!this.states.containsKey(name)) {
            Bukkit.getLogger().warning("[ResourceWorld]player " + name + "'s state isn't loaded when using");
            loadPlayerState(name);
        }
        return this.states.get(name);
    }

    //============Effect================================================================================================

    /*
    private void adsorption(Player player, double x, double z) {
        Location location = player.getLocation();
        Vector vec = player.getVelocity();
        double offsetX = location.getX() - x;
        double offsetZ = location.getZ() - z;

        double adsorptionPow = 0.6;
        offsetX = -offsetX * adsorptionPow;
        offsetZ = -offsetZ * adsorptionPow;
        vec.setX(vec.getX() + offsetX);
        vec.setZ(vec.getZ() + offsetZ);

        player.setVelocity(vec);
    }
    */

    private void pushAway(Player player, double x, double z) {
        Location location = player.getLocation();
        Vector vec = player.getVelocity();
        double offsetX = location.getX() - x;
        double offsetZ = location.getZ() - z;
        if (0.0 != offsetX * offsetX + offsetZ * offsetZ) {
            double tmp = MathHelper.sqrt(offsetX * offsetX + offsetZ * offsetZ);
            double pushPow = 0.5;
            offsetX = offsetX * pushPow / tmp;
            offsetZ = offsetZ * pushPow / tmp;
            vec.setX(vec.getX() + offsetX);
            vec.setZ(vec.getZ() + offsetZ);
        }
        player.setVelocity(vec);
    }

    private void playTelWaitEffect(Location location) {
        World world = location.getWorld();
        assert null != world;

        world.spawnParticle(Particle.PORTAL, location, 6, 2.0, 0.0, 2.0);
    }

    private void playTelSucEffect(Location location) {
        World world = location.getWorld();
        assert null != world;

        world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1, 1);
        world.spawnParticle(Particle.PORTAL, location, 20, 6.0, 0.0, 6.0);
    }

    private void playTelCancelEffect(Location location) {
        World world = location.getWorld();
        assert null != world;

        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 10, 3.0, 0.0, 3.0);
    }

    private void playFloatEffect(Player player) {
        Location location = player.getLocation();
        if (location.getBlockY() >= 255) return;

        PotionEffect potionEffect = new PotionEffect(PotionEffectType.LEVITATION, 5, 2, false, false);
        player.addPotionEffect(potionEffect);
        player.setFallDistance(0.0F);
    }

    //============FSM===================================================================================================

    @Override
    public void onTick() {
        super.onTick();

        for (Map.Entry<String, PlayerState> it : this.states.entrySet()) {
            PlayerState state = it.getValue();
            String name = it.getKey();

            if (state.timer < -1) {
                state.timer += 1;
            } else if (-1 == state.timer) {
                if (state.isInBeacon) {
                    playerTelInit(name, state);
                    state.timer += 1;
                }
            } else {
                if (state.isInBeacon) {
                    if (this.telDelay == state.timer) {
                        //TODO: 这里是为了给玩家进入世界预留充足时间，考虑换成更形式化的方法
                        if (getResWorld().isJoinable())
                            playerTelSuc(name, state);
                        else
                            playerTelCancel(name, state);
                        state.reset();
                    } else {
                        playerWaitForTel(name, state);
                    }
                    state.timer += 1;
                } else {
                    playerTelCancel(name, state);
                    state.reset();
                }
            }
        }
    }

    protected void playerTelInit(String name, PlayerState state) {}

    protected void playerTelCancel(String name, PlayerState state) {
        Player player = Bukkit.getPlayer(name);
        assert null != player;

        pushAway(player, state.beaconX + 0.5, state.beaconZ + 0.5);
        playTelCancelEffect(player.getLocation());
    }

    protected void playerWaitForTel(String name, PlayerState state) {
        Player player = Bukkit.getPlayer(name);
        assert null != player;

        playFloatEffect(player);
        playTelWaitEffect(player.getLocation());
    }

    protected void playerTelSuc(String name, PlayerState state) {
        Player player = Bukkit.getPlayer(name);
        assert null != player;

        playTelSucEffect(player.getLocation());

        if (-1 != state.curWorldCode)
            playerQuitFromBeacon(player);
        else
            playerJoinFromBeacon(player, state.beaconX, state.beaconZ);

        playTelSucEffect(player.getLocation());
    }
}