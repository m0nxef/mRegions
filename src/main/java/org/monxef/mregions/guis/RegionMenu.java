package org.monxef.mregions.guis;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.models.Region;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.monxef.mregions.utils.ParticleUtil;

import java.util.ArrayList;
import java.util.List;

public class RegionMenu extends FastInv {
    private final RegionPlugin plugin;
    private final Region region;

    public RegionMenu(RegionPlugin plugin, Region region) {
        super(27, "§8Region: " + region.getName());
        this.plugin = plugin;
        this.region = region;

        // Fill border with glass panes
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, "§r"));
            }
        }

        // Region Info (Center)
        setItem(4, createInfoItem());

        // Rename Option
        setItem(10, createItem(Material.NAME_TAG, "§e§lRename Region", 
            "§7Click to rename this region"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.rename")) {
                player.sendMessage("§cYou don't have permission to rename regions!");
                return;
            }
            player.closeInventory();
            startRenameConversation(player);
        });

        // Whitelist Add
        setItem(12, createItem(Material.PLAYER_HEAD, "§a§lAdd to Whitelist", 
            "§7Click to add a player to whitelist"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.whitelist.add")) {
                player.sendMessage("§cYou don't have permission to modify whitelist!");
                return;
            }
            player.closeInventory();
            startWhitelistAddConversation(player);
        });

        // Whitelist Remove
        setItem(14, createItem(Material.BARRIER, "§c§lRemove from Whitelist", 
            "§7Click to remove a player from whitelist"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.whitelist.remove")) {
                player.sendMessage("§cYou don't have permission to modify whitelist!");
                return;
            }
            player.closeInventory();
            startWhitelistRemoveConversation(player);
        });

        // Redefine Location
        setItem(16, createItem(Material.COMPASS, "§6§lRedefine Location", 
            "§7Click to redefine region boundaries",
            "§7You'll need to make a new selection"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.redefine")) {
                player.sendMessage("§cYou don't have permission to redefine regions!");
                return;
            }
            player.closeInventory();
            player.performCommand("region wand");
            player.sendMessage("§aUse the wand to make a new selection, then type §e/region redefine " + region.getName());
        });

        // Delete Region
        setItem(22, createItem(Material.TNT, "§c§lDelete Region", 
            "§7Click to permanently delete this region",
            "§c⚠ This action cannot be undone!"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.delete")) {
                player.sendMessage("§cYou don't have permission to delete regions!");
                return;
            }
            if (!region.getOwner().equals(player.getUniqueId()) && !player.hasPermission("region.admin")) {
                player.sendMessage("§cYou can only delete your own regions!");
                return;
            }
            player.closeInventory();
            if (plugin.getRegionManager().deleteRegion(region.getName())) {
                player.sendMessage("§aSuccessfully deleted region §e" + region.getName() + "§a!");
            } else {
                player.sendMessage("§cFailed to delete region §e" + region.getName() + "§c!");
            }
        });

        // Edit Flags
        setItem(24, createItem(Material.REDSTONE_TORCH, "§d§lEdit Flags", 
            "§7Click to open flags menu"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.flags")) {
                player.sendMessage("§cYou don't have permission to edit flags!");
                return;
            }
            player.closeInventory();
            plugin.getMenuManager().openFlagsMenu(player, region);
        });
        setItem(20, createItem(Material.APPLE, "§9§lShow Borders",
                "§7Click to see your region's borders"), e -> {
            Player player = (Player) e.getWhoClicked();
            if (!player.hasPermission("region.borders")) {
                player.sendMessage("§cYou don't have permission to see borders!");
                return;
            }
            player.closeInventory();
            ParticleUtil.showSelection(player,region.getPos1(), region.getPos2());
        });
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§l" + region.getName());

        List<String> lore = new ArrayList<>();
        lore.add("§7Owner: §f" + plugin.getServer().getOfflinePlayer(region.getOwner()).getName());
        lore.add("§7World: §f" + region.getWorld().getName());
        lore.add("§7Position 1: §f" + formatLocation(region.getPos1()));
        lore.add("§7Position 2: §f" + formatLocation(region.getPos2()));
        lore.add("");
        lore.add("§7Whitelisted Players: §f" + region.getWhitelist().size());
        lore.add("§7Active Flags: §f" + region.getFlags().size());

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void startRenameConversation(Player player) {
        new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the new name for the region:";
                }
                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    if (input.length() < 3 || input.length() > 16) {
                        player.sendMessage("§cRegion name must be between 3 and 16 characters!");
                        return null;
                    }
                    if (plugin.getRegionManager().getRegion(input).isPresent()) {
                        player.sendMessage("§cA region with that name already exists!");
                        return null;
                    }
                    String oldName = region.getName();
                    region.setName(input);
                    plugin.getDatabaseManager().saveRegion(region);
                    player.sendMessage("§aRegion renamed from §e" + oldName + "§a to §e" + input);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getMenuManager().openRegionMenu(player, region);
                    });
                    return null;
                }
            })
            .withLocalEcho(false)
            .buildConversation(player)
            .begin();
    }

    private void startWhitelistAddConversation(Player player) {
        new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the player name to add to whitelist:";
                }

                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    // Get player from name
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        player.sendMessage("§cPlayer not found!");
                        return null;
                    }

                    // Check if already whitelisted
                    if (region.getWhitelist().contains(target.getUniqueId())) {
                        player.sendMessage("§cPlayer is already whitelisted!");
                        return null;
                    }

                    // Add to whitelist
                    region.getWhitelist().add(target.getUniqueId());
                    plugin.getDatabaseManager().saveRegion(region);
                    player.sendMessage("§aAdded §e" + target.getName() + "§a to the whitelist!");

                    // Reopen menu
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getMenuManager().openRegionMenu(player, region);
                    });
                    return null;
                }
            })
            .withLocalEcho(false)
            .buildConversation(player)
            .begin();
    }

    private void startWhitelistRemoveConversation(Player player) {
        new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the player name to remove from whitelist:";
                }

                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    // Get player UUID from name
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        player.sendMessage("§cPlayer not found!");
                        return null;
                    }

                    // Check if whitelisted
                    if (!region.getWhitelist().contains(target.getUniqueId())) {
                        player.sendMessage("§cPlayer is not whitelisted!");
                        return null;
                    }

                    // Remove from whitelist
                    region.getWhitelist().remove(target.getUniqueId());
                    plugin.getDatabaseManager().saveRegion(region);
                    player.sendMessage("§aRemoved §e" + target.getName() + "§a from the whitelist!");

                    // Reopen menu
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getMenuManager().openRegionMenu(player, region);
                    });
                    return null;
                }
            })
            .withLocalEcho(false)
            .buildConversation(player)
            .begin();
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
