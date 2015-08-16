package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandListener implements CommandExecutor {
        private final PokeMobPlugin plugin;

        public CommandListener(PokeMobPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getCommand("pokemob").setExecutor(this);
        }

        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                if (args.length == 0) {
                        sender.sendMessage("PokeMob " + plugin.getDescription().getVersion());
                        sender.sendMessage("Usage: /pokemob [subcommand]");
                        sender.sendMessage("SubCommands:");
                        sender.sendMessage("- info");
                        sender.sendMessage("- reload");
                        sender.sendMessage("- mod <key> <value>");
                } else if (args.length == 1 && args[0].equals("reload")) {
                        plugin.config.onDisable();
                        plugin.reloadConfig();
                        plugin.config.onEnable();
                        sender.sendMessage("Configuration reloaded");
                } else if (args.length == 1 && args[0].equals("info")) {
                        sender.sendMessage("TotalWeight: " + plugin.config.totalWeight());
                        sender.sendMessage("CatchBlackList:");
                        for (EntityType item : plugin.config.catchBlacklist) {
                                sender.sendMessage("- " + item.name());
                        }
                        sender.sendMessage("ReleaseBlackList:");
                        for (EntityType item : plugin.config.releaseBlacklist) {
                                sender.sendMessage("- " + item.name());
                        }
                        sender.sendMessage("EntityChance:");
                        for (EntityType key : plugin.config.entityChance.keySet()) {
                                sender.sendMessage("- " + key.name() + ": " + plugin.config.entityChance.get(key));
                        }
                        sender.sendMessage("EntityWeight:");
                        for (EntityType key : plugin.config.entityWeight.keySet()) {
                                sender.sendMessage("- " + key.name() + ": " + plugin.config.entityWeight.get(key));
                        }
                        sender.sendMessage("EntityMaxHealth:");
                        for (EntityType key : plugin.config.entityMaxHealth.keySet()) {
                                sender.sendMessage("- " + key.name() + ": " + plugin.config.entityMaxHealth.get(key));
                        }
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
                        return false;
                }
                return true;
        }
}
