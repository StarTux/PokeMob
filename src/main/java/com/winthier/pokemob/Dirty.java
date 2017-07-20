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

import net.minecraft.server.v1_12_R1.AttributeInstance;
import net.minecraft.server.v1_12_R1.DataConverterSpawnEgg;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

public class Dirty {
    // Cats (and possibly others) do not have a "Tame" tag.  They
    // either have an owner or don't.  Therefore, we may not
    // delete OwnerUUID.
    final static List<String> REMOVE_NBT_TAGS = Arrays.asList(
                                                              "Pos", "Rotation", "Motion", "FallDistance", "OnGround", "Dimension", "PortalCooldown", "UUIDMost", "UUIDLeast", "UUID", "Passengers", "HurtByTimestamp", "WorldUUIDLeast", "WorldUUIDMost", "Spigot.ticksLived", "Bukkit.updateLevel", "Leashed", "Leash", "APX", "APY", "APZ"
        );
    public static ItemStack eggify(org.bukkit.entity.Entity e) {
        ItemStack result = new org.bukkit.inventory.ItemStack(Material.MONSTER_EGG, 1);
        try {
            net.minecraft.server.v1_12_R1.ItemStack item = CraftItemStack.asNMSCopy(result);
            if (!item.hasTag()) item.setTag(new NBTTagCompound());
            if (!item.getTag().hasKeyOfType("EntityTag", 10)) item.getTag().set("EntityTag", new NBTTagCompound());
            NBTTagCompound tag = item.getTag().getCompound("EntityTag");
            net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity)e).getHandle();
            nmsEntity.c(tag);
            // 8 = String
            if (!tag.hasKeyOfType("id", 8)) throw new IllegalArgumentException("Entity did not cooperate to be eggified: " + e);
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
            return null;
        }
        return result;
    }

    public static String getSpawnEggDataTag(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_12_R1.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
            if(!item.hasTag()) return null;
            if(!item.getTag().hasKeyOfType("EntityTag", 10)) return null;
            NBTTagCompound compound = item.getTag().getCompound("EntityTag");
            int size = compound.d();
            if (size < 2) return null;
            for (String name: REMOVE_NBT_TAGS) compound.remove(name);
            return compound.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
