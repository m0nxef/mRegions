package org.monxef.mregions.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.monxef.mregions.RegionPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class RegionEnterListener implements Listener {
    private static final int FADE_IN = 10;
    private static final int STAY = 40;
    private static final int FADE_OUT = 10;
    private static final Sound ENTER_SOUND = Sound.BLOCK_NOTE_BLOCK_PLING;
    private static final Sound EXIT_SOUND = Sound.BLOCK_NOTE_BLOCK_BASS;
    private static final float ENTER_PITCH = 1.2f;
    private static final float EXIT_PITCH = 0.8f;
    
    private final RegionPlugin plugin;
    private final Map<UUID, Set<String>> playerRegions = new ConcurrentHashMap<>();
    private final boolean showTitles;
    private final String enterTitleMain;
    private final String enterTitleSecondary;
    private final String exitTitleMain;
    private final String exitTitleSecondary;

    public RegionEnterListener(RegionPlugin plugin) {
        this.plugin = plugin;
        this.showTitles = plugin.getConfig().getBoolean("show-region-titles", true);
        
        // Load and color-code messages
        this.enterTitleMain = colorize(plugin.getConfig().getString("region-entrance.enter.main", 
                "&8[&a⚑&8] &b&l%name%"));
        this.enterTitleSecondary = colorize(plugin.getConfig().getString("region-entrance.enter.secondary", 
                "&7You have entered this region"));
        this.exitTitleMain = colorize(plugin.getConfig().getString("region-entrance.exit.main", 
                "&8[&c⚐&8] &b&l%name%"));
        this.exitTitleSecondary = colorize(plugin.getConfig().getString("region-entrance.exit.secondary", 
                "&7You have left this region"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!shouldProcessMovement(event)) {
            return;
        }

        Player player = event.getPlayer();
        processRegionChanges(player, event.getTo());
    }

    private boolean shouldProcessMovement(PlayerMoveEvent event) {
        if (!showTitles || event.getTo() == null) {
            return false;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        return from.getBlockX() != to.getBlockX() 
            || from.getBlockY() != to.getBlockY() 
            || from.getBlockZ() != to.getBlockZ();
    }

    private void processRegionChanges(Player player, Location to) {
        // Get current and new regions
        Set<String> currentRegions = playerRegions.computeIfAbsent(
            player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        Set<String> newRegions = getRegionsAt(to);

        // Process region changes
        processEnteredRegions(player, newRegions, currentRegions);
        processExitedRegions(player, newRegions, currentRegions);

        // Update stored regions
        playerRegions.put(player.getUniqueId(), newRegions);
    }

    private Set<String> getRegionsAt(Location location) {
        Set<String> regions = ConcurrentHashMap.newKeySet();
        plugin.getRegionManager().getRegionsAt(location)
            .forEach(region -> regions.add(region.getName()));
        return regions;
    }

    private void processEnteredRegions(Player player, Set<String> newRegions, Set<String> currentRegions) {
        Set<String> entered = new HashSet<>(newRegions);
        entered.removeAll(currentRegions);
        
        for (String region : entered) {
            showTitle(player, enterTitleMain, enterTitleSecondary, region);
            player.playSound(player.getLocation(), ENTER_SOUND, 1.0f, ENTER_PITCH);
        }
    }

    private void processExitedRegions(Player player, Set<String> newRegions, Set<String> currentRegions) {
        Set<String> exited = new HashSet<>(currentRegions);
        exited.removeAll(newRegions);
        
        for (String region : exited) {
            showTitle(player, exitTitleMain, exitTitleSecondary, region);
            player.playSound(player.getLocation(), EXIT_SOUND, 1.0f, EXIT_PITCH);
        }
    }

    private void showTitle(Player player, String main, String secondary, String region) {
        player.sendTitle(
            main.replace("%name%", region),
            secondary,
            FADE_IN, STAY, FADE_OUT
        );
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
