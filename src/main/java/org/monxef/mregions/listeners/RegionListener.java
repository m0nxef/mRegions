package org.monxef.mregions.listeners;


import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.flags.DefaultFlags;
import org.monxef.mregions.models.Region;

import java.util.List;

@RequiredArgsConstructor
public class RegionListener implements Listener {
    private final RegionPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        List<Region> regions = plugin.getRegionManager().getRegionsAt(event.getBlock().getLocation());

        for (Region region : regions) {
            DefaultFlags.BlockBreakFlag flag =
                    (DefaultFlags.BlockBreakFlag) plugin.getFlagManager().getFlag("block_break").orElse(null);

            if (flag != null && !region.isAllowed(player, flag)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "§cYou don't have permission to break blocks in this region!");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        List<Region> regions = plugin.getRegionManager().getRegionsAt(event.getBlock().getLocation());

        for (Region region : regions) {
            DefaultFlags.BlockPlaceFlag flag =
                    (DefaultFlags.BlockPlaceFlag) plugin.getFlagManager().getFlag("block_place").orElse(null);

            if (flag != null && !region.isAllowed(player, flag)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "§cYou don't have permission to place blocks in this region!");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        List<Region> regions = plugin.getRegionManager().getRegionsAt(event.getClickedBlock().getLocation());

        for (Region region : regions) {
            DefaultFlags.InteractFlag flag =
                    (DefaultFlags.InteractFlag) plugin.getFlagManager().getFlag("interact").orElse(null);

            if (flag != null && !region.isAllowed(player, flag)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "§cYou don't have permission to interact in this region!");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        Entity victim = event.getEntity();
        List<Region> regions = plugin.getRegionManager().getRegionsAt(victim.getLocation());

        for (Region region : regions) {
            DefaultFlags.EntityDamageFlag flag =
                    (DefaultFlags.EntityDamageFlag) plugin.getFlagManager().getFlag("entity_damage").orElse(null);

            if (flag != null && !region.isAllowed(player, flag)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "§cYou don't have permission to damage entities in this region!");
                return;
            }
        }
    }
}