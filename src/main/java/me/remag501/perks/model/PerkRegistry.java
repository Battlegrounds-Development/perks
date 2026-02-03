package me.remag501.perks.model;

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

    private static PerkRegistry instance;

    private final Map<PerkType, Perk> perks;
    private final Map<PerkType, ItemStack> perkItems;
    private final Plugin plugin;

    private PerkRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.perks = new HashMap<>();
        this.perkItems = new HashMap<>();
        registerPerks();
    }

    public static void initialize(Plugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("PerkRegistry already initialized");
        }
        instance = new PerkRegistry(plugin);
    }

    public static PerkRegistry getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PerkRegistry not initialized");
        }
        return instance;
    }

    private void registerPerks() {
        // Register KANGAROO perk
        PerkType kangarooType = PerkType.KANGAROO;
        PerkType bloodiedType = PerkType.BLOODIED;
        PerkType flashType = PerkType.FLASH;
        Perk kangarooPerk = new Kangaroo("KANGAROO", kangarooType);
        Perk bloodiedPerk = new Bloodied("BLOODIED", PerkType.BLOODIED);
        Perk flashPerk = new Flash("FLASH", PerkType.FLASH);
        registerPerk(kangarooType, kangarooPerk);
        registerPerk(bloodiedType, bloodiedPerk);
        registerPerk(flashType, flashPerk);
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

    public Plugin getPlugin() {
        return plugin;
    }
}