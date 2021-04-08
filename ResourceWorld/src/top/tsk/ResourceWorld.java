package top.tsk;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.core.Storage;
import top.tsk.core.Test;
import top.tsk.world.manager.SingleWorldManager;
import top.tsk.world.manager.WorldManager;

public class ResourceWorld extends JavaPlugin {

    private static ResourceWorld instance = null;

    public static Storage getStorage() {
        return instance.storage;
    }

    public static WorldManager getWorldManager() {
        return instance.worldManager;
    }

    //========================================================

    private Storage storage = null;
    private WorldManager worldManager = null;

    @Override
    public void onEnable() {
        ResourceWorld.instance = this;

        this.storage = new Storage(getDataFolder());

        //TODO: 测试模块，release版本中应移除
        Test testClass = new Test();
        PluginCommand cmd = getCommand("test");
        assert null != cmd;
        cmd.setTabCompleter(testClass);
        cmd.setExecutor(testClass);

        this.worldManager = new SingleWorldManager(this);
        worldManager.deploy(this);

        getLogger().info("Plugin ResourceWorld has been deployed");
    }

    @Override
    public void onDisable() {
        this.worldManager.save();
        this.storage.save();

        this.worldManager = null;
        this.storage = null;

        ResourceWorld.instance = null;
        getLogger().info("Plugin ResourceWorld has been removed");
    }
}
