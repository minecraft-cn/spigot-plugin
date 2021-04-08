package top.tsk.world.manager;

import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.world.world.DemoWorld;
import top.tsk.world.world.ResWorld;

public class SingleWorldManager extends ManagerItemWrapper {

    private ResWorld resWorld;

    public SingleWorldManager(JavaPlugin plugin) {
        super(plugin);
        this.resWorld = new DemoWorld(plugin);
    }

    @Override
    protected ResWorld getNextResWorldQue() {
        return resWorld;
    }

    @Override
    protected ResWorld getCurResWorld() {
        return resWorld;
    }
}