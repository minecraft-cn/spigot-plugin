package top.tsk.core;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import top.tsk.ResourceWorld;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Test implements TabExecutor {
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (1 == args.length) {
            String str = args[0];
            switch (str) {
                case "info":
                    sender.sendMessage(ResourceWorld.getWorldManager().getWorldInfo());
                    break;
                case "rebuild":
                    if (ResourceWorld.getWorldManager().rebuildWorldFromCommand()) {
                        sender.sendMessage("Successfully rebuild");
                    } else {
                        sender.sendMessage("Failed to rebuild");
                    }
                    break;
                case "destroy":
                    if (ResourceWorld.getWorldManager().destroyWorldFromCommand()) {
                        sender.sendMessage("Successfully destroy");
                    } else {
                        sender.sendMessage("Failed to rebuild");
                    }
                    break;
                default:
                    return false;
            }
        }

        if (2 == args.length) {
            String playerName = args[1];
            Player player = Bukkit.getPlayer(playerName);
            if (null == player) return false;

            switch (args[0]) {
                case "join":
                    if (!ResourceWorld.getWorldManager().playerJoinFromCommand(player)) {
                        sender.sendMessage("Failed to join");
                    }
                    break;
                case "leave":
                    if (!ResourceWorld.getWorldManager().playerQuitFromCommand(player)) {
                        sender.sendMessage("Failed to leave");
                    }
                    break;
                case "check":
                    sender.sendMessage(playerName + " is in world" + ResourceWorld.getWorldManager());
                    break;
                default:
                    return false;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (1 == args.length)
            return new ArrayList<String>() {{
                add("info");
                add("rebuild");
                add("destroy");
                add("join");
                add("leave");
                add("check");
            }};
        else
            return null;
    }
}
