package top.tsk.world.services;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.tsk.ResourceWorld;

import java.util.*;

public class ItemsConfiscator {
    private final Set<Material> banItemList = new HashSet<Material>() {{
        add(Material.ELYTRA);
        add(Material.BEACON);
        add(Material.SHULKER_SHELL);
        add(Material.SHULKER_BOX);
        add(Material.WHITE_SHULKER_BOX);
        add(Material.ORANGE_SHULKER_BOX);
        add(Material.MAGENTA_SHULKER_BOX);
        add(Material.LIGHT_BLUE_SHULKER_BOX);
        add(Material.YELLOW_SHULKER_BOX);
        add(Material.LIME_SHULKER_BOX);
        add(Material.PINK_SHULKER_BOX);
        add(Material.GRAY_SHULKER_BOX);
        add(Material.LIGHT_GRAY_SHULKER_BOX);
        add(Material.CYAN_SHULKER_BOX);
        add(Material.PURPLE_SHULKER_BOX);
        add(Material.BLUE_SHULKER_BOX);
        add(Material.BROWN_SHULKER_BOX);
        add(Material.GREEN_SHULKER_BOX);
        add(Material.RED_SHULKER_BOX);
        add(Material.BLACK_SHULKER_BOX);
    }};

    private final int[] takeItemOrder = new int[]{
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            26, 25, 24, 23, 22, 21, 20, 19, 18,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            40, 8, 7, 6, 5, 4, 3, 2, 1, 0
    };

    private final Map<String, List<ItemStack>> playerTmpInventories;

    public ItemsConfiscator() {
        playerTmpInventories = new HashMap<>();
    }

    private List<ItemStack> getPlayerTmpInventory(String playerName) {
        if (!this.playerTmpInventories.containsKey(playerName)) {
            //极限情况下，这玩意也才能整出n2件物品，效率问题可以忽略
            List<ItemStack> lst = new LinkedList<>();
            int sz = ResourceWorld.getStorage().getPlayerInt(playerName, "inventorysize", 0);
            for (int i = 0; i < sz; ++i) {
                ItemStack itemStack = ResourceWorld.getStorage().getPlayerItemStack(playerName, "inventory" + i, null);
                if (null == itemStack) continue;
                lst.add(itemStack);
            }
            this.playerTmpInventories.put(playerName, lst);
            return lst;
        } else
            return this.playerTmpInventories.get(playerName);
    }

    public void takeBanItems(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i <= 40; ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (null == itemStack) continue;
            if (itemStack.getType().equals(Material.AIR)) continue;
            if (this.banItemList.contains(itemStack.getType())) {
                takeItem(player, itemStack, i);
            }
        }
    }

    public void takeOtherItems(Player player, double p) {
        p += 0.1;
        if (p > 1.0) p = 1.0;

        Inventory inventory = player.getInventory();
        //先找到最后一个物品位置
        int keepI = -1;
        for (int i = 0; i <= 8; ++i) {
            ItemStack item = inventory.getItem(i);
            if (null == item) continue;
            if (item.getType().equals(Material.AIR)) continue;
            keepI = i;
            break;
        }

        //按比例清空
        for (int i = 0; i <= 36 * p; ++i) {
            ItemStack item = inventory.getItem(this.takeItemOrder[i]);
            if (keepI == this.takeItemOrder[i]) continue;
            if (null == item) continue;
            if (item.getType().equals(Material.AIR)) continue;
            takeItem(player, item, this.takeItemOrder[i]);
        }
    }

    //"没收"物品
    private void takeItem(Player player, ItemStack itemStack, int i) {
        Item item = player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        item.setPickupDelay(32767);
        //TODO: 物品消失时间是可以在服务器设置中调的，这里需要解决好
        item.setTicksLived(5 * 60 * 20 - 15);

        getPlayerTmpInventory(player.getName()).add(itemStack);
        player.getInventory().clear(i);
    }

    public void itemsGiveBack(Player player, Location dropLoc) {
        List<ItemStack> lst = getPlayerTmpInventory(player.getName());
        World world = dropLoc.getWorld();
        assert world != null;

        //dropLoc的元素world是不会使用的
        for (ItemStack itemStack : lst) {
            Item item = world.dropItem(dropLoc, itemStack);
            item.setPickupDelay(0);
        }
        lst.clear();
    }

    public void save() {
        for (Map.Entry<String, List<ItemStack>> it : this.playerTmpInventories.entrySet()) {
            List<ItemStack> lst = it.getValue();
            int sz = lst.size();
            ResourceWorld.getStorage().setPlayerData(it.getKey(), "inventorysize", sz);
            int i = 0;
            for (ItemStack itemStack : lst) {
                ResourceWorld.getStorage().setPlayerData(it.getKey(), "inventory" + i, itemStack);
                i += 1;
            }
        }
    }
}