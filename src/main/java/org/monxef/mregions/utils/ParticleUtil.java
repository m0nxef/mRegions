package org.monxef.mregions.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.monxef.mregions.RegionPlugin;

public class ParticleUtil {
    public static void showSelection(Player player, Location pos1, Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // Show particles at corners
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (isEdge(x, y, z, minX, minY, minZ, maxX, maxY, maxZ)) {
                        Location loc = new Location(pos1.getWorld(), x + 0.5, y + 0.5, z + 0.5);
                        int durationInSeconds = 5;
                        int intervalTicks = 10;

                        new BukkitRunnable() {
                            int elapsedTicks = 0;

                            @Override
                            public void run() {
                                if (elapsedTicks >= durationInSeconds * 20) { // 20 ticks = 1 second
                                    this.cancel(); // Stop the task after the duration
                                    return;
                                }
                                player.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
                                elapsedTicks += intervalTicks;
                            }
                        }.runTaskTimer(RegionPlugin.getInstance(), 0, intervalTicks); // Start immediately, repeat every intervalTicks
                    }
                }
            }
        }
    }

    private static boolean isEdge(int x, int y, int z, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return (x == minX || x == maxX) && (y == minY || y == maxY) ||
                (x == minX || x == maxX) && (z == minZ || z == maxZ) ||
                (y == minY || y == maxY) && (z == minZ || z == maxZ);
    }
}