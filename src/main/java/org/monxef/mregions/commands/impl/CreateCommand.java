package org.monxef.mregions.commands.impl;


import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.SubCommand;
import org.monxef.mregions.managers.RegionManager;

@RequiredArgsConstructor
public class CreateCommand implements SubCommand {
    private final RegionPlugin plugin;

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getPermission() {
        return "region.create";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") + "§cUsage: /region create <name>");
            return;
        }

        String name = args[0];
        RegionManager.Selection selection = plugin.getRegionManager().getSelection(player);

        if (selection == null) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cPlease select two points using the region wand first!");
            return;
        }

        if (plugin.getRegionManager().createRegion(name, player, selection)) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    plugin.getConfig().getString("messages.region-created").replace("%name%", name));
        } else {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cA region with that name already exists!");
        }
    }
}