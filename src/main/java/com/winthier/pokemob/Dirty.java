package com.winthier.pokemob;

import java.lang.reflect.Field;
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
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;

public class Dirty {
    static String minecraftNameOf(EntityType et) {
        switch (et) {
        case VINDICATOR: return "minecraft:vindication_illager";
        case EVOKER: return "minecraft:evocation_illager";
        case PIG_ZOMBIE: return "minecraft:zombie_pigman";
        default:
            return "minecraft:" + et.name().toLowerCase();
        }
    }

    public static ItemStack spawnEggOf(EntityType et) {
        ItemStack result = new org.bukkit.inventory.ItemStack(Material.MONSTER_EGG, 1);
        String name = minecraftNameOf(et);
        try {
            net.minecraft.server.v1_11_R1.ItemStack item = CraftItemStack.asNMSCopy(result);
            if(!item.hasTag())
                item.setTag(new NBTTagCompound());
            if(!item.getTag().hasKeyOfType("EntityTag", 10))
                item.getTag().set("EntityTag", new NBTTagCompound());
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
                String val = name.substring(10);
                if ("vindication_illager".equals(val)) return EntityType.VINDICATOR;
                if ("evocation_illager".equals(val)) return EntityType.EVOKER;
                if ("zombie_pigman".equals(val)) return EntityType.PIG_ZOMBIE;
                try {
                    return EntityType.valueOf(val.toUpperCase());
                } catch (IllegalArgumentException iae) {
                    System.err.println("PokeMob: Cannot find EntityType for '" + name + "'");
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

    public static Double getHorseSpeed(AbstractHorse h){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        return attributes.getValue();
    }

    public static void setHorseSpeed(AbstractHorse h, double speed){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        attributes.setValue(speed);
    }
}
