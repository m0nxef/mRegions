package org.monxef.mregions.commands.impl;

import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.SubCommand;
import org.monxef.mregions.managers.RegionManager;
import org.monxef.mregions.models.Region;

import java.util.List;

public class RedefineCommand implements SubCommand {
    private final RegionPlugin plugin;

    public RedefineCommand(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "redefine";
    }

    @Override
    public String getPermission() {
        return "region.redefine";
    }


    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cUsage: /region flag <region> <flag> <state>");
            return;
        }

        String regionName = args[0];
        RegionManager.Selection selection = plugin.getRegionManager().getSelection(player);

        if (selection == null) {
            player.sendMessage("§cYou need to make a selection first! Use /region wand");
            return;
        }

        // Check if region exists
        plugin.getRegionManager().getRegion(regionName).ifPresentOrElse(region -> {
            // Check ownership or admin permission
            if (!player.hasPermission("region.admin")) {
                player.sendMessage("§cYou don't have permission to redefine this region!");
                return;
            }

            // Update region boundaries
            region.setPos1(selection.getPos1());
            region.setPos2(selection.getPos2());
            
            // Save changes
            plugin.getDatabaseManager().saveRegion(region);
            
            player.sendMessage("§aSuccessfully redefined region §e" + region.getName() + "§a!");
        }, () -> player.sendMessage("§cRegion not found!"));
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return SubCommand.super.getTabCompletions(player, args);
    }
}
