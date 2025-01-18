package org.monxef.mregions.commands;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mregions.RegionPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface SubCommand {
    String getName();
    String getPermission();
    void execute(Player player, String[] args);
    default List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }
}
