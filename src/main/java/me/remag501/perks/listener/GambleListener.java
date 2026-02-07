package me.remag501.perks.listener;

import me.remag501.bgscore.api.event.EventService;
import me.remag501.perks.manager.GambleManager;
import me.remag501.perks.ui.PerkMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GambleListener {

    private final GambleManager gambleManager;
    private final PerkMenu perkMenu;

    public GambleListener(EventService eventService, GambleManager gambleManager, PerkMenu perkMenu) {
        this.gambleManager = gambleManager;
        this.perkMenu = perkMenu;

        // Registering via the injected TaskHelper
        eventService.subscribe(InventoryClickEvent.class)
                .filter(event -> event.getView().getTitle().equals("Roll for Perks"))
                .handler(this::handleMenuClick);
    }

    private void handleMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Navigation
        if (clicked.getType() == Material.ARROW) {
            perkMenu.open(player, 0, false);
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