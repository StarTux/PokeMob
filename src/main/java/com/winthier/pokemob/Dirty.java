package com.winthier.pokemob;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_11_R1.AttributeInstance;
import net.minecraft.server.v1_11_R1.DataConverterSpawnEgg;
import net.minecraft.server.v1_11_R1.EntityInsentient;
import net.minecraft.server.v1_11_R1.GenericAttributes;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;

public class Dirty {
    final static List<String> REMOVE_NBT_TAGS = Arrays.asList(
        "Pos", "Rotation", "Motion", "FallDistance", "OnGround", "Dimension", "PortalCooldown", "UUIDMost", "UUIDLeast", "UUID", "Passengers", "HurtByTimestamp", "WorldUUIDLeast", "WorldUUIDMost", "Spigot.ticksLived", "Bukkit.updateLevel", "OwnerUUID"
        );
    public static ItemStack eggify(org.bukkit.entity.Entity e) {
        ItemStack result = new org.bukkit.inventory.ItemStack(Material.MONSTER_EGG, 1);
        try {
            net.minecraft.server.v1_11_R1.ItemStack item = CraftItemStack.asNMSCopy(result);
            if (!item.hasTag()) item.setTag(new NBTTagCompound());
            if (!item.getTag().hasKeyOfType("EntityTag", 10)) item.getTag().set("EntityTag", new NBTTagCompound());
            NBTTagCompound tag = item.getTag().getCompound("EntityTag");
            net.minecraft.server.v1_11_R1.Entity nmsEntity = ((CraftEntity)e).getHandle();
            nmsEntity.c(tag);
            // Clean her up a bit
            for (String name: REMOVE_NBT_TAGS) tag.remove(name);
            NBTTagList tagList = tag.getList("Attributes", 10);
            if (tagList != null) {
                for (int i = 0; i < tagList.size(); ) {
                    NBTTagCompound tagAttr = tagList.get(i);
                    if ("generic.followRange".equals(tagAttr.getString("Name"))) {
                        tagList.remove(i);
                    } else {
                        i += 1;
                    }
                }
            }
            result = CraftItemStack.asBukkitCopy(item);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    static String minecraftNameOf(EntityType et) {
        return "minecraft:" + et.getName();
    }

    public static ItemStack spawnEggOf(EntityType et) {
        ItemStack result = new org.bukkit.inventory.ItemStack(Material.MONSTER_EGG, 1);
        String name = minecraftNameOf(et);
        try {
            net.minecraft.server.v1_11_R1.ItemStack item = CraftItemStack.asNMSCopy(result);
            if (!item.hasTag()) {
                item.setTag(new NBTTagCompound());
            }
            if (!item.getTag().hasKeyOfType("EntityTag", 10)) {
                item.getTag().set("EntityTag", new NBTTagCompound());
            }
            item.getTag().getCompound("EntityTag").setString("id", name);
            result = CraftItemStack.asBukkitCopy(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static EntityType getSpawnEggType(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_11_R1.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
            if(!item.hasTag())
                item.setTag(new NBTTagCompound());
            if(!item.getTag().hasKeyOfType("EntityTag", 10))
                item.getTag().set("EntityTag", new NBTTagCompound());
            String name = item.getTag().getCompound("EntityTag").getString("id");
            if (name.startsWith("minecraft:")) {
                try {
                    return EntityType.fromName(name.substring(10));
                } catch (IllegalArgumentException iae) {
                    PokeMobPlugin.getInstance().getLogger().warning("Cannot find EntityType for '" + name + "'");
                }
            }
            // Fetch Legacy Name
            if (name.equals("PolarBear")) return EntityType.POLAR_BEAR;
            Field idArray = DataConverterSpawnEgg.class.getDeclaredField("a");
            idArray.setAccessible(true);
            String[] ids = (String[]) idArray.get(null);
            for (int i = 0; i < ids.length; ++i) {
                if (ids[i] != null && ids[i].equals(name)) return EntityType.fromId(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSpawnEggDataTag(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_11_R1.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
            if(!item.hasTag()) return null;
            if(!item.getTag().hasKeyOfType("EntityTag", 10)) return null;
            NBTTagCompound compound = item.getTag().getCompound("EntityTag");
            int size = compound.d();
            if (size < 2) return null;
            return compound.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Double getHorseSpeed(AbstractHorse h){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        return attributes.getValue();
    }

    public static void setHorseSpeed(AbstractHorse h, double speed){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        attributes.setValue(speed);
    }
}
