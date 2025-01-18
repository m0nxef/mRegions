package org.monxef.mregions.commands.impl;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.SubCommand;
import org.monxef.mregions.models.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WhitelistCommand implements SubCommand {
    private final RegionPlugin plugin;

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public String getPermission() {
        return "region.whitelist";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                    "§cUsage: /region whitelist <region>");
            return;
        }

        String regionName = args[0];
        plugin.getRegionManager().getRegion(regionName).ifPresentOrElse(
                region -> {
                    List<String> playerNames = region.getWhitelist().stream()
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .collect(Collectors.toList());

                    player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                            "§6Whitelisted players in " + regionName + ":");
                    playerNames.forEach(name -> player.sendMessage("§7- §e" + name));
                },
                () -> player.sendMessage(plugin.getConfig().getString("messages.prefix") + "§cRegion not found!")
        );
    }

    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return plugin.getRegionManager().getRegions().stream()
                    .map(Region::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}