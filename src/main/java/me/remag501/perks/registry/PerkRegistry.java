package me.remag501.perks.registry;

import me.remag501.bgscore.api.TaskHelper;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.perk.impl.Bloodied;
import me.remag501.perks.perk.impl.Flash;
import me.remag501.perks.perk.impl.Kangaroo;
import me.remag501.perks.service.ItemService;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified PerkRegistry - no PerkInstance tracking needed.
 * Just manages the singleton Perk objects and their items.
 */
public class PerkRegistry {

    private final Map<PerkType, Perk> perks;
    private final Map<PerkType, ItemStack> perkItems;

    private final TaskHelper taskHelper;
    private final ItemService itemService;

    public PerkRegistry(TaskHelper taskHelper, ItemService itemService) {
        this.taskHelper = taskHelper;
        this.itemService = itemService;
        this.perks = new HashMap<>();
        this.perkItems = new HashMap<>();
    }

    public void init(PerkManager perkManager) {
        // Register KANGAROO perk
        registerPerk(PerkType.KANGAROO, new Kangaroo(taskHelper, perkManager));
        registerPerk(PerkType.BLOODIED, new Bloodied(taskHelper, perkManager));
        registerPerk(PerkType.FLASH, new Flash(taskHelper, perkManager));
    }

    private void registerPerk(PerkType type, Perk perk) {
        perks.put(type, perk);

        ItemStack item = itemService.createPerkItem(
                type.getCustomModelData(),
                type.getDisplayName(),
                type.getId(),
                type.getRarity(),
                type.getDescription()
        );
        perkItems.put(type, item);
    }

    public Perk getPerk(PerkType type) {
        return perks.get(type);
    }

    public ItemStack getPerkItem(PerkType type) {
        return perkItems.get(type);
    }

    public List<PerkType> getPerksByRarity(int rarity) {
        List<PerkType> perks = new ArrayList<>();
        for (PerkType type : PerkType.values()) {
            if (type.getRarity() == rarity) {
                perks.add(type);
            }
        }
        return perks;
    }
}