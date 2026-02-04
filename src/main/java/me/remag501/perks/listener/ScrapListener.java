package me.remag501.perks.listener;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.ui.PerkMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ScrapListener implements Listener {

    private final PerkManager perkManager;
    private final PerkRegistry perkRegistry;
    private final PerkMenu perkMenu;

    public ScrapListener(PerkManager perkManager, PerkRegistry perkRegistry, PerkMenu perkMenu) {
        this.perkManager = perkManager;
        this.perkRegistry = perkRegistry;
        this.perkMenu = perkMenu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Confirm Scrap")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        PerkProfile perkProfile = perkManager.getProfile(player.getUniqueId());
        // We retrieve the perk the player intended to scrap from the manager's cache
        PerkType pending = perkProfile.getPendingScrap();

        if (clicked.getType() == Material.GREEN_WOOL) {
            if (pending != null) {
                int points = perkProfile.scrapPerk(pending);
                player.sendMessage("§6§lPERKS §8» §7Scrapped " + perkRegistry.getPerkItem(pending).getItemMeta().getDisplayName() + " §7for " + points + " points!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
            }
        } else if (clicked.getType() == Material.RED_WOOL) {
            player.sendMessage("§6§lPERKS §8» §7Scrapping cancelled.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        } else {
            return; // Ignore other clicks
        }

        // Cleanup and return to main menu
        perkProfile.setPendingScrap(null);
        perkMenu.open(player, 0, false);
    }
}