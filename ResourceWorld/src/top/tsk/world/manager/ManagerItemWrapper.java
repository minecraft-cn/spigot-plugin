package top.tsk.world.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.world.services.ItemsConfiscator;

abstract public class ManagerItemWrapper extends ManagerTelWrapper {

    ItemsConfiscator itemsConfiscator;

    public ManagerItemWrapper(JavaPlugin plugin) {
        super(plugin);

        itemsConfiscator = new ItemsConfiscator();
    }

    @Override
    protected void onQuitResWorld(Player player) {
        PlayerState state = getPlayerState(player.getName());
        itemsConfiscator.itemsGiveBack(player, state.spawnLocInMainWorld);
        super.onQuitResWorld(player);
    }

    @Override
    protected void playerTelInit(String name, PlayerState state) {
        super.playerTelInit(name, state);

        //保存玩家背包,进行首次存储以防止服务器关闭所致的物品丢失
        Player player = Bukkit.getPlayer(name);
        assert null != player;
        if (-1 == state.curWorldCode)
            itemsConfiscator.takeBanItems(player);
    }

    @Override
    protected void playerTelCancel(String name, PlayerState state) {
        super.playerTelCancel(name, state);

        Player player = Bukkit.getPlayer(name);
        assert null != player;
        if (-1 == state.curWorldCode)
            itemsConfiscator.itemsGiveBack(player, player.getLocation());
    }

    @Override
    protected void playerWaitForTel(String name, PlayerState state) {
        super.playerWaitForTel(name, state);

        Player player = Bukkit.getPlayer(name);
        assert null != player;

        //此处-1是因为清理物品需要确保从 0 到 1.0
        if (-1 == state.curWorldCode) {
            itemsConfiscator.takeBanItems(player);
            itemsConfiscator.takeOtherItems(player, (double) state.timer / (telDelay - 1));
        }
    }

    @Override
    protected void playerTelSuc(String name, PlayerState state) {
        Player player = Bukkit.getPlayer(name);
        assert null != player;

        if (-1 == state.curWorldCode) {
            itemsConfiscator.takeBanItems(player);
            itemsConfiscator.takeOtherItems(player, 1.0);
        }

        super.playerTelSuc(name, state);
    }

    @Override
    public void save() {
        super.save();
        itemsConfiscator.save();
    }
}