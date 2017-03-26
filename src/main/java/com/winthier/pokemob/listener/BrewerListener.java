package com.winthier.pokemob.listener;

import com.winthier.custom.CustomPlugin;
import com.winthier.pokemob.PokeMobPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class BrewerListener implements Listener {
    final PokeMobPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        final BrewerInventory inv = event.getContents();
        new BukkitRunnable() {
            @Override public void run() {
                for (int i = 0; i < inv.getSize(); ++i) {
                    ItemStack item = inv.getItem(i);
                    if (item == null || item.getType() != Material.SPLASH_POTION) continue;
                    PotionMeta meta = (PotionMeta)item.getItemMeta();
                    if (meta.getBasePotionData().getType() != PotionType.SLOWNESS) continue;
                    if (item == null) continue;
                    if (item.getType() != Material.SPLASH_POTION) continue;
                    inv.setItem(i, CustomPlugin.getInstance().getItemManager().spawnItemStack("pokemob:pokeball", item.getAmount()));
                }
            }
        }.runTask(plugin);
    }
}

