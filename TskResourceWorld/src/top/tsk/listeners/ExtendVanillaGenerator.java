/**
 * projectName: TskResourceWorld
 * fileName: ExtendVanillaGenerator.java
 * packageName: top.tsk.listeners
 * buildDate: 2020-07-21 18:55
 */
package top.tsk.listeners;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import top.tsk.TskResourceWorld;

import java.util.Random;

/**
 * listeners.ExtendVanillaGenerator
 * 修改世界生成器
 **/
public class ExtendVanillaGenerator implements Listener {

    @EventHandler
    public void onInit(WorldInitEvent event) {
        if (TskResourceWorld.resWorldName.equals(event.getWorld().getName())) {
            event.getWorld().getPopulators().add(new RemainsPopulator());
            event.getWorld().getPopulators().add(new DiamondPopulator());
            event.getWorld().getPopulators().add(new ExitPopulator());
        }
    }

    private static class RemainsPopulator extends BlockPopulator {

        private static final int amount = 32;   //位置寻找次数
        private static final int maxAmount = 2; //最大生成数量
        private static final int maxHeight = 128; //最高生成高度

        @Override
        public void populate(World world, Random random, Chunk chunk) {

            int curAmount = 0;  //当前生成数量

            for (int i = 0; i < amount; ++i) {
                //不会在区块边界上生成
                int x = 1 + random.nextInt(14);
                int z = 1 + random.nextInt(14);

                for (int y = maxHeight; y >= 4; --y) {
                    if (Material.LAVA == chunk.getBlock(x, y + 1, z).getType()) {
                        if (7 == random.nextInt(30)) {
                            chunk.getBlock(x, y, z).setType(Material.ANCIENT_DEBRIS);
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
        public void populate(World world, Random random, Chunk chunk) {

            int curAmount = 0;  //当前生成数量

            for (int i = 0; i < amount; ++i) {
                //不会在区块边界上生成
                int x = 1 + random.nextInt(14);
                int z = 1 + random.nextInt(14);

                for (int y = maxHeight; y >= 4; --y) {
                    if (isLegal(chunk.getBlock(x, y, z).getType())) {
                        if (7 == random.nextInt(30)) {
                            chunk.getBlock(x, y, z).setType(Material.DIAMOND_ORE);
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
        public void populate(World world, Random random, Chunk chunk) {

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
                        chunk.getBlock(locX[i], y, locZ[i]).setType(Material.IRON_BLOCK);
                    }
                    chunk.getBlock(locX[4], y + 1, locZ[4]).setType(Material.BEACON);
                    break;
                }
            }

        }
    }
}