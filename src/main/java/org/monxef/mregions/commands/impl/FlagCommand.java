package org.monxef.mregions.commands.impl;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.SubCommand;
import org.monxef.mregions.models.Flag;

@RequiredArgsConstructor
public class FlagCommand implements SubCommand {
    private final RegionPlugin plugin;

    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public String getPermission() {
        return "region.flag";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cUsage: /region flag <region> <flag> <state>");
            return;
        }

        String regionName = args[0];
        String flagName = args[1];
        String stateName = args[2].toUpperCase();

        plugin.getRegionManager().getRegion(regionName).ifPresentOrElse(
                region -> {
                    plugin.getFlagManager().getFlag(flagName).ifPresentOrElse(
                            flag -> {
                                try {
                                    Flag.FlagState state = Flag.FlagState.valueOf(stateName);
                                    region.setFlag(flag, state);
                                    plugin.getDatabaseManager().saveRegion(region);
                                    player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                                            "§aFlag " + flagName + " has been set to " + stateName + " for region " + regionName);
                                } catch (IllegalArgumentException e) {
                                    player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                                            "§cInvalid flag state! Use: EVERYONE, WHITELIST, or NONE");
                                }
                            },
                            () -> player.sendMessage(plugin.getConfig().getString("messages.prefix") + "§cInvalid flag!")
                    );
                },
                () -> player.sendMessage(plugin.getConfig().getString("messages.prefix") + "§cRegion not found!")
        );
    }
}
