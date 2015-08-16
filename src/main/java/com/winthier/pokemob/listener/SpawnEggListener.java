package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SpawnEggListener implements Listener {
    private final PokeMobPlugin plugin;

    public SpawnEggListener(PokeMobPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        switch (event.getAction()) {
        case LEFT_CLICK_AIR: case LEFT_CLICK_BLOCK: return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.MONSTER_EGG) return;
        if (!event.getPlayer().isSneaking() && Util.canUseBlock(event.getClickedBlock())) return;
        event.setCancelled(true);
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0.0, 0.5);
        EntityType et = EntityType.fromId(event.getItem().getDurability());
        if (et == null || et.getEntityClass() == null) {
            plugin.getLogger().warning(String.format("Player %s tried to release %s (%d) from spawn egg at %s,%d,%d,%d, but entity cannot be spawned!", player.getName(), (et != null ? Util.enumToHuman(et.name()) : "null"), event.getItem().getDurability(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            return;
        }
        if (!plugin.config.canRelease(et)) {
            plugin.getLogger().warning(String.format("Player %s tried to release %s (%d) from spawn egg at %s,%d,%d,%d, but lacks permission!", player.getName(), (et != null ? Util.enumToHuman(et.name()) : "null"), event.getItem().getDurability(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            return;
        }
        Entity e = loc.getWorld().spawnEntity(loc, et);
        if (e == null || !e.isValid()) return;
        if (!(e instanceof LivingEntity)) {
            e.remove();
            return;
        }
        PokeMobPlugin.instance.getLogger().info(String.format("%s used %s spawn egg in %s at %d,%d,%d.", player.getName(), Util.enumToHuman(et.name()), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        Util.useSpawnEgg(event.getItem(), (LivingEntity)e);
        if (e instanceof Tameable) {
            Tameable tameable = (Tameable)e;
            if (tameable.isTamed()) tameable.setOwner(player);
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack item = event.getPlayer().getItemInHand();
            item.setAmount(item.getAmount() - 1);
            event.getPlayer().setItemInHand(item);
        }
        return;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getItem().getType() != Material.MONSTER_EGG) return;
        if (event.getBlock().getType() != Material.DISPENSER) return;
        event.setCancelled(true);
        Location loc = event.getBlock().getLocation();
        EntityType et = EntityType.fromId(event.getItem().getDurability());
        if (et == null || et.getEntityClass() == null || !plugin.config.canRelease(et)) {
            plugin.getLogger().warning(String.format("%s (%d) was dispensed from spawn egg at %s,%d,%d,%d without permission!", (et != null ? Util.enumToHuman(et.name()) : "null"), event.getItem().getDurability(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            return;
        }
        ItemStack item = event.getItem();
        boolean hasItem = false;
        if (!(event.getBlock().getState() instanceof InventoryHolder)) return;
        Inventory inventory = ((InventoryHolder)event.getBlock().getState()).getInventory();
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack found = inventory.getItem(i);
            if (found != null && found.isSimilar(item)) {
                hasItem = true;
                found.setAmount(found.getAmount() - 1);
                inventory.setItem(i, found);
                break;
            }
        }
        if (!hasItem) return;
        Entity e = loc.getWorld().spawnEntity(event.getVelocity().toLocation(loc.getWorld()), et);
        if (e == null || !e.isValid()) return;
        if (!(e instanceof LivingEntity)) {
            e.remove();
            return;
        }
        Util.useSpawnEgg(event.getItem(), (LivingEntity)e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getItemInHand().getType() != Material.MONSTER_EGG) return;
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
        case OCELOT_BABY:
        {
            event.setCancelled(true);
            break;
        }
        case SPAWNER_EGG:
        case DISPENSE_EGG:
        {
            Entity e = event.getEntity();
            //if (e.getType() == EntityType.PIG_ZOMBIE && event.getLocation().getBlock().getType() == Material.PORTAL) return;
            Location loc = e.getLocation();
            plugin.getLogger().warning(String.format("%s was spawned from egg in %s at %d,%d,%d without a cause!", Util.enumToHuman(e.getType().name()), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            event.setCancelled(true);
            break;
        }
        }
    }
}
