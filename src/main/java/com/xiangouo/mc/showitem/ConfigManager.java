package com.xiangouo.mc.showitem;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {
    private static FileConfiguration config, item, blacklist;
    private static File configFile, itemFile, blacklistFile;

    public static FileConfiguration getConfig() {
        return config;
    }

    public static FileConfiguration getBlacklist() { return blacklist; }

    public FileConfiguration getItem() {
        return item;
    }

    public String getItemName(Material material){
        return item.getString(material.toString());
    }

    public static void createConfig(Plugin plugin){
        if (!plugin.getDataFolder().exists()){
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        itemFile = new File(plugin.getDataFolder(), "item.yml");
        blacklistFile = new File(plugin.getDataFolder(), "blacklist.yml");

        if (!configFile.exists()){
            plugin.saveResource("config.yml", true);
            if (!itemFile.exists()){
                plugin.saveResource("item.yml", true);
            }
            if (!blacklistFile.exists()){
                plugin.saveResource("blacklist.yml", true);
            }
        }
    }

    public static void reloadConfig(){
        ConfigManager.createConfig(ShowItem.plugin);
        config = YamlConfiguration.loadConfiguration(configFile);
        item = YamlConfiguration.loadConfiguration(itemFile);
        blacklist = YamlConfiguration.loadConfiguration(blacklistFile);
    }
}
