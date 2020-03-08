package com.xiangouo.mc.showitem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    public ShowItem plugin;
    private FileConfiguration config, item;
    private File configFile, itemFile;

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getItem() {
        return item;
    }

    public void createConfig(){
        if (!plugin.getDataFolder().exists()){
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        itemFile = new File(plugin.getDataFolder(), "item.yml");

        if (!configFile.exists()){
            try{
                configFile.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
            if (!itemFile.exists()){
                try {
                    itemFile.createNewFile();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            item = YamlConfiguration.loadConfiguration(itemFile);
        }
    }
}
