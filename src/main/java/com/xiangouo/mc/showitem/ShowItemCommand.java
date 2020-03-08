package com.xiangouo.mc.showitem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private Map<UUID, LocalDateTime> lastCommandExecute = new ConcurrentHashMap<>();
    public ShowItem plugin;

    public ShowItemCommand(ShowItem showItem) {
        plugin = showItem;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("not player");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("showitem.use")) {
            player.sendMessage(ChatColor.RED + "你沒有權限這麼做!!");
            return true;
        }


        int cooldown = plugin.getConfig().getInt("cooldown");

        if (lastCommandExecute.containsKey(player.getUniqueId())) {
            LocalDateTime previousTalkTime = lastCommandExecute.get(player.getUniqueId());
            Duration duration = Duration.between(previousTalkTime, LocalDateTime.now());
            if (duration.toMillis() < cooldown) {
                double sec = BigDecimal.valueOf((double) (cooldown - duration.toMillis()) / 1000).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                player.sendMessage(ShowItem.getMessage("cooldown-message").replace("<sec>", sec + ""));
                return true;
            }
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            //
        }
        ItemUtils.broadcastItem(player, itemStack);
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemUtils.sendItemTooltipMessage(p, player.getDisplayName(), itemStack);
        }
        lastCommandExecute.put(player.getUniqueId(), LocalDateTime.now());
        return true;
    }

}
