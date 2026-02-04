package me.remag501.perks.listener;

import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
//import me.remag501.perks.ui.GambleMenu;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.ui.GambleMenu;
import me.remag501.perks.ui.PerkMenu;
import me.remag501.perks.ui.ScrapMenu;
import me.remag501.perks.service.ItemService;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PerkMenuListener implements Listener {

    private final PerkManager perkManager;
    private final PerkMenu perkMenu;
    private final GambleMenu gambleMenu;
    private final ScrapMenu scrapMenu;
    private final ItemService itemService;

    public PerkMenuListener(PerkManager perkManager, PerkMenu perkMenu, GambleMenu gambleMenu, ItemService itemService, ScrapMenu scrapMenu) {
        this.perkManager = perkManager;
        this.perkMenu = perkMenu;
        this.gambleMenu = gambleMenu;
        this.scrapMenu = scrapMenu;
        this.itemService = itemService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Choose Your Perk")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        PerkProfile profile = perkManager.getProfile(player.getUniqueId());

        // 1. Extract current page from the 'Next' or 'Back' button lore
        ItemStack pager = event.getInventory().getItem(53);
        int currentPage = Integer.parseInt(pager.getItemMeta().getLore().get(0).split("/")[0].replace("§7§o", "")) - 1;

        // 2. Handle Static Buttons
        String name = clicked.getItemMeta().getDisplayName();
        if (clicked.getType() == Material.BEDROCK) {
            player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 10, 0);
            return;
        }

        if (name.equals("§6§lOBTAIN PERKS")) {
            gambleMenu.open(player);
            return;
        } else if (name.equals("§a§lNEXT")) {
            perkMenu.open(player, currentPage + 1, itemService.hiddenItem(clicked));
            return;
        } else if (name.equals("§c§lBACK")) {
            perkMenu.open(player, currentPage - 1, itemService.hiddenItem(clicked));
            return;
        }

        // 3. Handle Perk Interaction
        String perkId = itemService.getPerkID(clicked);

        if (perkId != null) {
            try {
                // Direct conversion from ID to Enum
                PerkType type = PerkType.valueOf(perkId.toUpperCase());
                boolean isHidden = itemService.hiddenItem(clicked);

                if (event.getClick() == ClickType.LEFT) {
                    if (profile.equipPerk(type, player)) {
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
                    } else {
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 10, 1);
                    }
                } else if (event.getClick() == ClickType.RIGHT) {
                    if (profile.unequipPerk(type, player)) {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 10, 2);
                    } else {
                        profile.setPendingScrap(type);
                        scrapMenu.open(player);
                        return;
                    }
                }

                // Refresh the current UI
                perkMenu.open(player, currentPage, isHidden);

            } catch (IllegalArgumentException e) {
                // This would only happen if the ID in the NBT doesn't match an Enum value
                player.sendMessage("§cError: Invalid Perk Data.");
            }
        }

    }
}