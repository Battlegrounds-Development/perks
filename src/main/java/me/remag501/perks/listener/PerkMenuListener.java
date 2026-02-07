package me.remag501.perks.listener;

import me.remag501.bgscore.api.event.EventService;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.ui.GambleMenu;
import me.remag501.perks.ui.PerkMenu;
import me.remag501.perks.ui.ScrapMenu;
import me.remag501.perks.service.ItemService;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PerkMenuListener {

    private final PerkManager perkManager;
    private final PerkMenu perkMenu;
    private final GambleMenu gambleMenu;
    private final ScrapMenu scrapMenu;
    private final ItemService itemService;

    public PerkMenuListener(EventService eventService, PerkManager perkManager, PerkMenu perkMenu, GambleMenu gambleMenu, ItemService itemService, ScrapMenu scrapMenu) {
        this.perkManager = perkManager;
        this.perkMenu = perkMenu;
        this.gambleMenu = gambleMenu;
        this.scrapMenu = scrapMenu;
        this.itemService = itemService;

        // Register the primary subscription
        eventService.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getView().getTitle().equals("Choose Your Perk"))
                .handler(this::handleMenuClick);
    }

    private void handleMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // 1. Get Context
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        int currentPage = getPageFromLore(event.getInventory().getItem(53));
        String name = clicked.getItemMeta().getDisplayName();

        // 2. Navigation & Static Buttons
        if (clicked.getType() == Material.BEDROCK) {
            player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 10, 0);
            return;
        }

        if (name.equals("§6§lOBTAIN PERKS")) {
            gambleMenu.open(player);
            return;
        }

        if (handlePagination(player, name, clicked, currentPage)) return;

        // 3. Perk Specific Logic
        handlePerkInteraction(player, clicked, profile, currentPage, event.getClick());
    }

    private boolean handlePagination(Player player, String name, ItemStack clicked, int currentPage) {
        if (name.equals("§a§lNEXT")) {
            perkMenu.open(player, currentPage + 1, itemService.hiddenItem(clicked));
            return true;
        } else if (name.equals("§c§lBACK")) {
            perkMenu.open(player, currentPage - 1, itemService.hiddenItem(clicked));
            return true;
        }
        return false;
    }

    private void handlePerkInteraction(Player player, ItemStack clicked, PerkProfile profile, int currentPage, ClickType clickType) {
        String perkId = itemService.getPerkID(clicked);
        if (perkId == null) return;

        try {
            PerkType type = PerkType.valueOf(perkId.toUpperCase());
            boolean isHidden = itemService.hiddenItem(clicked);

            if (clickType == ClickType.LEFT) {
                boolean success = profile.equipPerk(type, player);
                player.playSound(player, success ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.ENTITY_VILLAGER_NO, 10, success ? 2 : 1);
            } else if (clickType == ClickType.RIGHT) {
                if (profile.unequipPerk(type, player)) {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 10, 2);
                } else {
                    profile.setPendingScrap(type);
                    scrapMenu.open(player);
                    return; // Don't refresh if opening new menu
                }
            }
            perkMenu.open(player, currentPage, isHidden);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cError: Invalid Perk Data.");
        }
    }

    private int getPageFromLore(ItemStack pager) {
        if (pager == null || !pager.hasItemMeta()) return 0;
        try {
            return Integer.parseInt(pager.getItemMeta().getLore().get(0).split("/")[0].replace("§7§o", "")) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}