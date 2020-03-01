package com.xiangouo.mc.showitem;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShowItemCommand implements CommandExecutor {

    private ItemStack itemStack;
    private static Map<UUID, LocalDateTime> lastCommandExecute = new ConcurrentHashMap<>();
    public ShowItem plugin;

    public ShowItemCommand(ShowItem showItem) {
        plugin = showItem;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase("showitem")) {
                Player player = (Player) sender;
                if (player.hasPermission("showitem.use")) {
                    int cooldown = plugin.getConfig().getInt("cooldown");
                    if (lastCommandExecute.containsKey(player.getUniqueId())) {
                        LocalDateTime previousTalkTime = lastCommandExecute.get(player.getUniqueId());
                        Duration duration = Duration.between(previousTalkTime, LocalDateTime.now());
                        if (duration.toMillis() < cooldown) {
                            double sec = new BigDecimal((double) (cooldown - duration.toMillis()) / 1000).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("cooldown-message").replace("<sec>", sec + "")));
                            return false;
                        }
                    }
                    ShowItemSerialize showItemSerialize = new ShowItemSerialize();
                    itemStack = player.getInventory().getItemInMainHand();
                    showItemSerialize.showitemtobungee(player, itemStack);
                    String s = plugin.convertItemStackToJson(itemStack);
                    BaseComponent[] hoverEventComponents = new BaseComponent[]{
                            new TextComponent(s)
                    };
                    HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
                    String[] message = plugin.getConfig().getString("message").split("<Item>");
                    String firist = message[0];
                    String second = message[1];
                    TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', firist));
                    TextComponent component1 = new TextComponent(ChatColor.translateAlternateColorCodes('&', itemStack.getItemMeta().getDisplayName()));
                    TextComponent component2 = new TextComponent(ChatColor.translateAlternateColorCodes('&', second));
                    component1.setHoverEvent(event);
                    component.addExtra(component1);
                    component.addExtra(component2);
                    player.spigot().sendMessage(component);
                    lastCommandExecute.put(player.getUniqueId(), LocalDateTime.now());
                    return true;
                }
                player.sendMessage(ChatColor.RED + "你沒有權限這麼做!!");
            }

        }

        return false;
    }

}
