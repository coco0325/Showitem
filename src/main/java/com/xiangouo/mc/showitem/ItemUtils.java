package com.xiangouo.mc.showitem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ItemUtils {

    public static final String CHANNEL = "ShowItem";
    private static final Gson GSON = new Gson();
    private static ConfigManager configManager = new ConfigManager();

    public static void broadcastItem(Player player, ItemStack itemStack) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward"); // So BungeeCord knows to forward it
        out.writeUTF("ALL");
        out.writeUTF(CHANNEL); // The channel name to check if this your data
        String s = ReflectionUtil.convertItemStackToJson(itemStack);
        System.out.println("Orign：" + s);
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

    public static void sendItemTooltipMessage(Player player, String name, ItemStack item) {
        String s = ReflectionUtil.convertItemStackToJson(item);
        BaseComponent[] hoverEventComponents = new BaseComponent[]{new TextComponent(s)};
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
        String[] message = ShowItem.getMessage("message").split("<Item>");
        String first = message[0];
        String second = message.length >= 2 ? message[1] : "";
        boolean displayname = Objects.requireNonNull(item.getItemMeta()).hasDisplayName();
        TextComponent component = new TextComponent(first.replace("<player>", name));
        TextComponent component1 = new TextComponent(!displayname ? Optional.ofNullable(configManager.getItemName(item.getType())).orElse(item.getType().toString()) : item.getItemMeta().getDisplayName());
        TextComponent component2 = new TextComponent(second);
        component1.setHoverEvent(event);
        component.addExtra(component1);
        component.addExtra(component2);
        player.spigot().sendMessage(component);
    }
}
