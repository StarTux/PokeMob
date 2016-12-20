package com.winthier.pokemob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Button;
import org.bukkit.material.Colorable;
import org.bukkit.material.Diode;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;

public class Util {
    public static void eggify(LivingEntity e) {
        if (!canEggify(e)) return;
        ItemStack stack = Dirty.eggify(e);
        if (stack == null) return;
        saveLore(e, stack);
        Location loc = e.getLocation();
        e.getWorld().dropItemNaturally(loc, stack);
        e.getEquipment().clear();
        if (e instanceof AbstractHorse) ((AbstractHorse)e).getInventory().clear();
        e.remove();
    }

    public static LivingEntity useSpawnEgg(Location location, EntityType entityType, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        // Spawn the entity
        String dataTag = Dirty.getSpawnEggDataTag(item);
        Entity e;
        if (dataTag != null) {
            e = PokeMobPlugin.getInstance().summon(entityType, location, dataTag);
            if (e == null) {
                PokeMobPlugin.getInstance().getLogger().warning(String.format("Failed spawning %s with data tag: %s", entityType.getName(), dataTag));
            }
        } else { // Legacy
            // Build the lore
            Map<String, String> lore;
            if (meta == null || !meta.hasLore()) {
                lore = new HashMap<>();
            } else {
                lore = loreToMap(meta.getLore());
            }
            // Support legacy spawn eggs
            if (entityType == EntityType.SKELETON) {
                String skeletonType = getValue(lore, "Skeleton Type");
                if (skeletonType == null) {
                    // Do nothing
                } else if ("Normal".equalsIgnoreCase(skeletonType)) {
                    // Do nothing
                } else if ("Stray".equalsIgnoreCase(skeletonType)) {
                    entityType = EntityType.STRAY;
                } else if ("Wither".equalsIgnoreCase(skeletonType)) {
                    entityType = EntityType.WITHER_SKELETON;
                }
            } else if (entityType == EntityType.ZOMBIE) {
                String zombieType = getValue(lore, "Zombie Type");
                if ("Villager".equalsIgnoreCase(zombieType)) {
                    entityType = EntityType.ZOMBIE_VILLAGER;
                }
            } else if (entityType == EntityType.HORSE) {
                String horseVariant = getValue(lore, "Horse Variant");
                if (horseVariant == null) {
                } else if ("Donkey".equalsIgnoreCase(horseVariant)) {
                    entityType = EntityType.DONKEY;
                } else if ("Horse".equalsIgnoreCase(horseVariant)) {
                } else if ("Llama".equalsIgnoreCase(horseVariant)) {
                    entityType = EntityType.LLAMA;
                } else if ("Mule".equalsIgnoreCase(horseVariant)) {
                    entityType = EntityType.MULE;
                } else if ("Skeleton Horse".equalsIgnoreCase(horseVariant)) {
                    entityType = EntityType.SKELETON_HORSE;
                } else if ("Undead Horse".equalsIgnoreCase(horseVariant)) {
                    entityType = EntityType.ZOMBIE_HORSE;
                }
            }
            e = PokeMobPlugin.getInstance().summon(entityType, location, "");
            if (e instanceof Ageable) {
                Ageable ageable = (Ageable)e;
                ageable.setBreed(false);
            }
            if (e != null && e instanceof LivingEntity) {
                applyLore((LivingEntity)e, lore);
            }
        }
        if (e == null || !e.isValid()) return null;
        if (!(e instanceof LivingEntity)) {
            e.remove();
            return null;
        }
        LivingEntity living = (LivingEntity)e;
        // Set the name
        if (meta != null && meta.hasDisplayName()) {
            living.setCustomName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));
            living.setCustomNameVisible(true);
            living.setRemoveWhenFarAway(false);
        }
        return living;
    }

    public static boolean isPokeMob(LivingEntity e) {
        if (!e.isCustomNameVisible()) return false;
        String customName = e.getCustomName();
        if (customName == null) return false;
        if (ChatColor.stripColor(customName).isEmpty()) return false;
        if (customName.charAt(0) != ChatColor.COLOR_CHAR) return false;
        if (customName.charAt(0) == ChatColor.RESET.getChar()) return false;
        return true;
    }

    /**
     * @return A collection of dropped items or null
     */
    static void saveLore(LivingEntity e, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        {
            String name = e.getCustomName();
            if (name != null) {
                meta.setDisplayName(name);
            }
        }
        List<String> lore;
        if (!meta.hasLore()) {
            lore = new ArrayList<String>();
        } else {
            lore = meta.getLore();
        }
        Map<String, String> data = new LinkedHashMap<String, String>();
        saveLore(e, data);
        for (String key : data.keySet()) {
            setValue(lore, key, data.get(key));
        }
        Collections.sort(lore);
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    /**
     * @return drops
     */
    static void saveLore(LivingEntity e, Map<String, String> lore) {
        if (e instanceof Damageable) {
            Damageable damageable = (Damageable)e;
            int health = (int)damageable.getHealth();
            int maxHealth = (int)damageable.getMaxHealth();
            if (health != maxHealth) {
                lore.put("Health", "" + health + "/" + maxHealth);
            } else if (e instanceof AbstractHorse) {
                lore.put("Health", "" + health);
            }
        }
        if (e instanceof Ageable) {
            Ageable ageable = (Ageable)e;
            if (!ageable.isAdult()) {
                lore.put("Age", "Baby");
            }
        }
        if (e instanceof Tameable) {
            Tameable tameable = (Tameable)e;
            if (tameable.isTamed()) {
                lore.put("Tamed", "True");
            }
        }
        if (e instanceof Colorable) {
            Colorable colorable = (Colorable)e;
            lore.put("Color", enumToHuman(colorable.getColor().name()));
        }
        if (e instanceof Sheep) {
            Sheep sheep = (Sheep)e;
            if (sheep.isSheared()) {
                lore.put("Sheared", "True");
            }
        }
        if (e instanceof Pig) {
            Pig pig = (Pig)e;
            if (pig.hasSaddle()) {
                lore.put("Saddled", "True");
            }
        }
        if (e instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)e;
            if (ocelot.getCatType() != Ocelot.Type.WILD_OCELOT) {
                lore.put("Cat Type", enumToHuman(ocelot.getCatType().name()));
            }
        }
        if (e instanceof Wolf) {
            Wolf wolf = (Wolf)e;
            if (wolf.isTamed()) {
                lore.put("Collar Color", enumToHuman(wolf.getCollarColor().name()));
            }
        }
        if (e instanceof Villager) {
            Villager villager = (Villager)e;
            lore.put("Profession", enumToHuman(villager.getProfession().name()));
        }
        if (e instanceof Horse) {
            Horse horse = (Horse)e;
            if (e.getType() == EntityType.HORSE) {
                Horse.Color color = horse.getColor();
                lore.put("Horse Color", enumToHuman(color.name()));
            }
        }
        if (e instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)e;
            if (horse.isTamed()) {
                lore.put("Tamed", "True");
            }
            String horseJumpStrength = "" + horse.getJumpStrength();
            if (horseJumpStrength.length() > 4) {
                horseJumpStrength = horseJumpStrength.substring(0, 4);
            }
            lore.put("Jump Strength", horseJumpStrength);
            AttributeInstance att = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            lore.put("Speed", String.format("%.2f", att.getBaseValue()));
        }
        if (e instanceof ChestedHorse) {
            ChestedHorse horse = (ChestedHorse)e;
            if (horse.isCarryingChest()) {
                lore.put("Carries Chest", "True");
            }
        }
        if (e instanceof Llama) {
            Llama llama = (Llama)e;
            lore.put("Llama Color", enumToHuman(llama.getColor().name()));
            if (llama.isAdult()) {
                lore.put("Strength", "" + llama.getStrength());
            }
        }
        if (e instanceof Zombie) {
            Zombie zombie = (Zombie)e;
            if (zombie.isBaby()) {
                lore.put("Age", "Baby");
            }
        }
        if (e instanceof Creeper) {
            Creeper creeper = (Creeper)e;
            if (creeper.isPowered()) {
                lore.put("Powered", "True");
            }
        }
        if (e instanceof Slime) {
            Slime slime = (Slime)e;
            lore.put("Size", "" + slime.getSize());
        }
        if (e instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)e;
            lore.put("Rabbit Type", enumToHuman(rabbit.getRabbitType().name()));
        }
    }

    public static Map<String, String> loreToMap(List<String> lore) {
        Map<String, String> result = new HashMap<String, String>();
        for (String i : lore) {
            String j = ChatColor.stripColor(i);
            String tokens[] = j.split(": *", 2);
            if (tokens.length == 2) {
                result.put(tokens[0], tokens[1]);
            } else {
                result.put(tokens[0], "True");
            }
        }
        return result;
    }

    static String firstLetterUp(String s) {
        if (s.length() == 0) return s;
        if (s.length() == 1) return s.toUpperCase();
        return "" + Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static String enumToHuman(String s) {
        String tokens[] = s.split("_");
        if (tokens.length == 0) return "";
        StringBuilder sb = new StringBuilder(firstLetterUp(tokens[0]));
        for (int i = 1; i < tokens.length; ++i) {
            sb.append(" ").append(firstLetterUp(tokens[i]));
        }
        return sb.toString();
    }
        
    public static String humanToEnum(String s) {
        return s.toUpperCase().replaceAll(" ", "_").replaceAll("-", "_");
    }

    public static void setValue(List<String> lore, String key, String value) {
        final String PREFIX = "" + ChatColor.BLUE + ChatColor.ITALIC;
        final String SEP = "" + ChatColor.GOLD + " ";
        final String OFF = "" + ChatColor.DARK_GRAY;
        final String VAL = "" + ChatColor.GOLD;
        String line;
        if (value.equals("True")) {
            line = PREFIX + key;
        } else if (value.contains("/")) {
            String[] tokens = value.split("/", 2);
            line = PREFIX + key + SEP + tokens[0] + OFF + "/" + VAL + tokens[1];
        } else if (value.contains(".")) {
            String[] tokens = value.split("\\.", 2);
            line = PREFIX + key + SEP + tokens[0] + OFF + "." + VAL + tokens[1];
        } else {
            line = PREFIX + key + SEP + value;
        }
        lore.add(line);
        return;
    }

    public static String getValue(Map<String, String> lore, String key) {
        Object result = lore.get(key);
        if (result == null) return null;
        return result.toString();
    }

    public static boolean canUseBlock(Block block) {
        if (block == null) return false;
        BlockState state = block.getState();
        if (state instanceof InventoryHolder) return true;
        MaterialData data = state.getData();
        if (state instanceof Button ||
            state instanceof Lever ||
            state instanceof Diode) return true;
        return false;
    }

    public static boolean canEggify(Entity e) {
        return canEggify(e.getType());
    }

    public static boolean canEggify(EntityType et) {
        if (et == EntityType.PLAYER) return false;
        if (!et.isAlive()) return false;
        return true;
    }

    // Legacy
    
    public static ItemStack loadEquippedItem(Map<String, String> lore, String key) {
        String value = getValue(lore, key);
        if (value == null) return null;
        String tokens[] = value.split(":");
        Material mat;
        try {
            mat = Material.valueOf(humanToEnum(tokens[0]));
        } catch (IllegalArgumentException iae) {
            return null;
        }
        int d = 0;
        if (tokens.length == 2) {
            try {
                d = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException nfe) {}
        }
        return new ItemStack(mat, 1, (short)d);
    }
    
    public static void applyLore(LivingEntity e, Map<String, String> lore) {
        // tamed wolves have more health than untamed
        // ones. So we handle Tameable first, then reset
        // max health under Damageable.
        if (e instanceof Tameable) {
            Tameable tameable = (Tameable)e;
            String tamed = getValue(lore, "Tamed");
            if (tamed != null && tamed.equalsIgnoreCase("True")) {
                tameable.setTamed(true);
                e.setRemoveWhenFarAway(false);
            } else {
                tameable.setTamed(false);
            }
        }
        if (e instanceof Damageable) {
            Damageable damageable = (Damageable)e;
            damageable.resetMaxHealth();
            damageable.setHealth(damageable.getMaxHealth());
            String health = getValue(lore, "Health");
            if (health != null) {
                String tokens[] = health.split(java.util.regex.Pattern.quote("/"), 2);
                if (tokens.length == 2) {
                    try {
                        int h = Integer.parseInt(tokens[0]);
                        int mh = Integer.parseInt(tokens[1]);
                        damageable.setMaxHealth(Math.max(2, mh));
                        damageable.setHealth(Math.max(2, h));
                    } catch (NumberFormatException nfe) {
                    } catch (IllegalArgumentException iae) {}
                } else {
                    try {
                        int h = Integer.parseInt(health);
                        // Horse fix: If there's no max health, make up a reasonable one.
                        if (e.getType() == EntityType.HORSE) {
                            damageable.setMaxHealth(Math.max(20, h));
                        }
                        damageable.setHealth(Math.max(2, h));
                    } catch (NumberFormatException nfe) {
                    } catch (IllegalArgumentException iae) {}
                }
            }
            // Horses fix: When there aren't any
            // health information, make up reasonable
            // ones.
            else if (e.getType() == EntityType.HORSE) {
                damageable.setMaxHealth(20);
                damageable.setHealth(20);
            }
        }
        if (e instanceof Ageable) {
            Ageable ageable = (Ageable)e;
            String age = getValue(lore, "Age");
            if (age != null && age.equals("Baby")) {
                ageable.setBaby();
            } else {
                ageable.setAdult();
            }
        }
        if (e instanceof Colorable) {
            Colorable colorable = (Colorable)e;
            String color = getValue(lore, "Color");
            if (color != null) {
                try {
                    DyeColor dc = DyeColor.valueOf(humanToEnum(color));
                    colorable.setColor(dc);
                } catch (IllegalArgumentException iae) {}
            }
        }
        if (e instanceof Sheep) {
            Sheep sheep = (Sheep)e;
            String sheared = getValue(lore, "Sheared");
            if (sheared != null && sheared.equalsIgnoreCase("True")) {
                sheep.setSheared(true);
            } else {
                sheep.setSheared(false);
            }
        }
        if (e instanceof Pig) {
            Pig pig = (Pig)e;
            String saddled = getValue(lore, "Saddled");
            if (saddled != null && saddled.equalsIgnoreCase("True")) {
                pig.setSaddle(true);
            } else {
                pig.setSaddle(false);
            }
        }
        if (e instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)e;
            String catType = getValue(lore, "Cat Type");
            if (catType != null) {
                ocelot.setRemoveWhenFarAway(false);
                try {
                    Ocelot.Type ct = Ocelot.Type.valueOf(humanToEnum(catType));
                    ocelot.setCatType(ct);
                } catch (IllegalArgumentException iae) {}
            }
        }
        if (e instanceof Wolf) {
            Wolf wolf = (Wolf)e;
            String angry = getValue(lore, "Angry");
            // if (angry != null && angry.equalsIgnoreCase("True")) {
            //         wolf.setAngry(true);
            // } else {
            //         wolf.setAngry(false);
            // }
            String collarColor = getValue(lore, "Collar Color");
            if (collarColor != null) {
                try {
                    DyeColor dc = DyeColor.valueOf(humanToEnum(collarColor));
                    wolf.setCollarColor(dc);
                } catch (IllegalArgumentException iae) {}
            }
        }
        if (e instanceof Villager) {
            Villager villager = (Villager)e;
            String profession = getValue(lore, "Profession");
            if (profession != null) {
                try {
                    Profession p = Profession.valueOf(humanToEnum(profession));
                    villager.setProfession(p);
                } catch (IllegalArgumentException iae) {}
            }
        }
        if (e instanceof Horse) {
            Horse horse = (Horse)e;
            String horseStyle = getValue(lore, "Horse Style");
            if (horseStyle != null) {
                try {
                    Horse.Style style = Horse.Style.valueOf(humanToEnum(horseStyle));
                    horse.setStyle(style);
                } catch (IllegalArgumentException iae) {}
            } else {
                horse.setStyle(Horse.Style.NONE);
            }
            String horseColor = getValue(lore, "Horse Color");
            if (horseColor != null) {
                try {
                    Horse.Color color = Horse.Color.valueOf(humanToEnum(horseColor));
                    horse.setColor(color);
                } catch (IllegalArgumentException iae) {}
            }
        }
        if (e instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)e;
            String horseDomestication = getValue(lore, "Domestication");
            if (!horse.isTamed() && horseDomestication != null) {
                String tokens[] = horseDomestication.split("/", 2);
                if (tokens.length == 2) {
                    try {
                        int domestication = Integer.parseInt(tokens[0]);
                        int maxDomestication = Integer.parseInt(tokens[1]);
                        horse.setDomestication(domestication);
                        horse.setMaxDomestication(maxDomestication);
                    } catch (NumberFormatException nfe) {}
                }
            }
            String horseJumpStrength = getValue(lore, "Jump Strength");
            if (horseJumpStrength != null) {
                try {
                    double jumpStrength = Double.parseDouble(horseJumpStrength);
                    horse.setJumpStrength(jumpStrength);
                } catch (NumberFormatException nfe) {}
            }
            try {
                String horseSpeed = getValue(lore, "Speed");
                if (horseSpeed != null) {
                    double speed = Double.parseDouble(horseSpeed);
                    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
                }
            } catch (NumberFormatException nfe) {
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (e instanceof ChestedHorse) {
            ChestedHorse horse = (ChestedHorse)e;
            String horseCarriesChest = getValue(lore, "Carries Chest");
            if (horseCarriesChest != null && horseCarriesChest.equalsIgnoreCase("true")) {
                horse.setCarryingChest(true);
            } else {
                horse.setCarryingChest(false);
            }
        }
        if (e instanceof Llama) {
            Llama llama = (Llama)e; // llama llama ding dong
            String llamaColor = getValue(lore, "Llama Color");
            if (llamaColor != null) {
                try {
                    Llama.Color color = Llama.Color.valueOf(humanToEnum(llamaColor));
                    llama.setColor(color);
                } catch (IllegalArgumentException ase) {}
            }
        }
        if (e instanceof Zombie) {
            Zombie zombie = (Zombie)e;
            String age = getValue(lore, "Age");
            if (age != null && age.equals("Baby")) {
                zombie.setBaby(true);
            } else {
                zombie.setBaby(false);
            }
        }
        if (e instanceof Creeper) {
            Creeper creeper = (Creeper)e;
            String powered = getValue(lore, "Powered");
            if (powered != null && powered.equalsIgnoreCase("true")) {
                creeper.setPowered(true);
            } else {
                creeper.setPowered(false);
            }
        }
        if (e instanceof Slime) {
            Slime slime = (Slime)e;
            String size = getValue(lore, "Size");
            if (size != null) {
                try {
                    slime.setSize(Integer.parseInt(size));
                } catch (NumberFormatException nfe) {}
            }
        }
        if (e instanceof Enderman) {
            String hand = getValue(lore, "Hand");
            if (hand != null) {
                String tokens[] = hand.split(":", 2);
                Material mat = null;
                byte data = (byte)0;
                try {
                    mat = Material.valueOf(humanToEnum(tokens[0]));
                } catch (IllegalArgumentException iae) {}
                if (mat != null && mat != Material.AIR) {
                    if (tokens.length == 2) {
                        try {
                            data = (byte)Byte.parseByte(tokens[1]);
                        } catch (NumberFormatException nfe) {}
                    }
                    MaterialData md = new MaterialData(mat, data);
                    Enderman enderman = (Enderman)e;
                    enderman.setCarriedMaterial(md);
                }
            }
        }
        if (e instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)e;
            String rabbitArg = getValue(lore, "Rabbit Type");
            if (rabbitArg != null) {
                try {
                    Rabbit.Type rabbitType = Rabbit.Type.valueOf(humanToEnum(rabbitArg));
                    rabbit.setRabbitType(rabbitType);
                } catch (IllegalArgumentException iae) {}
            }
        }
        String fire = getValue(lore, "Fire");
        if (fire != null) {
            try {
                e.setFireTicks(Integer.parseInt(fire));
            } catch (NumberFormatException nfe) {}
        }
        {
            LivingEntity living = (LivingEntity)e;
            EntityEquipment equip = living.getEquipment();
            equip.clear();
            equip.setItemInHand(loadEquippedItem(lore, "Hand"));
            equip.setHelmet(loadEquippedItem(lore, "Helmet"));
            equip.setChestplate(loadEquippedItem(lore, "Chestplate"));
            equip.setLeggings(loadEquippedItem(lore, "Leggings"));
            equip.setBoots(loadEquippedItem(lore, "Boots"));
        }
    }
}
