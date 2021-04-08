package top.tsk.world.world;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.*;

public class DemoWorld extends ResWorldBaseWrapper implements Listener {

    public DemoWorld(JavaPlugin plugin) {
        super(plugin);
    }

    //============Listener==============================================================================================

    @EventHandler
    public void listenerWorldInit(WorldInitEvent event) {
        if (event.getWorld().getName().equals(resWorldName)) {
            event.getWorld().getPopulators().add(new RemainsPopulator());
            event.getWorld().getPopulators().add(new DiamondPopulator());
            //event.getWorld().getPopulators().add(new ExitPopulator());
            //event.getWorld().getPopulators().add(new CanyonPopulator());
        }
    }

    private static class CanyonPopulator extends BlockPopulator {

        private static final int baseY = 20;
        private static final int h = 40;
        private static final int a = -12;
        private static final int b = -8;
        private static final int c = 8;
        private static final int d = 12;

        private int f(int x, int z) {
            if (x <= a)
                return 0;
            else if (x <= b)
                return  (x - a) * h / (b - a);
            else if (x <= c)
                return h;
            else if (x <= d)
                return (d - x) * h / (d - c);
            else
                return 0;
        }

        @Override
        public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk chunk) {
            for (int x = 0; x < 16; ++x)
                for (int z = 0; z < 16; ++z) {
                    //TODO: 注意1.17世界高度改变
                    int offset = f(chunk.getX() << 4 | x, chunk.getZ() << 4 | z);
                    if (0 == offset) continue;

                    for (int y = baseY; y < 256; ++y) {
                        Block block = chunk.getBlock(x, y, z);

                        while (y + offset < 256 && chunk.getBlock(x, y + offset, z).getType().equals(Material.AIR))
                            offset += 1;

                        if (y + offset >= 256) {
                            block.setType(Material.AIR, false);
                        } else {
                            block.setType(chunk.getBlock(x, y + offset, z).getType(), false);
                        }
                    }
                }
        }
    }

    private static class RemainsPopulator extends BlockPopulator {

        private static final int amount = 32;   //位置寻找次数
        private static final int maxAmount = 2; //最大生成数量
        private static final int maxHeight = 128; //最高生成高度

        @Override
        public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk chunk) {

            int curAmount = 0;  //当前生成数量

            for (int i = 0; i < amount; ++i) {
                //不会在区块边界上生成
                int x = 1 + random.nextInt(14);
                int z = 1 + random.nextInt(14);

                for (int y = maxHeight; y >= 4; --y) {
                    if (Material.LAVA == chunk.getBlock(x, y + 1, z).getType()) {

                        if (7 == random.nextInt(30)) {
                            chunk.getBlock(x, y, z).setType(Material.ANCIENT_DEBRIS, false);
                            if (++curAmount == maxAmount) {
                                return;
                            }
                            break;
                        }
                    }
                }

            }

        }
    }

    private static class DiamondPopulator extends BlockPopulator {

        private static final int amount = 64;   //位置寻找次数
        private static final int maxAmount = 16; //最大生成数量
        private static final int maxHeight = 128; //最高生成高度

        private boolean isLegal(Material material) {
            if (Material.GOLD_ORE == material) return true;
            if (Material.IRON_ORE == material) return true;
            if (Material.REDSTONE_ORE == material) return true;
            if (Material.COAL_ORE == material) return true;
            return Material.LAPIS_ORE == material;
        }

        @Override
        public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk chunk) {

            int curAmount = 0;  //当前生成数量

            for (int i = 0; i < amount; ++i) {
                //不会在区块边界上生成
                int x = 1 + random.nextInt(14);
                int z = 1 + random.nextInt(14);

                for (int y = maxHeight; y >= 4; --y) {
                    if (isLegal(chunk.getBlock(x, y, z).getType())) {
                        if (7 == random.nextInt(30)) {
                            chunk.getBlock(x, y, z).setType(Material.DIAMOND_ORE, false);
                            if (++curAmount == maxAmount) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ExitPopulator extends BlockPopulator {

        private static final int[] locX = {7, 8, 9, 7, 8, 9, 7, 8, 9};
        private static final int[] locZ = {7, 7, 7, 8, 8, 8, 9, 9, 9};

        @Override
        public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk chunk) {

            if (7 != random.nextInt(1500)) return;

            for (int y = 254; y >= 0; --y) {
                boolean flag = false;
                for (int i = 0; i < 9; ++i) {
                    if (Material.AIR != chunk.getBlock(locX[i], y, locZ[i]).getType()) {
                        flag = true;
                        break;
                    }
                }

                if (flag) {
                    for (int i = 0; i < 9; ++i) {
                        chunk.getBlock(locX[i], y, locZ[i]).setType(Material.IRON_BLOCK, false);
                    }
                    chunk.getBlock(locX[4], y + 1, locZ[4]).setType(Material.BEACON, false);
                    break;
                }
            }
        }
    }

    //============Override==============================================================================================

    @Override
    public String info() {
        return "DemoWorld";
    }

    private static final int[] locX = {7, 8, 9, 7, 8, 9, 7, 8, 9};
    private static final int[] locZ = {7, 7, 7, 8, 8, 8, 9, 9, 9};

    @Override
    public void built(World world) {
        super.built(world);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setTime(15000);

        for (int y = 254; y >= 0; --y) {
            boolean flag = false;
            for (int i = 0; i < 9; ++i) {
                if (Material.AIR != world.getBlockAt(locX[i], y, locZ[i]).getType()) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                for (int i = 0; i < 9; ++i) {
                    world.getBlockAt(locX[i], y, locZ[i]).setType(Material.IRON_BLOCK, false);
                }
                world.getBlockAt(locX[4], y + 1, locZ[4]).setType(Material.BEACON, false);
                break;
            }
        }
    }

    @Override
    public Location getSpawnLocation(Player player) {
        int y = 255;
        while(y > 0 && getWorld().getBlockAt(1145, y - 1, 1419).getType().equals(Material.AIR)) {
            y -= 1;
        }
        return new Location(getWorld(), 1145, y, 1419);
    }
}