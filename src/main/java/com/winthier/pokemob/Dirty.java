package com.winthier.pokemob;

import java.lang.reflect.Field;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.DataConverterSpawnEgg;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;

public class Dirty {
    public static ItemStack spawnEggOf(int data) {
        ItemStack result = new org.bukkit.inventory.ItemStack(Material.MONSTER_EGG, 1);
        try {

            // fetch name
            Field idArray = DataConverterSpawnEgg.class.getDeclaredField("a");
            idArray.setAccessible(true);
            String[] ids = (String[]) idArray.get(null);
            String name = ids[data & 255];
            if (data == 102 && name == null) name = "PolarBear";
            if (name == null) return null;
            
            net.minecraft.server.v1_10_R1.ItemStack item = CraftItemStack.asNMSCopy(result);
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

    public static ItemStack spawnEggOf(EntityType et) {
        return spawnEggOf(et.getTypeId());
    }

    public static int getSpawnEggData(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_10_R1.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
            if(!item.hasTag())
                item.setTag(new NBTTagCompound());
            if(!item.getTag().hasKeyOfType("EntityTag", 10))
                item.getTag().set("EntityTag", new NBTTagCompound());
            String name = item.getTag().getCompound("EntityTag").getString("id");

            if (name.equals("PolarBear")) return 102;

            // fetch name
            Field idArray = DataConverterSpawnEgg.class.getDeclaredField("a");
            idArray.setAccessible(true);
            String[] ids = (String[]) idArray.get(null);
            for (int i = 0; i < ids.length; ++i) {
                if (ids[i] != null && ids[i].equals(name)) return i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static EntityType getSpawnEggType(ItemStack itemStack) {
        return EntityType.fromId(getSpawnEggData(itemStack));
    }

    public static Double getHorseSpeed(Horse h){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        return attributes.getValue();
    }

    public static void setHorseSpeed(Horse h,double speed){
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity)h).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        attributes.setValue(speed);
    }
}
