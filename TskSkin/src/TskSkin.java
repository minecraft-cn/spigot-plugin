import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import kong.unirest.Unirest;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import top.tsk.utils.TskUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class TskSkin extends JavaPlugin implements Listener {

  private Plugin thisTskSkin = this;
  private Map<String, Property> PlayerProperty = new ConcurrentHashMap<>();

  static final String textures = "textures";
  static final String MojangAPIUrlUUID = "https://api.mojang.com/users/profiles/minecraft";
  static final String MojangAPIUrlProfile = "https://sessionserver.mojang.com/session/minecraft/profile";

  @Override
  public void onLoad() {
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveConfig();
    }
  }

  @Override
  public void onEnable() {
    TskUtils.LoadObject(this);
    getServer().getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDisable() {
    TskUtils.SaveObject(this);
  }

  Property getPropertyByName(String playerName) {
    String url = String.format("%s/%s", MojangAPIUrlUUID, playerName);
    JSONObject jsonObject = Unirest.get(url).asJson().getBody().getObject();
    String id = null;
    try {
      id = jsonObject.getString("id");
    } catch (JSONException e) {
      Bukkit.getLogger().log(Level.FINE, String.format("no mojang uuid for name: %s", playerName));
      return null;
    }
    String url2 = String.format("%s/%s", MojangAPIUrlProfile, id);
    JSONObject profile = Unirest.get(url2).queryString("unsigned", "false").asJson().getBody().getObject();
    JSONObject property0 = profile.getJSONArray("properties").getJSONObject(0);
    Property property = new Property(property0.getString("name"), property0.getString("value"), property0.getString("signature"));

    return property;
  }

  void setSkin(Player player, String skinName) {
    EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
    GameProfile gameProfile = entityPlayer.getProfile();
    PropertyMap properties = gameProfile.getProperties();
    properties.put(textures, PlayerProperty.get(skinName));
  }

  @EventHandler
  public void SetSkin(PlayerJoinEvent event) {
    final String playerName = event.getPlayer().getName();
    if (!PlayerProperty.containsKey(playerName)) {
      new BukkitRunnable() {
        @Override
        public void run() {
          Property property = getPropertyByName(playerName);
          if (property == null) {
            return;
          }
          PlayerProperty.put(playerName, property);
//          Player player = Bukkit.getPlayer(playerName);
//          if (player != null) {
//            setSkin(player, playerName);
//          }
        }
      }.runTaskAsynchronously(this);
    } else {
      setSkin(event.getPlayer(), playerName);
    }
  }
}
