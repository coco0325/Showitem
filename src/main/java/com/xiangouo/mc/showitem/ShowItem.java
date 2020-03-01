package com.xiangouo.mc.showitem;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;

public class ShowItem extends JavaPlugin implements PluginMessageListener {

    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        ConfigurationSerialization.registerClass(ShowItemSerialize.class);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        FileConfiguration config = this.getConfig();
        config.addDefault("cooldown", 1000);
        config.addDefault("cooldown-message", "&e展示物品// &7你發送指令太快，請等待 <sec> 秒!");
        config.addDefault("message", "&e展示物品// &7你展示了 &7<Item> !");
        config.addDefault("bungee-message", "&e展示物品// &7<player> 展示了 &7<Item> !");
        config.options().copyDefaults(true);
        saveConfig();
        getCommand("showitem").setExecutor(new ShowItemCommand(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] messages) {
        if (!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(messages);
        try {
            String subchannel = in.readUTF();
            if (subchannel.equalsIgnoreCase("MyChannel")) {
                final Short length = in.readShort();
                final byte[] bytesMessage = new byte[length];

                in.readFully(bytesMessage);

                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytesMessage));
                ShowItemSerialize showItemSerialize = new ShowItemSerialize();
                String playername = inputStream.readUTF();
                String input = inputStream.readUTF();
                Gson gson = new Gson();
                Map<String, Object> json = gson.fromJson(input, Map.class);
                ItemStack itemStack = showItemSerialize.deserialize(json);
                sendItemTooltipMessage(player, playername, itemStack);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sendItemTooltipMessage(Player player, String name, ItemStack item) {

        String s = convertItemStackToJson(item);
        BaseComponent[] hoverEventComponents = new BaseComponent[]{
                new TextComponent(s)
        };
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
        String[] message = plugin.getConfig().getString("bungee-message").split("<Item>");
        String firist = message[0];
        String second = message[1];
        TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', firist.replace("<player>", name)));
        TextComponent component1 = new TextComponent(ChatColor.translateAlternateColorCodes('&', item.getItemMeta().getDisplayName()));
        TextComponent component2 = new TextComponent(ChatColor.translateAlternateColorCodes('&', second));
        component1.setHoverEvent(event);
        component.addExtra(component1);
        component.addExtra(component2);
        player.spigot().sendMessage(component);
    }

    public static String convertItemStackToJson(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Optional<Method> asNMSCopyMethodOpt = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Optional<Method> saveNmsItemStackMethodOpt = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method


        try {
            Method asNMSCopyMethod = asNMSCopyMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 CraftItemStack 的 asNMSCopy 方法"));
            Method saveNmsItemStackMethod = saveNmsItemStackMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 ItemStack 的 save 方法"));
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.getConstructor().newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "ItemStack 轉換json失敗", t);
            return null;
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }
}
