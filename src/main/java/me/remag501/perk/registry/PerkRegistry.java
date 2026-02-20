package me.remag501.perk.registry;

import me.remag501.bgscore.api.event.EventService;
import me.remag501.bgscore.api.task.TaskService;
import me.remag501.perk.manager.PerkManager;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import me.remag501.perk.perk.impl.Bloodied;
import me.remag501.perk.perk.impl.Flash;
import me.remag501.perk.perk.impl.Kangaroo;
import me.remag501.perk.service.ItemService;
import org.bukkit.inventory.ItemStack;

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

    private final TaskService taskService;
    private final EventService eventService;
    private final ItemService itemService;

    public PerkRegistry(EventService eventService, TaskService taskService, ItemService itemService) {
        this.eventService = eventService;
        this.taskService = taskService;
        this.itemService = itemService;
        this.perks = new HashMap<>();
        this.perkItems = new HashMap<>();
    }

    public void init(PerkManager perkManager) {
        // Register KANGAROO perk
        registerPerk(PerkType.KANGAROO, new Kangaroo(eventService, taskService, perkManager));
        registerPerk(PerkType.BLOODIED, new Bloodied(eventService, taskService, perkManager));
        registerPerk(PerkType.FLASH, new Flash(taskService, perkManager));
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