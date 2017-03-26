package com.winthier.pokemob;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockContext;
import com.winthier.custom.block.CustomBlock;
import com.winthier.custom.block.UnbreakableBlock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Getter @RequiredArgsConstructor
public class PokeBallBlock implements CustomBlock, UnbreakableBlock {
    private final PokeMobPlugin plugin;
    private final String customId = "pokemob:pokeball";

    @Override
    public void setBlock(Block block) {
        block.setType(Material.SKULL);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, BlockContext context) {
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event, BlockContext context) {
        event.setInstaBreak(true);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event, BlockContext context) {
        event.setCancelled(true);
        CustomPlugin.getInstance().getBlockManager().removeBlockWatcher(context.getBlockWatcher());
        context.getBlock().setType(Material.AIR);
        CustomPlugin.getInstance().getItemManager().dropItemStack(context.getBlock().getLocation().add(0.5, 0.5, 0.5), customId, 1);
    }
}
