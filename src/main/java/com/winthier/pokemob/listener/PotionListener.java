package com.winthier.pokemob.listener;

import com.winthier.pokemob.PokeMobPlugin;
import com.winthier.pokemob.Util;
import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PotionListener implements Listener {
    private final PokeMobPlugin plugin;
    private final Random random = new Random(System.currentTimeMillis());

    public PotionListener(PokeMobPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        Player player = null;
        if (event.getPotion().getShooter() instanceof Player) {
            player = (Player)event.getPotion().getShooter();
        }
        if (event.getPotion().getShooter() != null && player == null) return;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (effect.getType().equals(PotionEffectType.SLOW)) {
                int weight = plugin.config.totalWeight();
            entityLoop:
                for (LivingEntity e : event.getAffectedEntities()) {
                    if (plugin.config.canEggify(e)) {
                        // creative override
                        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
                            Util.eggify(e);
                            continue entityLoop;
                        }
                        weight -= plugin.config.entityWeight(e);
                        if (weight < 0) break entityLoop;
                        if (e.getPassenger() != null) continue entityLoop;
                        if (e instanceof Tameable) {
                            Tameable tameable = (Tameable)e;
                            if (tameable.isTamed()) {
                                AnimalTamer owner = tameable.getOwner();
                                if (player == null || (owner != null && !owner.getName().equals(player.getName()))) {
                                    continue entityLoop;
                                }
                            }
                        }
                        double chance = plugin.config.eggifyChance(e) * event.getIntensity(e);
                        if (Util.isPokeMob(e) || (plugin.config.checkMaxHealth(e) && random.nextDouble() < chance)) {
                            EntityDamageByEntityEvent edbee = new EntityDamageByEntityEvent(event.getPotion(), e, EntityDamageByEntityEvent.DamageCause.CUSTOM, 0.0);
                            plugin.getServer().getPluginManager().callEvent(edbee);
                            if (!edbee.isCancelled()) {
                                event.setIntensity(e, 0.0);
                                Util.eggify(e);
                            }
                        } else {
                            Util.eggifyFail(e);
                        }
                    }
                }
            } else if (effect.getType().equals(PotionEffectType.NIGHT_VISION)) {
            entityLoop:
                for (LivingEntity e : event.getAffectedEntities()) {
                    if (!(e instanceof Creature)) continue entityLoop;
                targetLoop:
                    for (LivingEntity f : event.getAffectedEntities()) {
                        //if (!(f instanceof Creature)) continue entityLoop;
                        if (e != f) {
                            //((Creature)e).setTarget(f);
                            //break targetLoop;
                            Projectile p = e.launchProjectile(Snowball.class);
                            Location l1 = p.getLocation();
                            Location l2 = f.getLocation();
                            Vector v = new Vector(l2.getX() - l1.getX(),
                                                  l2.getY() - l1.getY(),
                                                  l2.getZ() - l1.getZ());
                            v = v.normalize();
                            p.setVelocity(v);
                            p.setShooter(e);
                        }
                    }
                }
            }
        }
    }
}
