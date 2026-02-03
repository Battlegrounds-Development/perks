package me.remag501.perks.ui;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PerkMenu {

    private final PerkManager perkManager;
    private final PerkRegistry perkRegistry;

    public PerkMenu(PerkManager perkManager, PerkRegistry perkRegistry) {
        this.perkManager = perkManager;
        this.perkRegistry = perkRegistry;
    }

    public void open(Player player, int page, boolean hiddenMenu) {
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, "Choose Your Perk");

        // 1. Load Header
        ItemStack head = ItemUtil.createPerkSkull(player.getUniqueId(), player.getDisplayName(), player.getDisplayName());
        inv.setItem(0, head);

        ItemStack rollButton = ItemUtil.createItem(
                Material.SUNFLOWER,
                "§6§lOBTAIN PERKS",
                "casino",
                false,
                "§7Perk Points: " + profile.getPerkPoints()
        );
        inv.setItem(8, rollButton);

        // 2. Load Active Perks (Slots 2-6)
        Map<PerkType, Integer> equipped = profile.getEquippedPerks();
        int slotIndex = 2;

        for (Map.Entry<PerkType, Integer> entry : equipped.entrySet()) {
            if (slotIndex >= 7) break; // Max 5 slots (2-6)

            PerkType type = entry.getKey();
            int stars = entry.getValue();

            ItemStack item = perkRegistry.getPerkItem(type).clone();
            ItemUtil.updateStarCount(item, type, stars);
            inv.setItem(slotIndex++, item);
        }

        // Fill remaining equipped slots with air
        while (slotIndex < 7) {
            inv.setItem(slotIndex++, new ItemStack(Material.AIR));
        }

        // 3. Load Available Perks (Middle Grid)
        List<PerkType> owned = profile.getOwnedPerksList();
        List<PerkType> pagePerks = perkTypesByPage(page);

        for (int i = 19, k = 0; i < 35; i++) {
            if (i % 9 == 0 || (i + 1) % 9 == 0) continue;

            if (k < pagePerks.size()) {
                PerkType type = pagePerks.get(k++);
                ItemStack item = perkRegistry.getPerkItem(type).clone();

                // Update item with player-specific info
                ItemUtil.updateEquipStatus(item, equipped);
                ItemUtil.updateCount(item, owned, type);
                ItemUtil.updateRequirements(item, equipped, type);

                inv.setItem(i, item);
            } else {
                inv.setItem(i, ItemUtil.createItem(Material.BARRIER, "???", null, true));
            }
        }

        // 4. Pagination Buttons
        int totalPages = hiddenMenu ?
                (int) Math.ceil(PerkType.values().length / 14.0) :
                (int) Math.ceil((PerkType.values().length - getPerksByRarity(4).size()) / 14.0);

        if (page >= totalPages - 1) {
            inv.setItem(53, ItemUtil.createItem(
                    Material.PAPER,
                    "§f§lLAST PAGE",
                    null,
                    false,
                    "§7§o" + (page + 1) + "/" + totalPages
            ));
        } else {
            inv.setItem(53, ItemUtil.createItem(
                    Material.GREEN_DYE,
                    "§a§lNEXT",
                    null,
                    false,
                    "§7§o" + (page + 1) + "/" + totalPages
            ));
        }

        if (page == 0) {
            inv.setItem(45, ItemUtil.createItem(
                    Material.PAPER,
                    "§f§lFIRST PAGE",
                    null,
                    false,
                    "§7§o" + (page + 1) + "/" + totalPages
            ));
        } else {
            inv.setItem(45, ItemUtil.createItem(
                    Material.RED_DYE,
                    "§c§lBACK",
                    null,
                    false,
                    "§7§o" + (page + 1) + "/" + totalPages
            ));
        }

        player.openInventory(inv);
    }

    private List<PerkType> getPerksByRarity(int rarity) {
        List<PerkType> perks = new ArrayList<>();
        for (PerkType type : PerkType.values()) {
            if (type.getRarity() == rarity) {
                perks.add(type);
            }
        }
        return perks;
    }

    private List<PerkType> perkTypesByPage(int page) {
        List<PerkType> perks = new ArrayList<>();
        int count = 0, passed = 0;

        for (PerkType type : PerkType.values()) {
            if (type.getRarity() != -1) { // Item is not hidden
                if (passed / 14 == page) {
                    perks.add(type);
                    count++;
                } else {
                    passed++;
                }

                if (count == 14) {
                    break;
                }
            }
        }
        return perks;
    }

}