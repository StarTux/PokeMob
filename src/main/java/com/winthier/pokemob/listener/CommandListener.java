package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

@RequiredArgsConstructor
public class CommandListener implements CommandExecutor {
    final PokeMobPlugin plugin;

    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        if (args.length == 0) {
            usage(sender);
        } else if (args.length == 1 && args[0].equals("reload")) {
            plugin.reload();
            sender.sendMessage("Configuration reloaded");
        } else if (args.length == 1 && args[0].equals("info")) {
            sender.sendMessage("TotalWeight: " + plugin.getConfiguration().getTotalWeight());
            sender.sendMessage("CatchBlackList:");
            for (EntityType item : plugin.getConfiguration().getCatchBlacklist()) {
                sender.sendMessage("- " + item.name());
            }
            sender.sendMessage("ReleaseBlackList:");
            for (EntityType item : plugin.getConfiguration().getReleaseBlacklist()) {
                sender.sendMessage("- " + item.name());
            }
            sender.sendMessage("EntityChance:");
            for (EntityType key : plugin.getConfiguration().getEntityChance().keySet()) {
                sender.sendMessage("- " + key.name() + ": " + plugin.getConfiguration().entityChance.get(key));
            }
            sender.sendMessage("EntityWeight:");
            for (EntityType key : plugin.getConfiguration().getEntityWeight().keySet()) {
                sender.sendMessage("- " + key.name() + ": " + plugin.getConfiguration().entityWeight.get(key));
            }
            sender.sendMessage("EntityMaxHealth:");
            for (EntityType key : plugin.getConfiguration().getEntityMaxHealth().keySet()) {
                sender.sendMessage("- " + key.name() + ": " + plugin.getConfiguration().entityMaxHealth.get(key));
            }
        } else if (args.length == 1 && args[0].equals("potion")) {
            Player player = sender instanceof Player ? (Player)sender : null;
            if (player == null) return false;
            ItemStack item = new ItemStack(Material.SPLASH_POTION, 64);
            PotionMeta meta = (PotionMeta)item.getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.SLOWNESS));
            meta.setDisplayName("" + ChatColor.BLUE + ChatColor.BOLD + "PokéMob Potion");
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            item.setItemMeta(meta);
            player.getWorld().dropItem(player.getEyeLocation(), item).setPickupDelay(0);
        } else if (args.length == 3 && args[0].equals("mod")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player expected");
                return true;
            }
            Player player = (Player)sender;
            if (player.getItemInHand().getType() != Material.MONSTER_EGG) {
                sender.sendMessage("You must hold a monster egg");
                return true;
            }
            ItemMeta meta = player.getItemInHand().getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<String>();
            String key = Util.enumToHuman(args[1]);
            String value = Util.enumToHuman(args[2]);
            Util.setValue(lore, key, value);
            meta.setLore(lore);
            player.getItemInHand().setItemMeta(meta);
            player.sendMessage("Attribute modified");
        } else {
            usage(sender);
        }
        return true;
    }

    void usage(CommandSender sender) {
        sender.sendMessage("/PokeMob info");
        sender.sendMessage("/PokeMob reload");
        sender.sendMessage("/PokeMob potion");
        sender.sendMessage("/PokeMob mod <key> <value>");
    }
}
