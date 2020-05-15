package com.xiangouo.mc.showitem;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;

public class ShowItem extends JavaPlugin implements PluginMessageListener {

    public static Plugin plugin;

    public static String getMessage(String path) {
        return Optional.ofNullable(ConfigManager.getConfig().getString(path))
                .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                .orElseThrow(() -> new IllegalStateException("找不到路徑 " + path));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatColor.GOLD + "展示插件關閉.....");
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        ConfigManager.createConfig(this);
        ConfigManager.reloadConfig();
        Optional.ofNullable(getCommand("showitem")).ifPresent(c -> c.setExecutor(new ShowItemCommand(this)));
        Bukkit.getLogger().info(ChatColor.GOLD + "展示插件開啟.....");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] messages) {
        if (!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(messages);
        String subchannel = in.readUTF();
        if (!subchannel.equalsIgnoreCase(ItemUtils.CHANNEL)) return;
        final short length = in.readShort();
        final byte[] bytesMessage = new byte[length];
        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytesMessage))) {
            in.readFully(bytesMessage);
            String showingPlayer = inputStream.readUTF();
            String input = inputStream.readUTF();
            ItemStack itemStack = ReflectionUtil.deserializeItemStackFromNBTJson(input);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("showitem.look")) {
                    ItemUtils.sendItemTooltipMessage(player, showingPlayer, itemStack);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
