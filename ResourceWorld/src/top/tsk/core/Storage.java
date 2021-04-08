package top.tsk.core;

import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Storage {

    private File playerFolder;
    private File dataFile;
    private Map<String, FileConfiguration> mp;

    private FileConfiguration dataConfig = null;

    public Storage(@NotNull File dataFolder) {
        playerFolder = new File(dataFolder, "player");
        dataFile = new File(dataFolder, "data.yml");
        mp = new HashMap<>();
    }

    //==================================================================================================================

    private void loadDataConfig() {
        this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
    }

    private void loadPlayerConfig(String playerName) {
        File playerFile = new File(playerFolder, playerName + ".yml");
        this.mp.put(playerName, YamlConfiguration.loadConfiguration(playerFile));
    }

    private FileConfiguration getPlayerConfig(String playerName) {
        if (!this.mp.containsKey(playerName)) {
            loadPlayerConfig(playerName);
        }
        return this.mp.get(playerName);
    }

    private FileConfiguration getDataConfig() {
        if (null == this.dataConfig) {
            loadDataConfig();
        }

        return this.dataConfig;
    }

    //==================================================================================================================

    public void save() {
        try {
            dataConfig.save(this.dataFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not save config to " + this.dataFile, e);
        }

        for (Map.Entry<String, FileConfiguration> it : mp.entrySet()) {
            File playerFile = new File(playerFolder, it.getKey() + ".yml");
            try {
                it.getValue().save(playerFile);
            } catch (IOException e) {
                Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not save config to " + playerFolder, e);
            }
        }
    }

    public void setData(String path, Object value) {
        getDataConfig().createSection(path);
        getDataConfig().set(path, value);
    }

    public int getInt(String path, int def) {
        return getDataConfig().getInt(path, def);
    }

    public String getString(String path, String def) {
        return getDataConfig().getString(path, def);
    }

    public Location getLocation(String path, Location def) {
        return getDataConfig().getLocation(path, def);
    }

    public ItemStack getItemStack(String path, ItemStack def) {
        return getDataConfig().getItemStack(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return getDataConfig().getBoolean(path, def);
    }

    public void setPlayerData(String playerName, String path, Object value) {
        FileConfiguration config = getPlayerConfig(playerName);
        config.createSection(path);
        config.set(path, value);
    }

    public int getPlayerInt(String playerName, String path, int def) {
        return getPlayerConfig(playerName).getInt(path, def);
    }

    public String getPlayerString(String playerName, String path, String def) {
        return getPlayerConfig(playerName).getString(path, def);
    }

    public Location getPlayerLocation(String playerName, String path, Location def) {
        return getPlayerConfig(playerName).getLocation(path, def);
    }

    public ItemStack getPlayerItemStack(String playerName, String path, ItemStack def) {
        return getPlayerConfig(playerName).getItemStack(path, def);
    }

    public boolean getPlayerBoolean(String playerName, String path, boolean def) {
        return getPlayerConfig(playerName).getBoolean(path, def);
    }
}
