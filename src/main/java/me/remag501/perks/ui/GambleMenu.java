package me.remag501.perks.ui;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GambleMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Roll for Perks");

        PerkManager perkManager = PerkManager.getPlayerPerks(player.getUniqueId());
        int points = perkManager.getPerkPoints();

        // Build Buttons
        inv.setItem(10, ItemUtil.createItem(Material.WHITE_STAINED_GLASS_PANE, "§f§lCOMMON", "common", true, "§7Costs 2 out of " + points + " perk points."));
        inv.setItem(12, ItemUtil.createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§lUNCOMMON", "uncommon", true, "§7Costs 4 out of " + points + " perk points."));
        inv.setItem(14, ItemUtil.createItem(Material.BLUE_STAINED_GLASS_PANE, "§1§lRARE", "rare", true, "§7Costs 7 out of " + points + " perk points."));
        inv.setItem(16, ItemUtil.createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6§lLEGENDARY", "legendary", true, "§7Costs 10 out of " + points + " perk points."));

        // Back Arrow
        ItemStack backArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = backArrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c← Back");
            backArrow.setItemMeta(meta);
        }
        inv.setItem(0, backArrow);

        player.openInventory(inv);
    }
}