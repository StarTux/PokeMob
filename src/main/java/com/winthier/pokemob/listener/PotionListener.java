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

    static enum Check {
        IMPOSSIBLE,
        PERM,
        TAMED,
        HEALTH,
        CHANCE,
        SUCCESS,
        ;
    }

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
            Check check = shouldEggify(entity, player, event);
            Location loc = entity.getEyeLocation();
            event.setIntensity(entity, 0.0);
            if (check == Check.SUCCESS) {
                Util.eggify(entity);
                loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 64, 0.5, 0.5, 0.5, 0.5);
                weight -= plugin.getConfiguration().getEntityWeight(entity);
                if (weight <= 0) break;
            } else if (check == Check.HEALTH) {
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 12, 0.2, 0.2, 0.2, 0.2);
            } else if (check == Check.CHANCE) {
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 24, 0.4, 0.4, 0.4, 0.01);
            } else if (check == Check.TAMED) {
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                loc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 24, 0.5, 0.5, 0.5, 0.01);
            }
        }
    }

    Check shouldEggify(LivingEntity entity, Player player, PotionSplashEvent event) {
        if (!Util.canEggify(entity)) return Check.IMPOSSIBLE;
        if (player != null && player.getGameMode() == GameMode.CREATIVE) return Check.SUCCESS;
        if (!plugin.getConfiguration().canEggify(entity)) return Check.PERM;
        if (entity.isLeashed() && entity.getLeashHolder() instanceof Player) return Check.PERM;
        // Test event
        EntityDamageByEntityEvent edbee = new EntityDamageByEntityEvent(event.getPotion(), entity, EntityDamageByEntityEvent.DamageCause.CUSTOM, 0.0);
        plugin.getServer().getPluginManager().callEvent(edbee);
        if (edbee.isCancelled()) return Check.PERM;
        // Check ownership
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable)entity;
            if (tameable.isTamed()) {
                AnimalTamer owner = tameable.getOwner();
                if (player == null) {
                    return Check.TAMED;
                } else if (owner != null && !owner.getUniqueId().equals(player.getUniqueId())) {
                    return Check.TAMED;
                } else {
                    return Check.SUCCESS;
                }
            }
        }
        // Check namedness
        if (Util.isPokeMob(entity)) return Check.SUCCESS;
        // Roll dice
        if (!plugin.getConfiguration().checkMaxHealth(entity)) return Check.HEALTH;
        double chance = plugin.getConfiguration().getEggifyChance(entity);
        if (random.nextDouble() > chance) return Check.CHANCE;
        // We win
        return Check.SUCCESS;
    }
}
