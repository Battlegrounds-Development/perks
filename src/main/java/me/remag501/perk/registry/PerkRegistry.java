package me.remag501.perk.registry;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.oraxen.OraxenService;
import me.remag501.core.api.task.TaskService;
import me.remag501.perk.manager.PerkManager;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import me.remag501.perk.perk.impl.Berserker;
import me.remag501.perk.perk.impl.Bloodied;
import me.remag501.perk.perk.impl.Flash;
import me.remag501.perk.perk.impl.FlowerPower;
import me.remag501.perk.perk.impl.GuerillaTactics;
import me.remag501.perk.perk.impl.HotHandsPerk;
import me.remag501.perk.perk.impl.Jumper;
import me.remag501.perk.perk.impl.Kangaroo;
import me.remag501.perk.perk.impl.LowMaintenance;
import me.remag501.perk.perk.impl.Overdrive;
import me.remag501.perk.perk.impl.Resistant;
import me.remag501.perk.perk.impl.Serendipity;
import me.remag501.perk.perk.impl.SneakAttack;
import me.remag501.perk.perk.impl.Undead;
import me.remag501.perk.perk.impl.BountyHunter;
import me.remag501.perk.perk.impl.Concussion;
import me.remag501.perk.perk.impl.CookieClicker;
import me.remag501.perk.perk.impl.GhostFist;
import me.remag501.perk.perk.impl.TaiChi;
import me.remag501.perk.perk.impl.XPFarm;
import me.remag501.perk.perk.impl.PackMaster;
import me.remag501.perk.perk.impl.Feral;
import me.remag501.perk.perk.impl.Jumped;
import me.remag501.perk.perk.impl.WolfBounded;
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

    private final TaskService taskService;
    private final EventService eventService;
    private final ItemService itemService;

    private final Map<PerkType, Perk> perks;
    private final Map<PerkType, ItemStack> perkItems;


    public PerkRegistry(EventService eventService, TaskService taskService, ItemService itemService) {
        this.eventService = eventService;
        this.taskService = taskService;
        this.itemService = itemService;

        this.perks = new HashMap<>();
        this.perkItems = new HashMap<>();
    }

    public void init(PerkManager perkManager) {
        // Register all perks
        // Pack / wolf related perks first so dependent perks can reference PackMaster
        registerPerk(PerkType.PACK_MASTER, new PackMaster(eventService));
        registerPerk(PerkType.KANGAROO, new Kangaroo(eventService, taskService, perkManager));
        registerPerk(PerkType.BLOODIED, new Bloodied(eventService, taskService, perkManager));
        registerPerk(PerkType.FLASH, new Flash(taskService, perkManager));
        registerPerk(PerkType.LOW_MAINTENANCE, new LowMaintenance(taskService, perkManager));
        registerPerk(PerkType.JUMPER, new Jumper(taskService, perkManager));
        registerPerk(PerkType.RESISTANT, new Resistant(eventService, taskService, perkManager));
        registerPerk(PerkType.SERENDIPITY, new Serendipity(eventService));
        registerPerk(PerkType.OVERDRIVE, new Overdrive(eventService));
        registerPerk(PerkType.HOT_HANDS, new HotHandsPerk(eventService));
        registerPerk(PerkType.BERSERKER, new Berserker(eventService));
        registerPerk(PerkType.SNEAK_ATTACK, new SneakAttack(eventService));
        registerPerk(PerkType.FLOWER_POWER, new FlowerPower(eventService));
        registerPerk(PerkType.GUERRILLA_TACTICS, new GuerillaTactics(eventService, taskService));
        registerPerk(PerkType.UNDEAD, new Undead(eventService));
        // Migrated legacy perks
        registerPerk(PerkType.BOUNTY_HUNTER, new BountyHunter(eventService));
        registerPerk(PerkType.CONCUSSION, new Concussion(eventService));
        registerPerk(PerkType.COOKIE_CLICKER, new CookieClicker(eventService));
        registerPerk(PerkType.GHOST_FIST, new GhostFist(eventService, taskService));
        registerPerk(PerkType.TAI_CHI, new TaiChi(eventService));
        registerPerk(PerkType.XP_FARM, new XPFarm(eventService));
        // Wolf-related perks that depend on PackMaster
        PackMaster packMaster = (PackMaster) getPerk(PerkType.PACK_MASTER);
        registerPerk(PerkType.FERAL, new Feral(eventService, packMaster));
        registerPerk(PerkType.JUMPED, new Jumped(eventService, packMaster));
        registerPerk(PerkType.WOLF_BOUNDED, new WolfBounded(eventService, taskService, packMaster));
    }

    private void registerPerk(PerkType type, Perk perk) {
        perks.put(type, perk);
        ItemStack item = createPerkItem(type);
        perkItems.put(type, item);
    }

    private ItemStack createPerkItem(PerkType type) {
        ItemStack item = itemService.createOraxenPerkItem(type);
        // If item is null, it wasn't defined in Oraxen config, so create a fallback item
        return (item != null) ? item : itemService.createPerkItem(
                type.getCustomModelData(),
                type.getDisplayName(),
                type.getId(),
                type.getRarity(),
                type.getDescription()
        );
    }

    public void reload() {
        // Clear existing perks and items
        perkItems.clear();
        // Recreate perk items (in case of config changes)
        for (PerkType type : perks.keySet()) {
            ItemStack item = createPerkItem(type);
            perkItems.put(type, item);
        }
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