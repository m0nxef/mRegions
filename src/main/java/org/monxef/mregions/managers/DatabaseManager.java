package org.monxef.mregions.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.monxef.mregions.RegionPlugin;
import org.monxef.mregions.models.Flag;
import org.monxef.mregions.models.Region;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {
    private final RegionPlugin plugin;
    private HikariDataSource dataSource;
    private boolean usingSQLite;

    public DatabaseManager(RegionPlugin plugin, HikariDataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.usingSQLite = plugin.getConfig().getString("storage.type", "sqlite").equalsIgnoreCase("sqlite");
        initializeHikari();
    }

    private void initializeHikari() {
        HikariConfig config = new HikariConfig();
        
        if (!usingSQLite) {
            String host = plugin.getConfig().getString("storage.host");
            int port = plugin.getConfig().getInt("storage.port");
            String database = plugin.getConfig().getString("storage.database");
            String username = plugin.getConfig().getString("storage.username");
            String password = plugin.getConfig().getString("storage.password");

            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                    host, port, database));
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/database.db");
            config.setDriverClassName("org.sqlite.JDBC");
        }

        // Connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000); // 5 seconds
        config.setIdleTimeout(300000); // 5 minutes
        config.setMaxLifetime(600000); // 10 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute
        
        // Add connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        dataSource = new HikariDataSource(config);
    }

    public void initializeTables() {
        String autoIncrement = usingSQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT";
        String onUpdateCascade = usingSQLite ? "" : "ON UPDATE CASCADE";

        // Create regions table
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS regions (" +
                             "id VARCHAR(36) PRIMARY KEY," +
                             "name VARCHAR(64) NOT NULL UNIQUE," +
                             "world VARCHAR(64) NOT NULL," +
                             "pos1_x DOUBLE NOT NULL," +
                             "pos1_y DOUBLE NOT NULL," +
                             "pos1_z DOUBLE NOT NULL," +
                             "pos2_x DOUBLE NOT NULL," +
                             "pos2_y DOUBLE NOT NULL," +
                             "pos2_z DOUBLE NOT NULL," +
                             "owner VARCHAR(36) NOT NULL," +
                             "creation_date BIGINT NOT NULL" +
                             ")"
             )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create whitelist table
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS region_whitelist (" +
                             "region_id VARCHAR(36) NOT NULL," +
                             "player_uuid VARCHAR(36) NOT NULL," +
                             "PRIMARY KEY (region_id, player_uuid)," +
                             "FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE " + onUpdateCascade +
                             ")"
             )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create flags table
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS region_flags (" +
                             "region_id VARCHAR(36) NOT NULL," +
                             "flag_id VARCHAR(64) NOT NULL," +
                             "flag_state VARCHAR(32) NOT NULL," +
                             "PRIMARY KEY (region_id, flag_id)," +
                             "FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE " + onUpdateCascade +
                             ")"
             )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRegion(Region region) {
        String upsertQuery = usingSQLite ?
                "INSERT OR REPLACE INTO regions (id, name, world, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z, owner, creation_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
                "INSERT INTO regions (id, name, world, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z, owner, creation_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "name=VALUES(name), world=VALUES(world), pos1_x=VALUES(pos1_x), pos1_y=VALUES(pos1_y), " +
                        "pos1_z=VALUES(pos1_z), pos2_x=VALUES(pos2_x), pos2_y=VALUES(pos2_y), pos2_z=VALUES(pos2_z), " +
                        "owner=VALUES(owner)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Save region data
                try (PreparedStatement stmt = conn.prepareStatement(upsertQuery)) {
                    stmt.setString(1, region.getId().toString());
                    stmt.setString(2, region.getName());
                    stmt.setString(3, region.getWorld().getName());
                    stmt.setDouble(4, region.getPos1().getX());
                    stmt.setDouble(5, region.getPos1().getY());
                    stmt.setDouble(6, region.getPos1().getZ());
                    stmt.setDouble(7, region.getPos2().getX());
                    stmt.setDouble(8, region.getPos2().getY());
                    stmt.setDouble(9, region.getPos2().getZ());
                    stmt.setString(10, region.getOwner().toString());
                    stmt.setLong(11, region.getCreationDate());
                    stmt.executeUpdate();
                }

                // Delete existing whitelist entries
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM region_whitelist WHERE region_id = ?"
                )) {
                    stmt.setString(1, region.getId().toString());
                    stmt.executeUpdate();
                }

                // Save whitelist
                if (!region.getWhitelist().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO region_whitelist (region_id, player_uuid) VALUES (?, ?)"
                    )) {
                        for (UUID playerUUID : region.getWhitelist()) {
                            stmt.setString(1, region.getId().toString());
                            stmt.setString(2, playerUUID.toString());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // Delete existing flag entries
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM region_flags WHERE region_id = ?"
                )) {
                    stmt.setString(1, region.getId().toString());
                    stmt.executeUpdate();
                }

                // Save flags
                if (!region.getFlags().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO region_flags (region_id, flag_id, flag_state) VALUES (?, ?, ?)"
                    )) {
                        for (Map.Entry<String, Flag.FlagState> entry : region.getFlags().entrySet()) {
                            stmt.setString(1, region.getId().toString());
                            stmt.setString(2, entry.getKey());
                            stmt.setString(3, entry.getValue().name());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<Region> loadRegion(String name) {
        try (Connection conn = dataSource.getConnection()) {
            Region region = null;

            // Load region data
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM regions WHERE name = ?"
            )) {
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    World world = org.bukkit.Bukkit.getWorld(rs.getString("world"));
                    if (world != null) {
                        Location pos1 = new Location(world, rs.getDouble("pos1_x"), rs.getDouble("pos1_y"), rs.getDouble("pos1_z"));
                        Location pos2 = new Location(world, rs.getDouble("pos2_x"), rs.getDouble("pos2_y"), rs.getDouble("pos2_z"));

                        region = Region.builder()
                                .id(UUID.fromString(rs.getString("id")))
                                .name(rs.getString("name"))
                                .world(world)
                                .pos1(pos1)
                                .pos2(pos2)
                                .owner(UUID.fromString(rs.getString("owner")))
                                .creationDate(rs.getLong("creation_date"))
                                .whitelist(new HashSet<>())
                                .flags(new HashMap<>())
                                .build();
                    }
                }
            }

            if (region != null) {
                // Load whitelist
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT player_uuid FROM region_whitelist WHERE region_id = ?"
                )) {
                    stmt.setString(1, region.getId().toString());
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        region.getWhitelist().add(UUID.fromString(rs.getString("player_uuid")));
                    }
                }

                // Load flags
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT flag_id, flag_state FROM region_flags WHERE region_id = ?"
                )) {
                    stmt.setString(1, region.getId().toString());
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        region.getFlags().put(
                                rs.getString("flag_id"),
                                Flag.FlagState.valueOf(rs.getString("flag_state"))
                        );
                    }
                }
            }

            return Optional.ofNullable(region);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void deleteRegion(String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM regions WHERE name = ?"
             )) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Region> loadAllRegions() {
        List<Region> regions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            // Load all regions first
            Map<String, Region> regionsMap = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM regions")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    World world = org.bukkit.Bukkit.getWorld(rs.getString("world"));
                    if (world != null) {
                        Location pos1 = new Location(world, rs.getDouble("pos1_x"), rs.getDouble("pos1_y"), rs.getDouble("pos1_z"));
                        Location pos2 = new Location(world, rs.getDouble("pos2_x"), rs.getDouble("pos2_y"), rs.getDouble("pos2_z"));

                        Region region = Region.builder()
                                .id(UUID.fromString(rs.getString("id")))
                                .name(rs.getString("name"))
                                .world(world)
                                .pos1(pos1)
                                .pos2(pos2)
                                .owner(UUID.fromString(rs.getString("owner")))
                                .creationDate(rs.getLong("creation_date"))
                                .whitelist(new HashSet<>())
                                .flags(new HashMap<>())
                                .build();

                        regionsMap.put(region.getId().toString(), region);
                        regions.add(region);
                    }
                }
            }

            // Batch load whitelists
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT region_id, player_uuid FROM region_whitelist")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String regionId = rs.getString("region_id");
                    Region region = regionsMap.get(regionId);
                    if (region != null) {
                        region.getWhitelist().add(UUID.fromString(rs.getString("player_uuid")));
                    }
                }
            }

            // Batch load flags
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT region_id, flag_id, flag_state FROM region_flags")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String regionId = rs.getString("region_id");
                    Region region = regionsMap.get(regionId);
                    if (region != null) {
                        region.getFlags().put(
                                rs.getString("flag_id"),
                                Flag.FlagState.valueOf(rs.getString("flag_state"))
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return regions;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}