package com.xiangouo.mc.showitem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShowItemSerialize implements ConfigurationSerializable {

    private ItemStack itemStack;

    public void showitemtobungee(Player player, ItemStack itemStack) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward"); // So BungeeCord knows to forward it
        out.writeUTF("ALL");
        out.writeUTF("MyChannel"); // The channel name to check if this your data

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        Gson gson = new Gson();
        this.itemStack = itemStack;
        String s = gson.toJson(serialize());
        try {
            msgout.writeUTF(player.getDisplayName());
            msgout.writeUTF(s);
            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
        } catch (IOException exception){
            exception.printStackTrace();
        }

        player.sendPluginMessage(ShowItem.plugin, "BungeeCord", out.toByteArray());
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> itemMap = new HashMap<>(itemStack.serialize());
        if (itemStack.hasItemMeta()) {
            itemMap.put("meta", new HashMap<>(itemStack.getItemMeta().serialize()));
        }
        return itemMap;
    }

    public ItemStack deserialize(Map<String, Object> map){
        if (map.get("meta") instanceof Map){
            Map<String, Object> obj = (Map<String, Object>) map.get("meta");
            Class<?> cls = ConfigurationSerialization.getClassByAlias(ItemMeta.class.getSimpleName());
            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(obj, (Class<? extends ConfigurationSerializable>) cls);
            map.put("meta", meta);
        }
        return ItemStack.deserialize(map);
    }
}
