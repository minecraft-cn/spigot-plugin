package top.tsk;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class TskPluginManager extends JavaPlugin implements Listener {


  CommandDispatcher<Object> commandDispatcher = new CommandDispatcher<>();


  @Override
  public void onLoad() {
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveConfig();
    }
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);


    commandDispatcher.register(
      LiteralArgumentBuilder.literal("tskpluginmanager")
        .then(
          LiteralArgumentBuilder.literal("1")
            .then(
              RequiredArgumentBuilder.argument("PluginName", StringArgumentType.word())
                .executes((commandSender) -> {
                  String name = StringArgumentType.getString(commandSender, "PluginName");
                  Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
                  if (!plugin.isEnabled()) {
                    Bukkit.getPluginManager().enablePlugin(plugin);
                  }
                  return 1;
                })
            )
        )
        .then(
          LiteralArgumentBuilder.literal("0")
            .then(
              RequiredArgumentBuilder.argument("PluginName", StringArgumentType.word())
                .executes((commandSender) -> {
                  String name = StringArgumentType.getString(commandSender, "PluginName");
                  Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
                  if (plugin.isEnabled()) {
                    Bukkit.getPluginManager().disablePlugin(plugin);
                  }
                  return 1;
                })
            )
        ));
  }

  @Override
  public void onDisable() {
    saveConfig();
  }


  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (command.getName().equals("tskpluginmanager")) {

      if (args.length == 1) {
        return TskUtils.FilterByPrefix(Arrays.asList("1", "0"), args[args.length - 1]);
      }

      if (args.length == 2) {
        LinkedList<String> linkedList = new LinkedList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
          linkedList.add(plugin.getName());
        }
        return TskUtils.FilterByPrefix(linkedList, args[args.length - 1]);
      }
    }

    return null;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    int result = 0;

    String cmd = command.getName().concat(" ").concat(TskUtils.CombineStrings(args));

    try {
      result = commandDispatcher.execute(cmd, sender);
    } catch (CommandSyntaxException e) {
      e.printStackTrace();
    }

    if (result == 0) {
      command.setUsage(ChatColor.RED + "/tpm 0 xxx");
      return false;
    }

    sender.sendMessage(ChatColor.GREEN + "OK");
    return true;
  }
}

