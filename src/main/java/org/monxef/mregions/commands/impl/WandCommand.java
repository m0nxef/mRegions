package org.monxef.mregions.commands.impl;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.commands.SubCommand;
import org.monxef.mregions.listeners.WandListener;

@RequiredArgsConstructor
public class WandCommand implements SubCommand {
    private final RegionPlugin plugin;

    @Override
    public String getName() {
        return "wand";
    }

    @Override
    public String getPermission() {
        return "region.create";
    }

    @Override
    public void execute(Player player, String[] args) {

        player.getInventory().addItem(WandListener.createWand());
        player.sendMessage(plugin.getConfig().getString("messages.prefix") +
                "§aYou have received the region selection wand!");
    }
}