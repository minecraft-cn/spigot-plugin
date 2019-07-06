package top.tsk.utils;

import org.bukkit.Material;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class TskUtils {
  public static String CombineStrings(String[] args) {
    if (args.length == 0) {
      return "";
    }
    StringBuilder s = new StringBuilder();
    for (String arg : args) {
      s.append(" ").append(arg);
    }
    return s.toString().substring(1);
  }

  public static List<String> FilterByPrefix(List<String> list, String prefix) {
    LinkedList<String> linkedList = new LinkedList<>();
    for (String s : list) {
      if (s.startsWith(prefix)) {
        linkedList.add(s);
      }
    }
    return linkedList;
  }

  public static boolean IsItemEmpty(ItemStack itemStack) {
    if (itemStack == null) {
      return true;
    }
    if (itemStack.getType().equals(Material.AIR)) {
      return true;
    }
    return false;
  }

  public static double GetMaxHealth(Attributable attributable) {
    return attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
  }

  public static void SaveObject(JavaPlugin javaPlugin) {
    Field[] fields = javaPlugin.getClass().getFields();
    for (Field field : fields) {
      field.setAccessible(true);
      try {
        javaPlugin.getConfig().set(field.getName(), field.get(javaPlugin));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    javaPlugin.saveConfig();
  }

  public static void LoadObject(JavaPlugin javaPlugin) {
    Field[] fields = javaPlugin.getClass().getFields();
    for (Field field : fields) {
      field.setAccessible(true);
      try {
        field.set(javaPlugin, javaPlugin.getConfig().get(field.getName(), field.get(javaPlugin)));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }
}
