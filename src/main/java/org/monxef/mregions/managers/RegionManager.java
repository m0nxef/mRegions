package org.monxef.mregions.managers;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.models.Region;
import org.monxef.mregions.utils.ParticleUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RegionManager {
    private final RegionPlugin plugin;
    private final Map<String, Region> regionCache = new ConcurrentHashMap<>();
    private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();

    public void loadRegions() {
        regionCache.clear();
        plugin.getDatabaseManager().loadAllRegions()
                .forEach(region -> regionCache.put(region.getName().toLowerCase(), region));
    }

    public Optional<Region> getRegion(String name) {
        return Optional.ofNullable(regionCache.get(name.toLowerCase()));
    }

    public List<Region> getRegions() {
        return new ArrayList<>(regionCache.values());
    }

    public List<Region> getRegionsAt(Location location) {
        return regionCache.values().stream()
                .filter(region -> region.contains(location))
                .collect(Collectors.toList());
    }

    public boolean createRegion(String name, Player owner, Selection selection) {
        if (regionCache.containsKey(name.toLowerCase())) {
            return false;
        }

        Region region = Region.builder()
                .id(UUID.randomUUID())
                .name(name)
                .world(selection.getPos1().getWorld())
                .pos1(selection.getPos1())
                .pos2(selection.getPos2())
                .owner(owner.getUniqueId())
                .whitelist(new HashSet<>())
                .flags(new HashMap<>())
                .creationDate(System.currentTimeMillis())
                .build();
        ParticleUtil.showSelection(owner,selection.getPos1(),selection.getPos2());
        // Initialize default flags
        plugin.getFlagManager().getRegisteredFlags().values()
                .forEach(flag -> region.setFlag(flag, flag.getDefaultState()));

        regionCache.put(name.toLowerCase(), region);
        plugin.getDatabaseManager().saveRegion(region);
        return true;
    }

    public boolean deleteRegion(String name) {
        Region region = regionCache.remove(name.toLowerCase());
        if (region != null) {
            plugin.getDatabaseManager().deleteRegion(name);
            return true;
        }
        return false;
    }

    public Selection getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void setSelection(Player player, Selection selection) {
        selections.put(player.getUniqueId(), selection);
    }

    public static class Selection {
        private final Location pos1;
        private final Location pos2;

        public Selection(Location pos1, Location pos2) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public Location getPos1() {
            return pos1;
        }

        public Location getPos2() {
            return pos2;
        }
    }
}