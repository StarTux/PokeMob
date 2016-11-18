package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
public class EntityListener implements Listener {
    final PokeMobPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity e = event.getEntity();
        if (e.getType() == EntityType.VILLAGER && e.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)e.getLastDamageCause()).getDamager().getType() == EntityType.ZOMBIE) return;                    
        if (!plugin.getConfiguration().canEggify(e)) return;
        if (!Util.isPokeMob(e)) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        e.setHealth(1.0);
        Util.eggify(e);
    }
}
