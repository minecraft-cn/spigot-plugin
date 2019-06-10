package top.tsk;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class TskWhitelist extends JavaPlugin implements Listener {

  static final String CONFIG_REFUSE = "refuse-message";

  static final String CONFIG_WHITE = "white-list";
  static final String CONFIG_BLACK = "black-list";
  static final String CONFIG_GRAY = "gray-list";

  static final String CONFIG_ENABLE = "enabled";


  String refuseMessage;

  List whiteList;
  List blackList;
  List grayList;

  Boolean enable;

  CommandDispatcher<Object> commandDispatcher = new CommandDispatcher<>();


  public void initConfig() {
    getConfig().set(CONFIG_REFUSE, "You are not in whitelist");

    getConfig().set(CONFIG_WHITE, new LinkedList<String>());
    getConfig().set(CONFIG_BLACK, new LinkedList<String>());
    getConfig().set(CONFIG_GRAY, new LinkedList<String>());

    getConfig().set(CONFIG_ENABLE, false);
  }

  @Override
  public void onLoad() {
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveDefaultConfig();
      initConfig();
    }
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);


    refuseMessage = getConfig().getString(CONFIG_REFUSE);

    whiteList = getConfig().getStringList(CONFIG_WHITE);
    blackList = getConfig().getStringList(CONFIG_BLACK);
    grayList = getConfig().getStringList(CONFIG_GRAY);

    enable = getConfig().getBoolean(CONFIG_ENABLE);


    commandDispatcher.register(
      LiteralArgumentBuilder.literal("tskwhitelist")
        .then(
          LiteralArgumentBuilder.literal("+")
            .then(
              RequiredArgumentBuilder.argument("PlayerName", StringArgumentType.word())
                .executes(player -> {
                  String name = StringArgumentType.getString(player, "PlayerName");
                  if (!whiteList.contains(name)) {
                    whiteList.add(name);
                    return 1;
                  } else {
                    return 0;
                  }

                })
            )
        )
        .then(
          LiteralArgumentBuilder.literal("-")
            .then(
              RequiredArgumentBuilder.argument("PlayerName", StringArgumentType.word())
                .executes(player -> {
                  String name = StringArgumentType.getString(player, "PlayerName");
                  if (whiteList.contains(name)) {
                    whiteList.remove(name);
                    return 1;
                  } else {
                    return 0;
                  }
                })
            )
        )
        .then(
          LiteralArgumentBuilder.literal(":")
            .executes((commandSender) -> {
              System.out.println(whiteList.toString());
              return 1;
            })
        )
        .then(
          LiteralArgumentBuilder.literal("1")
            .executes((commandSender) -> {
              enable = true;
              return 1;
            })
        )
        .then(
          LiteralArgumentBuilder.literal("0")
            .executes((commandSender) -> {
              enable = false;
              return 1;
            })
        )
    );
  }

  @Override
  public void onDisable() {

    getConfig().set(CONFIG_REFUSE, refuseMessage);

    getConfig().set(CONFIG_WHITE, whiteList);
    getConfig().set(CONFIG_BLACK, blackList);
    getConfig().set(CONFIG_GRAY, grayList);

    getConfig().set(CONFIG_ENABLE, enable);


    saveConfig();
  }


  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    int result;

    String cmd = command.getName().concat(" ").concat(TskUtils.CombineStrings(args));

//    System.out.println(cmd);
    try {
      result = commandDispatcher.execute(cmd, sender);
    } catch (CommandSyntaxException e) {
      e.printStackTrace();
      return false;
    }

    if (result == 0) {
      return false;
    }

    return true;
  }

  @EventHandler
  public void preLogin(AsyncPlayerPreLoginEvent event) {

    if (enable) {
      if (whiteList.contains(event.getName())) {
        event.allow();
      } else {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, refuseMessage);

        if (!blackList.contains(event.getName())) {
          if (!grayList.contains(event.getName())) {
            grayList.add(event.getName());
          }
        }
      }
    } else {
      if (blackList.contains(event.getName())) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, refuseMessage);
      } else {
        event.allow();
      }
    }
  }
}

