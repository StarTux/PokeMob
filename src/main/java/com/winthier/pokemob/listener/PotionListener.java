package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class PotionListener implements Listener {
    final PokeMobPlugin plugin;
    final Random random = new Random(System.currentTimeMillis());

    static boolean potionHasSlowness(ThrownPotion potion) {
        for (PotionEffect effect : potion.getEffects()) {
            if (effect.getType().equals(PotionEffectType.SLOW)) return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!potionHasSlowness(event.getPotion())) return;
        Player player = null;
        if (event.getPotion().getShooter() instanceof Player) {
            player = (Player)event.getPotion().getShooter();
        }
        if (player == null && event.getPotion().getShooter() != null) return;
        int weight = plugin.getConfiguration().getTotalWeight();
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!Util.canEggify(entity)) continue;
            // Creative override
            if (player != null && player.getGameMode() == GameMode.CREATIVE) {
                Util.eggify(entity);
                event.setIntensity(entity, 0.0);
                continue;
            }
            if (!plugin.getConfiguration().canEggify(entity)) continue;
            // Test event
            EntityDamageByEntityEvent edbee = new EntityDamageByEntityEvent(event.getPotion(), entity, EntityDamageByEntityEvent.DamageCause.CUSTOM, 0.0);
            plugin.getServer().getPluginManager().callEvent(edbee);
            if (edbee.isCancelled()) continue;
            Location loc = entity.getEyeLocation();
            boolean success = hitEntity(entity, player);
            if (success) {
                loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 64, 0.5, 0.5, 0.5, 0.5);
                event.setIntensity(entity, 0.0);
                weight -= plugin.getConfiguration().getEntityWeight(entity);
                if (weight <= 0) break;
            } else {
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 24, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    boolean hitEntity(LivingEntity entity, Player player) {
        // Check ownership
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable)entity;
            if (tameable.isTamed()) {
                AnimalTamer owner = tameable.getOwner();
                if (player == null || (owner != null && !owner.getUniqueId().equals(player.getUniqueId()))) {
                    return false;
                }
            }
        }
        // Roll dice unless has colorful name
        if (!Util.isPokeMob(entity)) {
            double chance = plugin.getConfiguration().getEggifyChance(entity);
            if (!plugin.getConfiguration().checkMaxHealth(entity)) return false;
            if (random.nextDouble() > chance) return false;
        }
        // We win
        Util.eggify(entity);
        return true;
    }
}
