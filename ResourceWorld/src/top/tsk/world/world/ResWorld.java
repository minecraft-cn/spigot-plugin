package top.tsk.world.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public interface ResWorld {

    String info();

    Location getSpawnLocation(Player player);

    World getWorld();

    boolean isJoinable();

    WorldCreator getWorldCreator();

    void built(World world);

    void deploy(boolean isBuild);

    void undeploy();

    boolean onTick();

    void save();

}