package org.monxef.mregions.commands.impl;

import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.SubCommand;
import org.monxef.mregions.models.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteCommand implements SubCommand {
    private final RegionPlugin plugin;

    public DeleteCommand(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getPermission() {
        return "region.delete";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "&cUsage: /region delete <name>");
            return;
        }

        String regionName = args[0];
        plugin.getRegionManager().getRegion(regionName).ifPresentOrElse(region -> {
            // Check if player is owner or has admin permission
            if (!player.hasPermission("region.admin")) {
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "&cYou don't have permission to delete this region!");
                return;
            }

            // Delete the region
            if (plugin.getRegionManager().deleteRegion(regionName)) {
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "&aSuccessfully deleted region &e" + region.getName() + "&a!");
            } else {
                player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                        "&cFailed to delete region &e" + region.getName() + "&c!");
            }
        }, () -> player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                "&cRegion not found!"));
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return plugin.getRegionManager().getRegions().stream()
                    .filter(region -> player.hasPermission("region.admin"))
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
