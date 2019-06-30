package top.tsk;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TskWorldManager extends JavaPlugin implements Listener {
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
      LiteralArgumentBuilder.literal("tskworldmanager")
        .then(
          LiteralArgumentBuilder.literal("+")
            .then(
              RequiredArgumentBuilder.argument("WorldName", StringArgumentType.word())
                .then(
                  RequiredArgumentBuilder.argument("WorldType", StringArgumentType.word())
                    .executes((commandSender) -> {
                      String worldName = StringArgumentType.getString(commandSender, "WorldName");
                      String worldType = StringArgumentType.getString(commandSender, "WorldType");
                      WorldCreator worldCreator = new WorldCreator(worldName);
                      worldCreator.type(WorldType.getByName(worldType));
                      Bukkit.createWorld(worldCreator);
                      return 1;
                    })
                )
            )
        )
        .then(
          LiteralArgumentBuilder.literal("-")
            .then(
              RequiredArgumentBuilder.argument("WorldName", StringArgumentType.word())
                .executes((commandSender) -> {
                  String worldName = StringArgumentType.getString(commandSender, "WorldName");
                  Bukkit.unloadWorld(worldName, true);
                  return 1;
                })
            )
        )
        .then(
          LiteralArgumentBuilder.literal(":")
            .executes((commandContext) -> {
              String msg = "";
              for (World world : Bukkit.getWorlds()) {
                msg += String.format("%s: [p:%d v:%b c:%d]\n", world.getName(), world.getPlayers().size(), world.getPVP(), world.getLoadedChunks().length);
              }
              ((CommandSender) commandContext.getSource()).sendMessage(msg);
              return 1;
            })
        )
        .then(
          LiteralArgumentBuilder.literal(">")
            .then(
              RequiredArgumentBuilder.argument("WorldName", StringArgumentType.word())
                .then(RequiredArgumentBuilder.argument("PlayerName", StringArgumentType.word())
                  .executes((commandContext) -> {
                    String worldName = StringArgumentType.getString(commandContext, "WorldName");
                    String playerName = StringArgumentType.getString(commandContext, "PlayerName");
                    Bukkit.getPlayer(playerName).teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                    return 1;
                  })
                )
            )
        )
    );
  }

  @Override
  public void onDisable() {
    saveConfig();
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (command.getName().equals("tskworldmanager")) {
      if (args.length == 1) {
        return TskUtils.FilterByPrefix(Arrays.asList("+", "-", ":", ">"), args[args.length - 1]);
      }
      if (args.length == 2) {
        LinkedList<String> linkedList = new LinkedList<>();
        for (World world : Bukkit.getWorlds()) {
          linkedList.add(world.getName());
        }
        return TskUtils.FilterByPrefix(linkedList, args[args.length - 1]);
      }
      if (args.length == 3 && args[0].equals("+")) {
        LinkedList<String> linkedList = new LinkedList<>();
        for (WorldType worldType : WorldType.values()) {
          linkedList.add(worldType.getName());
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
      command.setUsage(ChatColor.RED + "/twm + xxx");
      return false;
    }
    sender.sendMessage(ChatColor.GREEN + "OK");
    return true;
  }
}