package me.remag501.perks.listener;

import me.remag501.bgscore.api.event.EventService;
import me.remag501.bgscore.api.util.BGSColor;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.ui.PerkMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ScrapListener {

    private final PerkManager perkManager;
    private final PerkRegistry perkRegistry;
    private final PerkMenu perkMenu;

    public ScrapListener(EventService eventService, PerkManager perkManager, PerkRegistry perkRegistry, PerkMenu perkMenu) {
        this.perkManager = perkManager;
        this.perkRegistry = perkRegistry;
        this.perkMenu = perkMenu;

        eventService.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getView().getTitle().equals("Confirm Scrap"))
                .handler(this::handleScrapClick);
    }

    private void handleScrapClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        PerkType pending = profile.getPendingScrap();

        if (clicked.getType() == Material.GREEN_WOOL) {
            handleConfirm(player, profile, pending);
        } else if (clicked.getType() == Material.RED_WOOL) {
            handleCancel(player);
        } else {
            return;
        }

        // Common Cleanup
        profile.setPendingScrap(null);
        perkMenu.open(player, 0, false);
    }

    private void handleConfirm(Player player, PerkProfile profile, PerkType pending) {
        if (pending == null) return;

        int points = profile.scrapPerk(pending);
        String perkName = perkRegistry.getPerkItem(pending).getItemMeta().getDisplayName();

        player.sendMessage(BGSColor.PREFIX_PERKS + "Scrapped " + perkName + " §7for " + points + " points!");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    private void handleCancel(Player player) {
        player.sendMessage(BGSColor.PREFIX_PERKS + "Scrapping cancelled.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }
}