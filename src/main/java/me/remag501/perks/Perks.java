package me.remag501.perks;

import me.remag501.perks.command.PerksCommand;
import me.remag501.perks.listener.GlobalPerkListener;
import me.remag501.perks.listener.PerkMenuListener;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkRegistry;
import me.remag501.perks.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Perks extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Starting Perks plugin initialization...");

        // 1. Load configuration first
        loadConfiguration();

        // 2. Initialize singletons in correct order
        getLogger().info("Initializing PerkRegistry...");
        PerkRegistry.initialize(this);

        getLogger().info("Initializing PerkManager...");
        PerkManager.initialize(this);

        // 3. Register event listeners
        getLogger().info("Registering event listeners...");
        registerListeners();

        // 4. Register commands
        getLogger().info("Registering commands...");
        registerCommands();

        // 5. Load perks for online players (in case of reload)
        getLogger().info("Loading perks for online players...");
        loadOnlinePlayers();

        getLogger().info("Perks plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving all player perk data...");

        // Save all player data before shutdown
        if (PerkManager.getInstance() != null) {
            PerkManager.getInstance().saveAllPerks();
        }

        getLogger().info("Perks plugin disabled!");
    }

    /**
     * Load configuration from config.yml and perks.yml
     */
    private void loadConfiguration() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Load main config
        FileConfiguration config = getConfig();

        // Load world lists from config
        List<String> dropWorlds = config.getStringList("drop-worlds");
        List<String> disabledWorlds = config.getStringList("disabled-worlds");

        if (dropWorlds != null) {
            GlobalPerkListener.dropWorlds.clear();
            GlobalPerkListener.dropWorlds.addAll(dropWorlds);
            getLogger().info("Loaded " + dropWorlds.size() + " drop worlds");
        }

        if (disabledWorlds != null) {
            GlobalPerkListener.disabledWorlds.clear();
            GlobalPerkListener.disabledWorlds.addAll(disabledWorlds);
            getLogger().info("Loaded " + disabledWorlds.size() + " disabled worlds");
        }

        // Ensure perks.yml exists (will be created by ConfigUtil if needed)
        ConfigUtil perkConfig = new ConfigUtil(this, "perks.yml");
        perkConfig.save();
    }

    /**
     * Register all event listeners
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GlobalPerkListener(), this);
        getServer().getPluginManager().registerEvents(new PerkMenuListener(), this);

        // Note: Individual perk listeners are registered by PerkRegistry
    }

    /**
     * Register all commands
     */
    private void registerCommands() {
        this.getCommand("perks").setExecutor(new PerksCommand(this));

        // You can add tab completers here too
        // this.getCommand("perks").setTabCompleter(new PerksTabCompleter());
    }

    /**
     * Load perks for any players already online (useful for plugin reload)
     */
    private void loadOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            getLogger().info("Loading perks for online player: " + player.getName());
            PerkManager.getInstance().handlePlayerJoin(player);
        });
    }
}