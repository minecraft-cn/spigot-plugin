package top.tsk.utils;

import com.sun.istack.internal.NotNull;
import org.bukkit.Material;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

  public static Entity GetKiller(Entity entity) {
    if (entity instanceof LivingEntity &&
      ((LivingEntity) entity).getKiller() != null) {
      return ((LivingEntity) entity).getKiller();
    }
    if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
      Entity damager = ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();
      if (damager instanceof Projectile) {
        if (((Projectile) damager).getShooter() instanceof Entity) {
          return ((Entity) ((Projectile) damager).getShooter());
        }
      } else {
        return damager;
      }
    }
    return null;
  }

  public static Map<Enchantment, Integer> GetEnchants(@NotNull ItemStack itemStack) {
    Map<Enchantment, Integer> enchants = new HashMap<>();
    if (itemStack.hasItemMeta()) {
      if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
        enchants.putAll(((EnchantmentStorageMeta) itemStack.getItemMeta()).getStoredEnchants());
      } else {
        enchants.putAll(itemStack.getEnchantments());
      }
    }
    return enchants;
  }
}