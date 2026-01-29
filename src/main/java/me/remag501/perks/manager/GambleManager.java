package me.remag501.perks.manager;

import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GambleManager {

    public static void openInventory(Player player) {

        Inventory rollInventory = Bukkit.createInventory(null, 27, "Roll for Perks");

        PerkManager perkManager = PerkManager.getPlayerPerks(player.getUniqueId());
        int perkPoints = perkManager.getPerkPoints();
        ItemStack commonButton = ItemUtil.createItem(Material.WHITE_STAINED_GLASS_PANE, "§f§lCOMMON", "common", true, "§7Costs 2 out of " + perkPoints + " perk points.");
        ItemStack uncommonButton = ItemUtil.createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§lUNCOMMON", "uncommon", true, "§7Costs 4 out of " + perkPoints + " perk points.");
        ItemStack rareButton = ItemUtil.createItem(Material.BLUE_STAINED_GLASS_PANE, "§1§lRARE", "rare", true, "§7Costs 7 out of " + perkPoints + " perk points.");
        ItemStack legendaryButton = ItemUtil.createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6§lLEGENDARY", "legendary", true, "§7Costs 10 out of " + perkPoints + " perk points.");
        // Set Locations
        rollInventory.setItem(10, commonButton);
        rollInventory.setItem(12, uncommonButton);
        rollInventory.setItem(14, rareButton);
        rollInventory.setItem(16, legendaryButton);
        // Add back arrow at slot 18
        ItemStack backArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = backArrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c← Back");
            backArrow.setItemMeta(meta);
        }
        rollInventory.setItem(0, backArrow);

        player.openInventory(rollInventory);

    }


}
