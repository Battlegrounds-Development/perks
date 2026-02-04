package me.remag501.perks;

import me.remag501.perks.command.PerksCommand;
import me.remag501.perks.listener.GlobalPerkListener;
import me.remag501.perks.listener.PerkMenuListener;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.ui.PerkMenu;
import me.remag501.perks.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static me.remag501.perks.util.WorldUtil.DISABLED_WORLDS;
import static me.remag501.perks.util.WorldUtil.DROP_WORLDS;

public class Perks extends JavaPlugin {

    private PerkManager perkManager;

    @Override
    public void onEnable() {
        getLogger().info("Starting Perks plugin initialization...");

        // 1. Load configuration first
        loadConfiguration();

        // 2. Initialize singletons in correct order

        PerkRegistry perkRegistry = new PerkRegistry(this);
        ConfigManager configManager = new ConfigManager(this, "perks.yml");
        perkManager = new PerkManager(this, perkRegistry, configManager);

        perkRegistry.init(perkManager);

        PerkMenu perkMenu = new PerkMenu(perkManager, perkRegistry);

        // 3. Register event listeners
        getServer().getPluginManager().registerEvents(new GlobalPerkListener(perkManager, perkRegistry), this);
        getServer().getPluginManager().registerEvents(new PerkMenuListener(perkManager, perkRegistry, perkMenu), this);

        // 4. Register commands
        this.getCommand("perks").setExecutor(new PerksCommand(this, perkManager, perkMenu));
        // You can add tab completers here too
        // this.getCommand("perks").setTabCompleter(new PerksTabCompleter());

        // 5. Load perks for online players (in case of reload)
        Bukkit.getOnlinePlayers().forEach(player -> {
            getLogger().info("Loading perks for online player: " + player.getName());
            perkManager.handlePlayerJoin(player);
        });

        getLogger().info("Perks plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving all player perk data...");

        // Save all player data before shutdown
        if (perkManager != null) {
            perkManager.saveAllPerks();
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
            DROP_WORLDS.clear();
            DROP_WORLDS.addAll(dropWorlds);
            getLogger().info("Loaded " + dropWorlds.size() + " drop worlds");
        }

        if (disabledWorlds != null) {
            DISABLED_WORLDS.clear();
            DISABLED_WORLDS.addAll(disabledWorlds);
            getLogger().info("Loaded " + disabledWorlds.size() + " disabled worlds");
        }

        // Ensure perks.yml exists (will be created by ConfigUtil if needed)
        ConfigManager perkConfig = new ConfigManager(this, "perks.yml");
        perkConfig.save();
    }

}