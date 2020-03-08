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
import java.util.Map;

public class ItemUtils {

    public static final String CHANNEL = "ShowItem";
    private static final Gson GSON = new Gson();

    public static void broadcastItem(Player player, ItemStack itemStack) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward"); // So BungeeCord knows to forward it
        out.writeUTF("ALL");
        out.writeUTF(CHANNEL); // The channel name to check if this your data
        String s = GSON.toJson(serialize(itemStack));
        try (
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes)) {
            msgout.writeUTF(player.getDisplayName());
            msgout.writeUTF(s);
            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        player.sendPluginMessage(ShowItem.plugin, "BungeeCord", out.toByteArray());
    }


    public static Map<String, Object> serialize(ItemStack itemStack) {
        Map<String, Object> itemMap = itemStack.serialize();
        if (itemStack.getItemMeta() != null) {
            itemMap.put("meta", itemStack.getItemMeta().serialize());
        }
        return itemMap;
    }

    @SuppressWarnings("unchecked")
    public static ItemStack deserialize(Map<String, Object> map) {
        if (map.get("meta") instanceof Map) {
            Map<String, Object> obj = (Map<String, Object>) map.get("meta");
            Class<?> cls = ConfigurationSerialization.getClassByAlias(ItemMeta.class.getSimpleName());
            if (cls != null) {
                ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(obj, (Class<? extends ConfigurationSerializable>) cls);
                map.put("meta", meta);
            }
        }
        return ItemStack.deserialize(map);
    }
}
