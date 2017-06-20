package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
public class EntityListener implements Listener {
    final PokeMobPlugin plugin;
    @Getter @Setter EntityType expectedEntity;
    @Getter @Setter Entity spawnedEntity;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity e = event.getEntity();
        if (e.getType() == EntityType.VILLAGER && e.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)e.getLastDamageCause()).getDamager().getType() == EntityType.ZOMBIE) return;
        if (!plugin.getConfiguration().canEggify(e)) return;
        if (!Util.isPokeMob(e)) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        e.setHealth(1.0);
        if (Util.eggify(e)) {
            e.getWorld().playSound(e.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.3f, 1.2f);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getType() == expectedEntity) {
            spawnedEntity = event.getEntity();
        }
    }
}
