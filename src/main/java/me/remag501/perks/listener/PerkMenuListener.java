package me.remag501.perks.listener;

import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
//import me.remag501.perks.ui.GambleMenu;
import me.remag501.perks.ui.PerkMenu;
//import me.remag501.perks.ui.ScrapMenu;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PerkMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Choose Your Perk")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());

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
            // GambleMenu.open(player);
            player.sendMessage("§cGamble menu not yet implemented!");
            return;
        } else if (name.equals("§a§lNEXT")) {
            PerkMenu.open(player, currentPage + 1, ItemUtil.hiddenItem(clicked));
            return;
        } else if (name.equals("§c§lBACK")) {
            PerkMenu.open(player, currentPage - 1, ItemUtil.hiddenItem(clicked));
            return;
        }

        // 3. Handle Perk Interaction
        for (PerkType type : PerkType.values()) {
            if (ItemUtil.areItemsEqual(clicked, type.getItem())) {
                boolean isHidden = ItemUtil.hiddenItem(clicked);

                if (event.getClick() == ClickType.LEFT) {
                    // Left click: Equip perk
                    if (profile.equipPerk(type, player)) {
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
                    } else {
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 10, 1);
                    }
                } else if (event.getClick() == ClickType.RIGHT) {
                    // Right click: Unequip perk (or open scrap menu if not equipped)
                    if (profile.unequipPerk(type, player)) {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 10, 2);
                    } else {
                        // Perk not equipped, open scrap menu
                        profile.setPendingScrap(type);
                        // ScrapMenu.open(player);
                        player.sendMessage("§cScrap menu not yet implemented!");
                        return; // Don't refresh PerkMenu yet
                    }
                }

                // Refresh the current UI
                PerkMenu.open(player, currentPage, isHidden);
                break;
            }
        }
    }
}