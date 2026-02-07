package me.remag501.perks;

import me.remag501.bgscore.BGSCore;
import me.remag501.bgscore.api.BGSApi;
import me.remag501.bgscore.api.command.CommandService;
import me.remag501.bgscore.api.event.EventService;
import me.remag501.bgscore.api.task.TaskService;
import me.remag501.perks.command.PerksCommand;
import me.remag501.perks.listener.GambleListener;
import me.remag501.perks.listener.GlobalPerkListener;
import me.remag501.perks.listener.PerkMenuListener;
import me.remag501.perks.listener.ScrapListener;
import me.remag501.perks.manager.GambleManager;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.registry.WorldRegistry;
import me.remag501.perks.service.ItemService;
import me.remag501.perks.service.NamespaceService;
import me.remag501.perks.ui.GambleMenu;
import me.remag501.perks.ui.PerkMenu;
import me.remag501.perks.manager.ConfigManager;
import me.remag501.perks.ui.ScrapMenu;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.List;


public class Perks extends JavaPlugin {

    private PerkManager perkManager;
    private WorldRegistry worldRegistry;

    @Override
    public void onEnable() {
        getLogger().info("Starting Perks plugin initialization...");

        // 1. Get the services from the BGS api
        CommandService commandService = BGSApi.commands();
        EventService eventService = BGSApi.events();
        TaskService taskService = BGSApi.tasks();

        // 2. Load configuration first
        worldRegistry = new WorldRegistry();
        loadConfiguration();

        // 3. Initialize singletons in correct order

        NamespaceService namespaceService = new NamespaceService(this); // Needed to reference namespace
        ItemService itemService = new ItemService(namespaceService);

        PerkRegistry perkRegistry = new PerkRegistry(eventService, taskService, itemService);
        ConfigManager configManager = new ConfigManager(this, "perks.yml"); // Access plugin's folder config
        perkManager = new PerkManager(getLogger(), perkRegistry, worldRegistry, configManager);
        perkRegistry.init(perkManager);

        GambleMenu gambleMenu = new GambleMenu(perkManager, itemService);
        GambleManager gambleManager = new GambleManager(this, perkManager, perkRegistry, gambleMenu);
        PerkMenu perkMenu = new PerkMenu(perkManager, perkRegistry, itemService);
        ScrapMenu scrapMenu = new ScrapMenu();

        // 4. Register event listeners
        new GlobalPerkListener(eventService, perkManager, perkRegistry, worldRegistry, itemService);;
        new PerkMenuListener(eventService, perkManager, perkMenu, gambleMenu, itemService, scrapMenu);
        new GambleListener(eventService, gambleManager, perkMenu);
        new ScrapListener(eventService, perkManager, perkRegistry, perkMenu);

        // 4. Register commands
        this.getCommand("perks").setExecutor(new PerksCommand(this, perkManager, perkMenu, itemService));
        // You can add tab completers here too
        // this.getCommand("perks").setTabCompleter(new PerksTabCompleter());

        // 5. Load perks for online players (in case of reload)
        Bukkit.getOnlinePlayers().forEach(player -> perkManager.handlePlayerJoin(player));

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
            worldRegistry.DROP_WORLDS.clear();
            worldRegistry.DROP_WORLDS.addAll(dropWorlds);
            getLogger().info("Loaded " + dropWorlds.size() + " drop worlds");
        }

        if (disabledWorlds != null) {
            worldRegistry.DISABLED_WORLDS.clear();
            worldRegistry.DISABLED_WORLDS.addAll(disabledWorlds);
            getLogger().info("Loaded " + disabledWorlds.size() + " disabled worlds");
        }

        // Ensure perks.yml exists (will be created by ConfigUtil if needed)
        ConfigManager perkConfig = new ConfigManager(this, "perks.yml");
        perkConfig.save();
    }

}