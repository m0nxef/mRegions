package org.monxef.mregions;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monxef.mregions.commands.RegionCommand;
import org.monxef.mregions.guis.FlagsMenu;
import org.monxef.mregions.guis.MenuManager;
import org.monxef.mregions.listeners.RegionEnterListener;
import org.monxef.mregions.listeners.RegionListener;
import org.monxef.mregions.listeners.WandListener;
import org.monxef.mregions.managers.DatabaseManager;
import org.monxef.mregions.managers.FlagManager;
import org.monxef.mregions.managers.RegionManager;
import org.monxef.mregions.models.Region;

public class RegionPlugin extends JavaPlugin {

    @Getter
    private static RegionPlugin instance;

    @Getter
    private RegionManager regionManager;

    @Getter
    private FlagManager flagManager;
    @Getter
    private MenuManager menuManager;

    @Getter
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        saveDefaultConfig();

        FastInvManager.register(this);

        // Initialize managers
        initializeDatabase();
        initializeManagers();
        // Register commands
        registerCommands();

        // Load regions asynchronously
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            regionManager.loadRegions();
            getLogger().info("Loaded " + regionManager.getRegions().size() + " regions from database.");
        });

        // Register listeners
        registerListeners();

        getLogger().info("Region Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("Region Plugin has been disabled!");
    }

    private void initializeDatabase() {
        String storageType = getConfig().getString("storage.type", "sqlite").toLowerCase();
        HikariConfig config = new HikariConfig(); // Create config once

        if (storageType.equals("mysql")) {
            config.setJdbcUrl("jdbc:mysql://" +
                    getConfig().getString("storage.mysql.host", "localhost") + ":" +
                    getConfig().getInt("storage.mysql.port", 3306) + "/" +
                    getConfig().getString("storage.mysql.database", "minecraft"));
            config.setUsername(getConfig().getString("storage.mysql.username", "root"));
            config.setPassword(getConfig().getString("storage.mysql.password", ""));
            config.setMaximumPoolSize(getConfig().getInt("storage.mysql.pool-size", 10));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Explicitly set Driver

        } else {
            // SQLite configuration
            String dbPath = getDataFolder().getAbsolutePath() + "/database.db";
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1); // SQLite only supports one connection
            config.setConnectionTestQuery("SELECT 1");

            // SQLite-specific settings (still good to keep)
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        }
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setLeakDetectionThreshold(60000);

        databaseManager = new DatabaseManager(this, new HikariDataSource(config));

        // Handle the CompletableFuture returned by initializeTables()
        databaseManager.initializeTables();

    }
    private void initializeManagers() {
        flagManager = new FlagManager(this);
        regionManager = new RegionManager(this);
        menuManager = new MenuManager(this);
    }

    private void registerCommands() {
        getCommand("region").setExecutor(new RegionCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new WandListener(this), this);
        pm.registerEvents(new RegionListener(this), this);
        pm.registerEvents(new RegionEnterListener(this), this);
    }
}