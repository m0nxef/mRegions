package org.monxef.mregions.listeners;

import lombok.RequiredArgsConstructor;
import lombok.var;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.managers.RegionManager;

@RequiredArgsConstructor
public class WandListener implements Listener {
    private final RegionPlugin plugin;
    private static final Material WAND_MATERIAL = Material.GOLDEN_AXE;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if player is using the wand
        if (item == null || item.getType() != WAND_MATERIAL) {
            return;
        }

        // Check if player has permission
        if (!player.hasPermission("region.selection")) {
            player.sendMessage("§cYou don't have permission to use the region wand!");
            return;
        }

        // Cancel the event to prevent block breaking/interaction
        event.setCancelled(true);

        // Get the clicked block
        if (event.getClickedBlock() == null) {
            return;
        }

        RegionManager.Selection currentSelection = plugin.getRegionManager().getSelection(player);
        if (currentSelection == null) {
            currentSelection = new RegionManager.Selection(null, null);
        }

        // Left click for position 1, right click for position 2
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            currentSelection = new RegionManager.Selection(
                event.getClickedBlock().getLocation(),
                currentSelection.getPos2()
            );
            player.sendMessage("§aPosition 1 set to: " + formatLocation(event.getClickedBlock().getLocation()));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            currentSelection = new RegionManager.Selection(
                currentSelection.getPos1(),
                event.getClickedBlock().getLocation()
            );
            player.sendMessage("§aPosition 2 set to: " + formatLocation(event.getClickedBlock().getLocation()));
        }

        // Update the selection
        plugin.getRegionManager().setSelection(player, currentSelection);

        // Show selection info if both positions are set
        if (currentSelection.getPos1() != null && currentSelection.getPos2() != null) {
            showSelectionInfo(player, currentSelection);
        }
    }

    private void showSelectionInfo(Player player, RegionManager.Selection selection) {
        int xSize = Math.abs(selection.getPos1().getBlockX() - selection.getPos2().getBlockX()) + 1;
        int ySize = Math.abs(selection.getPos1().getBlockY() - selection.getPos2().getBlockY()) + 1;
        int zSize = Math.abs(selection.getPos1().getBlockZ() - selection.getPos2().getBlockZ()) + 1;
        int totalBlocks = xSize * ySize * zSize;

        player.sendMessage("§6Selection Info:");
        player.sendMessage("§7Size: §f" + xSize + "x" + ySize + "x" + zSize);
        player.sendMessage("§7Total Blocks: §f" + totalBlocks);
        player.sendMessage("§7World: §f" + selection.getPos1().getWorld().getName());
        player.sendMessage("§eUse §6/region create <name> §eto create a region");
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("X: %d, Y: %d, Z: %d", 
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§6§lRegion Wand");
        meta.setLore(java.util.Arrays.asList(
            "§7Left-click to set position 1",
            "§7Right-click to set position 2"
        ));
        wand.setItemMeta(meta);
        return wand;
    }
}
