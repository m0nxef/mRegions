package org.monxef.mregions.commands;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface SubCommand {
    String getName();
    String getPermission();
    void execute(Player player, String[] args);
    default List<String> getTabCompletions(Player player, String[] args) {
        return new ArrayList<>();
    }
}
