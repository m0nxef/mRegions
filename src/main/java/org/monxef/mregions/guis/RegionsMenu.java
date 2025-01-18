package org.monxef.mregions.guis;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.models.Region;

import java.util.ArrayList;
import java.util.List;

public class RegionsMenu extends FastInv {
    private final RegionPlugin plugin;
    private final int[] REGION_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public RegionsMenu(RegionPlugin plugin) {
        super(45, "§8Regions");
        this.plugin = plugin;
        
        // Fill border with glass panes
        for (int i = 0; i < 9; i++) {
            setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, "§r"));
            setItem(i + 36, createItem(Material.BLACK_STAINED_GLASS_PANE, "§r"));
        }
        for (int i = 0; i < 45; i += 9) {
            if (i != 0 && i != 36) {
                setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, "§r"));
                setItem(i + 8, createItem(Material.BLACK_STAINED_GLASS_PANE, "§r"));
            }
        }

        // Load and display regions
        loadRegions();
    }

    private void loadRegions() {
        List<Region> regions = plugin.getRegionManager().getRegions();
        if (regions.isEmpty()) {
            setItem(22, new ItemBuilder(Material.BARRIER)
                    .name("§c§lNo Regions Available")
                    .lore("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                          "§7You haven't created any regions yet!",
                          "",
                          "§e§lHow to create a region:",
                          "§7 1. Get the region wand §8(§6/region wand§8)",
                          "§7 2. Select two corners with §eLeft §7and §eRight §7click",
                          "§7 3. Create your region §8(§6/region create <name>§8)",
                          "",
                          "§e§lTIP: §7Regions help you protect your builds",
                          "§7and manage who can interact with them!",
                          "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                    .build());
            return;
        }
        int slot = 0;
        for (Region region : regions) {
            if (slot >= REGION_SLOTS.length) break;

            int finalSlot = REGION_SLOTS[slot++];
            setItem(finalSlot, createRegionItem(region), e -> {
                Player player = (Player) e.getWhoClicked();
                player.closeInventory();
                plugin.getMenuManager().openRegionMenu(player, region);
            });
        }
    }

    private ItemStack createRegionItem(Region region) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§b" + region.getName());

        List<String> lore = new ArrayList<>();
        lore.add("§7Owner: §f" + plugin.getServer().getOfflinePlayer(region.getOwner()).getName());
        lore.add("§7World: §f" + region.getWorld().getName());
        lore.add("§7Position 1: §f" + formatLocation(region.getPos1()));
        lore.add("§7Position 2: §f" + formatLocation(region.getPos2()));
        lore.add("");
        lore.add("§eClick to manage this region");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("X: %d, Y: %d, Z: %d", 
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }

        item.setItemMeta(meta);
        return item;
    }
}
