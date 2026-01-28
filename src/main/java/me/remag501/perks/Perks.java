package me.remag501.perks;

import me.remag501.perks.command.PerksCommand;
import me.remag501.perks.command.PerksCompleter;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PlayerPerks;
import me.remag501.perks.listener.GambleUI;
import me.remag501.perks.listener.PerkChangeListener;
import me.remag501.perks.listener.ScrapUI;
import me.remag501.perks.listener.UI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Perks extends JavaPlugin {

    private static Plugin perks;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getLogger().info("Perks has started up!");
        // Add commands to the plugin
        getCommand("perks").setExecutor(new PerksCommand(this));
        getCommand("perks").setTabCompleter(new PerksCompleter(this));
        // Register listeners for Perks UIs
        getServer().getPluginManager().registerEvents(new UI(null, false), this);
        getServer().getPluginManager().registerEvents(new GambleUI(), this);
        getServer().getPluginManager().registerEvents(new PerkChangeListener(), this);
        getServer().getPluginManager().registerEvents(new ScrapUI(), this);
        // Enable worlds for the plugin
        PerkChangeListener.dropWorlds.add("sahara");
        PerkChangeListener.dropWorlds.add("icycaverns");
        PerkChangeListener.dropWorlds.add("kuroko");
//        PerkChangeListener.disabledWorlds.add("musicland");
//        PerkChangeListener.disabledWorlds.add("thundra");
//        PerkChangeListener.disabledWorlds.add("test");
        PerkChangeListener.disabledWorlds.add("spawn");
        PerkChangeListener.disabledWorlds.add("dungeonhub");
        PerkChangeListener.disabledWorlds.add("honeyclicker");
//        // Enable listerners for perks
        for (PerkType perkType: PerkType.values()) {
            getServer().getPluginManager().registerEvents((Listener) perkType.getPerk(), this);
        }

        this.perks = this;
    }

    @Override
    public void onDisable() {
        // Disable all perks enabled for every player
        PlayerPerks.savePerks();
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Perk perk : PlayerPerks.getPlayerPerks(player.getUniqueId()).getEquippedPerks()) {
                perk.onDisable();
            }
        }
    }

    public static Plugin getPlugin() {
        return perks;
    }
}
