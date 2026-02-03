package me.remag501.perks.registry;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.perk.impl.Bloodied;
import me.remag501.perks.perk.impl.Flash;
import me.remag501.perks.perk.impl.Kangaroo;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified PerkRegistry - no PerkInstance tracking needed.
 * Just manages the singleton Perk objects and their items.
 */
public class PerkRegistry {

    private final Map<PerkType, Perk> perks;
    private final Map<PerkType, ItemStack> perkItems;
    private final Plugin plugin; // Used to register listener

    public PerkRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.perks = new HashMap<>();
        this.perkItems = new HashMap<>();
    }

    public void init(PerkManager perkManager) {
        // Register KANGAROO perk
        registerPerk(PerkType.KANGAROO, new Kangaroo(plugin, perkManager));
        registerPerk(PerkType.BLOODIED, new Bloodied(plugin , perkManager));
        registerPerk(PerkType.FLASH, new Flash(plugin, perkManager));
    }

    private void registerPerk(PerkType type, Perk perk) {
        perks.put(type, perk);

        ItemStack item = ItemUtil.createPerkItem(
                type.getCustomModelData(),
                type.getDisplayName(),
                type.getId(),
                type.getRarity(),
                type.getDescription()
        );
        perkItems.put(type, item);

        Bukkit.getPluginManager().registerEvents(perk, plugin);
    }

    public Perk getPerk(PerkType type) {
        return perks.get(type);
    }

    public ItemStack getPerkItem(PerkType type) {
        return perkItems.get(type);
    }
}