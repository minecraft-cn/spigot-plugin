package top.tsk.world.manager;

import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.ResourceWorld;

abstract public class ManagerBaseWrapper extends WorldManager {

    public ManagerBaseWrapper(JavaPlugin plugin) {
        super(plugin);

        this.timer = ResourceWorld.getStorage().getInt("systemtimer", 0);
    }

    //============Tick==================================================================================================

    protected int timer;

    @Override
    protected void onTick() {
        if (isWorldRun()) {
            if (!getResWorld().onTick()) destroyWorld();
        } else {
            this.timer += 1;
            if (resRebuildDuration == this.timer) {
                this.timer = 0;
                buildWorld();
            }
        }
    }

    //============Override==============================================================================================

    @Override
    public boolean rebuildWorldFromCommand() {
        this.timer = 0;
        return super.rebuildWorldFromCommand();
    }

    @Override
    public boolean destroyWorldFromCommand() {
        if (super.destroyWorldFromCommand()) {
            this.timer = 0;
            return true;
        }
        return false;
    }

    @Override
    public void save() {
        ResourceWorld.getStorage().setData("systemtimer", this.timer);

        super.save();
    }
}