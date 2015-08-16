package com.winthier.pokemob;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class Configuration {
        private final PokeMobPlugin plugin;
        private int totalWeight;
        public final Set<EntityType> catchBlacklist = new LinkedHashSet<EntityType>();
        public final Set<EntityType> releaseBlacklist = new LinkedHashSet<EntityType>();
        public final Map<EntityType, Double> entityChance = new LinkedHashMap<EntityType, Double>();
        public final Map<EntityType, Integer> entityWeight = new LinkedHashMap<EntityType, Integer>();
        public final Map<EntityType, Integer> entityMaxHealth = new LinkedHashMap<EntityType, Integer>();
        private double defaultChance;
        private int defaultWeight;
        private int defaultMaxHealth;

        public Configuration(PokeMobPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                totalWeight = plugin.getConfig().getInt("TotalWeight", 100);
                for (String item : plugin.getConfig().getStringList("CatchBlacklist")) {
                        EntityType et = EntityType.valueOf(Util.humanToEnum(item));
                        if (et == null) {
                                plugin.getLogger().warning("config.CatchBlacklist." + item + ": unknown entity type");
                        } else {
                                catchBlacklist.add(et);
                        }
                }
                for (String item : plugin.getConfig().getStringList("ReleaseBlacklist")) {
                        EntityType et = EntityType.valueOf(Util.humanToEnum(item));
                        if (et == null) {
                                plugin.getLogger().warning("config.ReleaseBlacklist." + item + ": unknown entity type");
                        } else {
                                releaseBlacklist.add(et);
                        }
                }
                ConfigurationSection section;
                if (null != (section = plugin.getConfig().getConfigurationSection("EntityChance"))) {
                        for (String key : section.getKeys(false)) {
                                if (key.equals("default")) {
                                        defaultChance = section.getDouble(key, 1.0);
                                        continue;
                                }
                                EntityType et = EntityType.valueOf(Util.humanToEnum(key));
                                if (et == null) {
                                        plugin.getLogger().warning("config.EntityChance." + key + ": unknown entity type");
                                } else {
                                        entityChance.put(et, section.getDouble(key, 1.0));
                                }
                        }
                }
                if (null != (section = plugin.getConfig().getConfigurationSection("EntityWeight"))) {
                        for (String key : section.getKeys(false)) {
                                if (key.equals("default")) {
                                        defaultWeight = section.getInt(key, 10);
                                        continue;
                                }
                                EntityType et = EntityType.valueOf(Util.humanToEnum(key));
                                if (et == null) {
                                        plugin.getLogger().warning("config.EntityWeight." + key + ": unknown entity type");
                                } else {
                                        entityWeight.put(et, section.getInt(key, totalWeight));
                                }
                        }
                }
                if (null != (section = plugin.getConfig().getConfigurationSection("EntityMaxHealth"))) {
                        for (String key : section.getKeys(false)) {
                                if (key.equals("default")) {
                                        defaultMaxHealth = section.getInt(key, 100);
                                        continue;
                                }
                                EntityType et = EntityType.valueOf(Util.humanToEnum(key));
                                if (et == null) {
                                        plugin.getLogger().warning("config.EntityMaxHealth." + key + ": unknown entity type");
                                } else {
                                        entityMaxHealth.put(et, section.getInt(key, 100));
                                }
                        }
                }
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
        }

        public void onDisable() {
                catchBlacklist.clear();
                releaseBlacklist.clear();
                entityChance.clear();
                entityWeight.clear();
                entityMaxHealth.clear();
        }

        public final int totalWeight() {
                return totalWeight;
        }

        public final boolean canEggify(Entity e) {
                if (catchBlacklist.contains(e.getType())) return false;
                return Util.canEggify(e);
        }

        public final boolean canRelease(Entity e) {
                return canRelease(e.getType());
        }

        public final boolean canRelease(EntityType e) {
                if (releaseBlacklist.contains(e)) return false;
                return Util.canEggify(e);
        }

        public final double eggifyChance(Entity e) {
                Double result = entityChance.get(e.getType());
                if (result == null) return defaultChance;
                return result;
        }

        public final int entityWeight(Entity e) {
                Integer result = entityWeight.get(e.getType());
                if (result == null) return defaultWeight;
                return result;
        }

        public final boolean checkMaxHealth(LivingEntity e) {
                Integer max = entityMaxHealth.get(e.getType());
                if (max == null) max = defaultMaxHealth;
                if (e.getHealth() <= max) return true;
                return false;
        }
}
