package me.remag501.perks.service;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class NamespaceService {

    // Centralized Keys
    public final NamespacedKey PERK_ID_KEY;
    public final NamespacedKey RARITY_KEY;


    public NamespaceService(Plugin plugin) {
        PERK_ID_KEY = new NamespacedKey(plugin, "unique_id");
        RARITY_KEY = new NamespacedKey(plugin, "rarity");
    }

}
