package org.monxef.mregions.managers;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.flags.DefaultFlags;
import org.monxef.mregions.models.Flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FlagManager {
    private final RegionPlugin plugin;
    @Getter
    private final Map<String, Flag> registeredFlags = new HashMap<>();

    public FlagManager(RegionPlugin plugin) {
        this.plugin = plugin;
        registerDefaultFlags();
    }

    private void registerDefaultFlags() {
        registerFlag(new DefaultFlags.BlockBreakFlag());
        registerFlag(new DefaultFlags.BlockPlaceFlag());
        registerFlag(new DefaultFlags.InteractFlag());
        registerFlag(new DefaultFlags.EntityDamageFlag());
    }

    public void registerFlag(Flag flag) {
        registeredFlags.put(flag.getId(), flag);
    }

    public Optional<Flag> getFlag(String id) {
        return Optional.ofNullable(registeredFlags.get(id));
    }

    public boolean validateFlag(String flagId, Player player, Flag.FlagState state) {
        return getFlag(flagId)
                .map(flag -> flag.validate(player, state))
                .orElse(false);
    }
}