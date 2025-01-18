package org.monxef.mregions.guis;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.models.Region;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MenuManager {
    private final RegionPlugin plugin;
    private final Map<Inventory, Menu> activeMenus = new HashMap<>();

    public void registerMenu(Inventory inventory, Menu menu) {
        activeMenus.put(inventory, menu);
    }

    public void unregisterMenu(Inventory inventory) {
        activeMenus.remove(inventory);
    }


    public void openRegionsMenu(Player player) {
        new RegionsMenu(plugin).open(player);
    }

    public void openRegionMenu(Player player, Region region) {
        new RegionMenu(plugin, region).open(player);
    }

    public void openFlagsMenu(Player player, Region region) {
        new FlagsMenu(plugin, region).open(player);
    }

    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r" + name);

        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add("§r" + line);
            }
            meta.setLore(loreList);
        }

        item.setItemMeta(meta);
        return item;
    }
}