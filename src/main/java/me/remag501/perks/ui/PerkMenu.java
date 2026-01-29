package me.remag501.perks.ui;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PerkMenu {

    public static void open(Player player, int page, boolean hiddenMenu) {
        PerkManager perks = PerkManager.getPlayerPerks(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, "Choose Your Perk");

        // 1. Load Header
        ItemStack head = ItemUtil.createPerkSkull(player.getUniqueId(), player.getDisplayName(), player.getDisplayName());
        inv.setItem(0, head);
        ItemStack rollButton = ItemUtil.createItem(Material.SUNFLOWER, "§6§lOBTAIN PERKS", "casino", false, "§7Perk Points: " + perks.getPerkPoints());
        inv.setItem(8, rollButton);

        // 2. Load Active Perks (Slots 2-6)
        List<Perk> equipped = perks.getEquippedPerks();
        int equippedSize = (equipped == null) ? 0 : equipped.size();
        for (int i = 0; i < 5; i++) {
            if (i < equippedSize) {
                ItemStack item = equipped.get(i).getItem().clone();
                ItemUtil.updateStarCount(item, equipped);
                inv.setItem(2 + i, item);
            } else {
                inv.setItem(2 + i, new ItemStack(Material.AIR));
            }
        }

        // 3. Load Available Perks (Middle Grid)
        List<Perk> owned = perks.getOwnedPerks();
        List<PerkType> pagePerks = PerkType.perkTypesByPage(page);
        for (int i = 19, k = 0; i < 35; i++) {
            if (i % 9 == 0 || (i + 1) % 9 == 0) continue;
            if (k < pagePerks.size()) {
                PerkType type = pagePerks.get(k++);
                ItemStack item = type.getItem().clone();
                ItemUtil.updateEquipStatus(item, equipped);
                ItemUtil.updateCount(item, owned);
                ItemUtil.updateRequirements(item, equipped, type);
                inv.setItem(i, item);
            } else {
                inv.setItem(i, ItemUtil.createItem(Material.BARRIER, "???", null, true));
            }
        }

        // 4. Pagination Buttons
        int totalPages = hiddenMenu ? (int) Math.ceil(PerkType.values().length / 14.0) :
                (int) Math.ceil((PerkType.values().length - PerkType.getPerksByRarity(4).size()) / 14.0);

        if (page >= totalPages - 1)
            inv.setItem(53, ItemUtil.createItem(Material.PAPER, "§f§lLAST PAGE", null, false, "§7§o" + (page + 1) + "/" + totalPages));
        else
            inv.setItem(53, ItemUtil.createItem(Material.GREEN_DYE, "§a§lNEXT", null, false, "§7§o" + (page + 1) + "/" + totalPages));

        if (page == 0)
            inv.setItem(45, ItemUtil.createItem(Material.PAPER, "§f§lFIRST PAGE", null, false, "§7§o" + (page + 1) + "/" + totalPages));
        else
            inv.setItem(45, ItemUtil.createItem(Material.RED_DYE, "§c§lBACK", null, false, "§7§o" + (page + 1) + "/" + totalPages));

        player.openInventory(inv);
    }
}