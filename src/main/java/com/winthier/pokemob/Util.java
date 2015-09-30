package com.winthier.pokemob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
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

import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;

public class Util {
    private static Random rnd = new Random(System.currentTimeMillis());

    public static void useSpawnEgg(ItemStack item, LivingEntity e) {
        loadMetaData(e, item);
        e.getWorld().playEffect(e.getLocation(), Effect.CLICK2, 0);
    }

    public static void eggify(LivingEntity e) {
        if (!canEggify(e)) return;
        ItemStack stack = getMonsterEgg(e);
        if (stack == null) return;
        Collection<ItemStack> drops = saveMetaData(e, stack);
        Location loc = e.getLocation();
        e.getWorld().dropItemNaturally(loc, stack);
        for (ItemStack drop : drops) e.getWorld().dropItemNaturally(loc, drop);
        e.getWorld().playEffect(loc, Effect.ZOMBIE_CHEW_IRON_DOOR, 0);
        e.getEquipment().clear();
        if (e instanceof Horse) ((Horse)e).getInventory().clear();
        e.remove();
    }

    public static boolean isPokeMob(LivingEntity e) {
        String customName = e.getCustomName();
        return customName != null && customName.charAt(0) == ChatColor.COLOR_CHAR && e.isCustomNameVisible();
    }

    public static void eggifyFail(LivingEntity e) {
        Location loc = e.getLocation().add(0.0, e.getEyeHeight(), 0.0);
        e.getWorld().playEffect(loc, Effect.EXTINGUISH, 0);
        e.getWorld().playEffect(loc, Effect.SMOKE, 0);
    }

    public static ItemStack getMonsterEgg(Entity e) {
        ItemStack result = new ItemStack(Material.MONSTER_EGG, 1, e.getType().getTypeId());
        return result;
    }

    /**
     * @return A collection of dropped items or null
     */
    public static Collection<ItemStack> saveMetaData(LivingEntity e, ItemStack stack) {
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
        Collection<ItemStack> drops = saveMetaData(e, data);
        for (String key : data.keySet()) {
            setValue(lore, key, data.get(key));
        }
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return drops;
    }

    /**
     * @return drops
     */
    public static Collection<ItemStack> saveMetaData(LivingEntity e, Map<String, String> lore) {
        Collection<ItemStack> drops = new ArrayList<ItemStack>(5);
        if (e instanceof Damageable) {
            Damageable damageable = (Damageable)e;
            int health = (int)damageable.getHealth();
            int maxHealth = (int)damageable.getMaxHealth();
            // damageable.resetMaxHealth();
            // if (maxHealth != damageable.getMaxHealth()) {
            // } else if (health != maxHealth) {
            //         setValue(lore, "Health", "" + health);
            // }
            // damageable.setMaxHealth(maxHealth);
            // damageable.setHealth(health);
            setValue(lore, "Health", "" + health + "/" + maxHealth);
        }
        if (e instanceof Ageable) {
            Ageable ageable = (Ageable)e;
            if (!ageable.isAdult()) {
                setValue(lore, "Age", "Baby");
            }
        }
        if (e instanceof Tameable) {
            Tameable tameable = (Tameable)e;
            if (tameable.isTamed()) {
                setValue(lore, "Tamed", "True");
            }
        }
        if (e instanceof Colorable) {
            Colorable colorable = (Colorable)e;
            setValue(lore, "Color", enumToHuman(colorable.getColor().name()));
        }
        if (e instanceof Sheep) {
            Sheep sheep = (Sheep)e;
            if (sheep.isSheared()) {
                setValue(lore, "Sheared", "True");
            }
        }
        if (e instanceof Pig) {
            Pig pig = (Pig)e;
            if (pig.hasSaddle()) {
                setValue(lore, "Saddled", "True");
            }
        }
        if (e instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)e;
            if (ocelot.getCatType() != Ocelot.Type.WILD_OCELOT) {
                setValue(lore, "Cat Type", enumToHuman(ocelot.getCatType().name()));
            }
        }
        if (e instanceof Wolf) {
            Wolf wolf = (Wolf)e;
            // if (wolf.isAngry()) {
            //         setValue(lore, "Angry", "True");
            // }
            setValue(lore, "Collar Color", enumToHuman(wolf.getCollarColor().name()));
        }
        if (e instanceof Villager) {
            Villager villager = (Villager)e;
            setValue(lore, "Profession", enumToHuman(villager.getProfession().name()));
        }
        if (e instanceof Horse) {
            Horse horse = (Horse)e;
            Horse.Variant variant = horse.getVariant();
            setValue(lore, "Horse Variant", enumToHuman(variant.name()));
            if (variant == Horse.Variant.HORSE) {
                Horse.Style style = horse.getStyle();
                setValue(lore, "Horse Style", enumToHuman(style.name()));
                Horse.Color color = horse.getColor();
                setValue(lore, "Horse Color", enumToHuman(color.name()));
            }
            if (!horse.isTamed()) {
                int domestication = horse.getDomestication();
                int maxDomestication = horse.getMaxDomestication();
                setValue(lore, "Domestication", "" + domestication + "/" + maxDomestication);
            }
            String horseJumpStrength = "" + horse.getJumpStrength();
            if (horseJumpStrength.length() > 4) {
                horseJumpStrength = horseJumpStrength.substring(0, 4);
            }
            setValue(lore, "Jump Strength", horseJumpStrength);
            try {
                Double speed = getHorseSpeed(horse);
                if (speed != null) setValue(lore, "Speed", String.format("%.2f", speed));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (horse.isCarryingChest()) {
                setValue(lore, "Carries Chest", "True");
            }
            for (ItemStack item : horse.getInventory()) {
                if (item != null) {
                    drops.add(item.clone());
                }
            }
        }
        if (e instanceof Skeleton) {
            Skeleton skelly = (Skeleton)e;
            SkeletonType st = skelly.getSkeletonType();
            if (st == SkeletonType.WITHER) {
                setValue(lore, "Skeleton Type", enumToHuman(st.name()));
            }
        }
        if (e instanceof Zombie) {
            Zombie zombie = (Zombie)e;
            if (zombie.isBaby()) {
                setValue(lore, "Age", "Baby");
            }
            if (zombie.isVillager()) {
                setValue(lore, "Zombie Type", "Villager");
            }
        }
        if (e instanceof Creeper) {
            Creeper creeper = (Creeper)e;
            if (creeper.isPowered()) {
                setValue(lore, "Powered", "True");
            }
        }
        if (e instanceof Slime) {
            Slime slime = (Slime)e;
            setValue(lore, "Size", "" + slime.getSize());
        }
        if (e instanceof Enderman) {
            Enderman enderman = (Enderman)e;
            MaterialData mat = enderman.getCarriedMaterial();
            if (mat.getItemType() != Material.AIR) {
                String hand = enumToHuman(mat.getItemType().name());
                if (mat.getData() != (byte)0) {
                    hand = hand + ":" + (int)mat.getData();
                }
                setValue(lore, "Hand", hand);
            }
        }
        if (e instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)e;
            setValue(lore, "Rabbit Type", enumToHuman(rabbit.getRabbitType().name()));
        }
        if (e.getFireTicks() > 0) {
            setValue(lore, "Fire", "" + e.getFireTicks());
        }
        {
            EntityEquipment equip = e.getEquipment();
            saveEquippedItem(lore, drops, equip.getItemInHand(), "Hand", equip.getItemInHandDropChance());
            saveEquippedItem(lore, drops, equip.getHelmet(), "Helmet", equip.getHelmetDropChance());
            saveEquippedItem(lore, drops, equip.getChestplate(), "Chestplate", equip.getChestplateDropChance());
            saveEquippedItem(lore, drops, equip.getLeggings(), "Leggings", equip.getLeggingsDropChance());
            saveEquippedItem(lore, drops, equip.getBoots(), "Boots", equip.getBootsDropChance());
        }
        return drops;
    }

    private static Map<String, String> loreToMap(List<String> lore) {
        Map<String, String> result = new HashMap<String, String>();
        for (String i : lore) {
            String j = ChatColor.stripColor(i);
            String tokens[] = j.split(": *", 2);
            if (tokens.length == 2) {
                result.put(tokens[0], tokens[1]);
            }
        }
        return result;
    }

    public static void loadMetaData(LivingEntity e, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        if (meta.hasDisplayName()) {
            e.setCustomName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));
            e.setCustomNameVisible(true);
            e.setRemoveWhenFarAway(false);
            if (e instanceof Ageable) {
                Ageable ageable = (Ageable)e;
                ageable.setBreed(false);
            }
        }
        List<String> lore;
        if (!meta.hasLore()) {
            lore = new ArrayList<String>();
        } else {
            lore = meta.getLore();
        }
        loadMetaData(e, loreToMap(lore));
    }

    public static void loadMetaData(LivingEntity e, Map<String, String> lore) {
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
            String horseVariant = getValue(lore, "Horse Variant");
            if (horseVariant != null) {
                try {
                    Horse.Variant variant = Horse.Variant.valueOf(humanToEnum(horseVariant));
                    horse.setVariant(variant);
                } catch (IllegalArgumentException iae) {}
            } else {
                horse.setVariant(Horse.Variant.HORSE);
            }
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
                    setHorseSpeed(horse, speed);
                }
            } catch (NumberFormatException nfe) {
            } catch (Throwable t) {
                t.printStackTrace();
            }
            String horseCarriesChest = getValue(lore, "Carries Chest");
            if (horseCarriesChest != null && horseCarriesChest.equalsIgnoreCase("true")) {
                horse.setCarryingChest(true);
            } else {
                horse.setCarryingChest(false);
            }
        }
        if (e instanceof Skeleton) {
            Skeleton skelly = (Skeleton)e;
            String skeletonType = getValue(lore, "Skeleton Type");
            if (skeletonType != null) {
                try {
                    SkeletonType st = SkeletonType.valueOf(humanToEnum(skeletonType));
                    skelly.setSkeletonType(st);
                } catch (IllegalArgumentException iae) {}
            } else {
                skelly.setSkeletonType(SkeletonType.NORMAL);
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
            String zt = getValue(lore, "Zombie Type");
            if (zt != null && zt.equals("Villager")) {
                zombie.setVillager(true);
            } else {
                zombie.setVillager(false);
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

    public static boolean isSimpleItem(ItemStack item) {
        if (item.getAmount() != 1) return false;
        if (item.hasItemMeta()) return false;
        return true;
    }

    /**
     * Subroutine for saveMetaData()
     */
    public static void saveEquippedItem(Map<String, String> lore, Collection<ItemStack> drops, ItemStack item, String key, float dropChance) {
        if (item == null || item.getType() == Material.AIR) return;
        if (isSimpleItem(item)) {
            if (item.getDurability() == 0) {
                setValue(lore, key, enumToHuman(item.getType().name()));
            } else {
                setValue(lore, key, enumToHuman(item.getType().name() + ":" + item.getDurability()));
            }
        } else {
            if (rnd.nextFloat() < dropChance) {
                drops.add(item);
            }
        }
    }

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

    public static String firstLetterUp(String s) {
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

    /**
     * @return The value or null if key not found
     */
    private static String getValue(List<String> lore, String key) {
        String search = key + ":";
        for (String i : lore) {
            String j = ChatColor.stripColor(i);
            if (j.startsWith(search)) {
                String tokens[] = j.split(": *", 2);
                if (tokens.length != 2) return "";
                return tokens[1];
            }
        }
        return null;
    }

    private static String getValue(Map<String, String> lore, String key) {
        Object result = lore.get(key);
        if (result == null) return null;
        return result.toString();
    }

    public static void setValue(Map<String, String> lore, String key, String value) {
        lore.put(key, value);
    }

    public static void setValue(List<String> lore, String key, String value) {
        String result = ChatColor.translateAlternateColorCodes('&', "&8" + key + "&8:&7 " + value);
        String search = key + ":";
        for (int i = 0; i < lore.size(); ++i) {
            String j = ChatColor.stripColor(lore.get(i));
            if (j.startsWith(search)) {
                lore.set(i, result);
                return;
            }
        }
        lore.add(result);
        return;
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
        return canEggifyEntity(e.getType().getTypeId());
    }

    public static boolean canEggify(EntityType e) {
        return canEggifyEntity(e.getTypeId());
    }

    public static boolean canEggifyEntity(int id) {
        if (id == 120) return true;
        if (id < 50 || id > 101) return false;
        if (id > 68 && id < 90) return false;
        return true;
    }

    public static boolean canBeVanillaSpawnEgg(int id) {
        if (id == 120) return true;
        if (id < 50 || id > 98) return false;
        if (id > 68 && id < 90) return false;
        switch (id) {
        case 53:
        case 63:
        case 64:
        case 97:
            return false;
        default:
            return true;
        }
    }

    static Double getHorseSpeed(Horse h){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        return attributes.getValue();
    }

    static void setHorseSpeed(Horse h,double speed){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        attributes.setValue(speed);
    }
}
