package com.winthier.pokemob;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;

@Getter
@RequiredArgsConstructor
public class Configuration {
    private final PokeMobPlugin plugin;
    private int totalWeight;
    public final Set<EntityType> catchBlacklist = new LinkedHashSet<EntityType>();
    public final Set<EntityType> releaseBlacklist = new LinkedHashSet<EntityType>();
    public final Map<EntityType, Double> entityChance = new LinkedHashMap<EntityType, Double>();
    public final Map<EntityType, Integer> entityWeight = new LinkedHashMap<EntityType, Integer>();
    public final Map<EntityType, Integer> entityMaxHealth = new LinkedHashMap<EntityType, Integer>();
    private double defaultChance = 0.5, monsterChance = 0.5, animalChance = 1.0;
    private int defaultWeight = 10, monsterWeight = 10, animalWeight = 1;
    private int defaultMaxHealth = 100, monsterMaxHealth = 10, animalMaxHealth = 100;

    public void load() {
        totalWeight = plugin.getConfig().getInt("TotalWeight", 100);
        for (String item : plugin.getConfig().getStringList("CatchBlacklist")) {
            try {
                EntityType et = EntityType.valueOf(Util.humanToEnum(item));
                catchBlacklist.add(et);
            } catch (IllegalArgumentException iae) {
                plugin.getLogger().warning("config.CatchBlacklist." + item + ": unknown entity type");
            }
        }
        for (String item : plugin.getConfig().getStringList("ReleaseBlacklist")) {
            try {
                EntityType et = EntityType.valueOf(Util.humanToEnum(item));
                releaseBlacklist.add(et);
            } catch (IllegalArgumentException iae) {
                plugin.getLogger().warning("config.ReleaseBlacklist." + item + ": unknown entity type");
            }
        }
        ConfigurationSection section;
        if (null != (section = plugin.getConfig().getConfigurationSection("EntityChance"))) {
            for (String key : section.getKeys(false)) {
                if (key.equals("default")) {
                    defaultChance = section.getDouble(key, defaultChance);
                } else if (key.equals("monster")) {
                    monsterChance = section.getDouble(key, monsterChance);
                } else if (key.equals("animal")) {
                    animalChance = section.getDouble(key, animalChance);
                } else {
                    try {
                        EntityType et = EntityType.valueOf(Util.humanToEnum(key));
                        entityChance.put(et, section.getDouble(key, 0.0));
                    } catch (IllegalArgumentException iae) {
                        plugin.getLogger().warning("config.EntityChance." + key + ": unknown entity type");
                    }
                }
            }
        }
        if (null != (section = plugin.getConfig().getConfigurationSection("EntityWeight"))) {
            for (String key : section.getKeys(false)) {
                if (key.equals("default")) {
                    defaultWeight = section.getInt(key, defaultWeight);
                } else if (key.equals("monster")) {
                    monsterWeight = section.getInt(key, monsterWeight);
                } else if (key.equals("animal")) {
                    animalWeight = section.getInt(key, animalWeight);
                } else {
                    try {
                        EntityType et = EntityType.valueOf(Util.humanToEnum(key));
                        entityWeight.put(et, section.getInt(key, totalWeight));
                    } catch (IllegalArgumentException iae) {
                        plugin.getLogger().warning("config.EntityWeight." + key + ": unknown entity type");
                    }
                }
            }
        }
        if (null != (section = plugin.getConfig().getConfigurationSection("EntityMaxHealth"))) {
            for (String key : section.getKeys(false)) {
                if (key.equals("default")) {
                    defaultMaxHealth = section.getInt(key, 100);
                } else if (key.equals("monster")) {
                    monsterMaxHealth = section.getInt(key, monsterMaxHealth);
                } else if (key.equals("animal")) {
                    animalMaxHealth = section.getInt(key, animalMaxHealth);
                } else {
                    try {
                        EntityType et = EntityType.valueOf(Util.humanToEnum(key));
                        entityMaxHealth.put(et, section.getInt(key, 100));
                    } catch (IllegalArgumentException iae) {
                        plugin.getLogger().warning("config.EntityMaxHealth." + key + ": unknown entity type");
                    }
                }
            }
        }
    }

    public boolean canEggify(Entity e) {
        if (catchBlacklist.contains(e.getType())) return false;
        return Util.canEggify(e);
    }

    public boolean canRelease(Entity e) {
        return canRelease(e.getType());
    }

    public boolean canRelease(EntityType e) {
        if (releaseBlacklist.contains(e)) return false;
        return Util.canEggify(e);
    }

    public double getEggifyChance(Entity e) {
        Double result = entityChance.get(e.getType());
        if (result != null) return result;
        if (e instanceof Monster) return monsterChance;
        if (e instanceof Animals) return animalChance;
        return defaultChance;
    }

    public int getEntityWeight(Entity e) {
        Integer result = entityWeight.get(e.getType());
        if (result != null) return result;
        if (e instanceof Monster) return monsterWeight;
        if (e instanceof Animals) return animalWeight;
        return defaultWeight;
    }

    public int getEntityMaxHealth(Entity e) {
        Integer result = entityMaxHealth.get(e.getType());
        if (result != null) return result;
        if (e instanceof Monster) return monsterMaxHealth;
        if (e instanceof Animals) return animalMaxHealth;
        return defaultMaxHealth;
    }

    public boolean checkMaxHealth(LivingEntity e) {
        Integer max = getEntityMaxHealth(e);
        return e.getHealth() <= max;
    }
}
