package com.xiangouo.mc.showitem;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {
    private static FileConfiguration config, item;
    private static File configFile, itemFile;

    public static FileConfiguration getConfig() {
        return config;
    }

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

        if (!configFile.exists()){
            plugin.saveResource("config.yml", true);
            if (!itemFile.exists()){
                plugin.saveResource("item.yml", true);
            }
        }
    }

    public static void reloadConfig(){
        ConfigManager.createConfig(ShowItem.plugin);
        config = YamlConfiguration.loadConfiguration(configFile);
        item = YamlConfiguration.loadConfiguration(itemFile);
    }
}
