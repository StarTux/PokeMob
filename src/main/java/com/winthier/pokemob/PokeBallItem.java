package com.winthier.pokemob;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.generic_events.ItemNameEvent;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

@Getter
public class PokeBallItem implements CustomItem, UncraftableItem {
    private final PokeMobPlugin plugin;
    private final String customId = "pokemob:pokeball";
    private final String displayName;
    private final ItemStack itemStack;
    private final ItemDescription itemDescription;

    PokeBallItem(PokeMobPlugin plugin) {
        this.plugin = plugin;
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        this.displayName = plugin.getConfig().getString("PokeBallItem.DisplayName");
        UUID uuid = UUID.fromString(plugin.getConfig().getString("PokeBallItem.Id"));
        String texture = plugin.getConfig().getString("PokeBallItem.Texture");
        item = com.winthier.custom.util.Dirty.setSkullOwner(item, displayName, uuid, texture);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Msg.format("&r%s", displayName));
        item.setItemMeta(meta);
        ItemDescription desc = new ItemDescription();
        desc.setCategory(plugin.getConfig().getString("PokeBallItem.Category"));
        desc.setDescription(plugin.getConfig().getString("PokeBallItem.Description"));
        desc.setUsage(plugin.getConfig().getString("PokeBallItem.Usage"));
        desc.apply(item);
        this.itemStack = item;
        this.itemDescription = desc;
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event, ItemContext context) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event, ItemContext context) {
        switch (event.getAction()) {
        case RIGHT_CLICK_BLOCK:
        case RIGHT_CLICK_AIR:
            event.setCancelled(true);
            Player player = event.getPlayer();
            ItemStack item = context.getItemStack();
            if (player.getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);
            throwPotion(player);
            break;
        default:
            break;
        }
    }

    public void onPlayerInteractEntity(PlayerInteractEvent event, ItemContext context) {
        event.setCancelled(true);
        Player player = context.getPlayer();
        ItemStack item = context.getItemStack();
        if (player.getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);
        throwPotion(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event, ItemContext context) {
        event.setCancelled(true);
        final Block block = event.getBlock();
        final Location loc = event.getVelocity().toLocation(block.getWorld()).add(0.5, 0.5, 0.5);
        final BlockFace fac = ((org.bukkit.material.Dispenser)block.getState().getData()).getFacing();
        final Vector velo = new Vector(fac.getModX(), fac.getModY(), fac.getModZ()).multiply(1.5);
        new BukkitRunnable() {
            @Override public void run() {
                if (block.getType() != Material.DISPENSER) return;
                org.bukkit.block.Dispenser state = (org.bukkit.block.Dispenser)block.getState();
                Inventory inv = state.getInventory();
                boolean located = false;
                for (int i = 0; i < inv.getSize(); ++i) {
                    ItemStack item = inv.getItem(i);
                    if (item == null || item.getType() == Material.AIR) continue;
                    CustomItem customItem = CustomPlugin.getInstance().getItemManager().getCustomItem(item);
                    if (customItem != PokeBallItem.this) continue;
                    item.setAmount(item.getAmount() - 1);
                    located = true;
                    break;
                }
                if (!located) return;
                state.update();
                SplashPotion potion = loc.getWorld().spawn(loc, SplashPotion.class, new Consumer<SplashPotion>() {
                    @Override public void accept(SplashPotion pot) {
                        ItemStack item = pot.getItem();
                        PotionMeta meta = (PotionMeta)item.getItemMeta();
                        meta.setBasePotionData(new PotionData(PotionType.SLOWNESS));
                        item.setItemMeta(meta);
                        pot.setItem(item);
                        pot.setVelocity(velo);
                    }
                });
                loc.getWorld().playSound(loc, Sound.BLOCK_DISPENSER_LAUNCH, 0.2f, 1.0f);
            }
        }.runTask(plugin);
    }

    void throwPotion(Player player) {
        SplashPotion potion = player.launchProjectile(SplashPotion.class);
        ItemStack item = potion.getItem();
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.SLOWNESS));
        item.setItemMeta(meta);
        potion.setItem(item);
    }

    @EventHandler
    public void onItemName(ItemNameEvent event, ItemContext context) {
        event.setItemName(displayName);
    }
}
