package org.monxef.mregions.guis;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.models.Region;
import org.monxef.mregions.models.Flag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlagsMenu extends FastInv {
    private final RegionPlugin plugin;
    private final Region region;
    private final int[] FLAG_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    public FlagsMenu(RegionPlugin plugin, Region region) {
        super(36, "§8Flags: " + region.getName());
        this.plugin = plugin;
        this.region = region;

        // Fill border with glass panes
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i > 26 || i % 9 == 0 || i % 9 == 8) {
                setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, "§r"));
            }
        }

        // Back button
        setItem(31, createItem(Material.ARROW, "§c§lBack to Region Menu", 
            "§7Click to return to region menu"), e -> {
            Player player = (Player) e.getWhoClicked();
            player.closeInventory();
            plugin.getMenuManager().openRegionMenu(player, region);
        });

        // Load flags
        loadFlags();
    }

    private void loadFlags() {
        List<Flag> flags = new ArrayList<>(plugin.getFlagManager().getRegisteredFlags().values());

        for (int i = 0; i < flags.size() && i < FLAG_SLOTS.length; i++) {
            Flag flag = flags.get(i);
            final int slot = FLAG_SLOTS[i];
            setFlagItem(slot, flag);
        }
    }

    private void setFlagItem(int slot, Flag flag) {
        ItemStack flagItem = createFlagItem(flag);
        setItem(slot, flagItem, e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.flags.toggle")) {
                player.sendMessage("§cYou don't have permission to toggle flags!");
                return;
            }

            toggleFlag(flag);
            plugin.getDatabaseManager().saveRegion(region);
            setFlagItem(slot, flag);
        });
    }

    private void toggleFlag(Flag flag) {
        Optional<Flag.FlagState> currentState = region.getFlagState(flag);
        Flag.FlagState newState;

        if (!currentState.isPresent()) {
            newState = flag.getDefaultState();
        } else {
            switch (currentState.get()) {
                case EVERYONE:
                    newState = Flag.FlagState.WHITELIST;
                    break;
                case WHITELIST:
                    newState = Flag.FlagState.NONE;
                    break;
                case NONE:
                default:
                    newState = Flag.FlagState.EVERYONE;
                    break;
            }
        }

        region.setFlag(flag, newState);
    }

    private void updateFlagItem(int slot, Flag flag) {
        setItem(slot, createFlagItem(flag));
    }

    private ItemStack createFlagItem(Flag flag) {
        Material material;
        Optional<Flag.FlagState> state = region.getFlagState(flag);
        Flag.FlagState currentState = state.orElse(flag.getDefaultState());

        // Choose material based on state
        switch (currentState) {
            case EVERYONE:
                material = Material.LIME_CONCRETE;
                break;
            case WHITELIST:
                material = Material.YELLOW_CONCRETE;
                break;
            case NONE:
                material = Material.RED_CONCRETE;
                break;
            default:
                material = Material.GRAY_CONCRETE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + flag.getName());

        List<String> lore = new ArrayList<>();
        lore.add("§7" + flag.getDescription());
        lore.add("");
        lore.add("§7Current State: " + getStateColor(currentState) + currentState.getDisplay());
        lore.add("§7" + currentState.getDescription());
        lore.add("");
        lore.add("§eClick to cycle through states:");
        lore.add("§a§lEVERYONE §7→ §e§lWHITELIST §7→ §c§lNONE");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getStateColor(Flag.FlagState state) {
        switch (state) {
            case EVERYONE:
                return "§a";
            case WHITELIST:
                return "§e";
            case NONE:
                return "§c";
            default:
                return "§7";
        }
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
