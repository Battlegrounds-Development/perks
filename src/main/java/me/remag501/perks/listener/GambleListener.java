package me.remag501.perks.listener;

import me.remag501.perks.manager.GambleManager;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.ui.GambleMenu;
import me.remag501.perks.ui.PerkMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GambleListener implements Listener {

    private final GambleManager gambleManager;

    public GambleListener(GambleManager gambleManager) {
        this.gambleManager = gambleManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Roll for Perks")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Navigation
        if (clicked.getType() == Material.ARROW) {
            // Here you would call PerkMenu.open(player) once that's refactored
            PerkMenu.open(player, 0, false);
            return;
        }

        // Gamble Logic
        String name = clicked.getItemMeta().getDisplayName();
        switch (name) {
            case "§f§lCOMMON" -> gambleManager.rollPerk(player, 0, 2);
            case "§a§lUNCOMMON" -> gambleManager.rollPerk(player, 1, 4);
            case "§1§lRARE" -> gambleManager.rollPerk(player, 2, 7);
            case "§6§lLEGENDARY" -> gambleManager.rollPerk(player, 3, 10);
        }
    }
}