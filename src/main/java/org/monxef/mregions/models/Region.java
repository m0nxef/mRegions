package org.monxef.mregions.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
public class Region {
    private String name;
    private UUID id;
    private Location pos1;
    private Location pos2;
    private World world;
    private Set<UUID> whitelist;
    private Map<String, Flag.FlagState> flags;
    private UUID owner;
    private long creationDate;

    public Region(String name, UUID id, Location pos1, Location pos2, World world,
                  Set<UUID> whitelist, Map<String, Flag.FlagState> flags, UUID owner) {
        this.name = name;
        this.id = id;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.world = world;
        this.whitelist = whitelist != null ? whitelist : new HashSet<>();
        this.flags = flags != null ? flags : new HashMap<>();
        this.owner = owner;
        this.creationDate = System.currentTimeMillis();
    }

    public boolean isAllowed(Player player, Flag flag) {
        if (!flags.containsKey(flag.getId())) {
            return false;
        }

        Flag.FlagState state = flags.get(flag.getId());

        // Check if player has bypass permission
        if (player.hasPermission("region.bypass")) {
            return true;
        }

        // Check owner
        if (player.getUniqueId().equals(owner)) {
            return true;
        }

        switch (state) {
            case EVERYONE:
                return true;
            case WHITELIST:
                return whitelist.contains(player.getUniqueId());
            case NONE:
                return false;
            default:
                return false;
        }
    }

    public void setFlag(Flag flag, Flag.FlagState state) {
        flags.put(flag.getId(), state);
    }

    public Optional<Flag.FlagState> getFlagState(Flag flag) {
        return Optional.ofNullable(flags.get(flag.getId()));
    }

    // Location checking methods remain the same
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}