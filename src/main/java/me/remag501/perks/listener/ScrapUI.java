package me.remag501.perks.listener;

import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PlayerPerks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class ScrapUI implements Listener {
    private final Inventory scrapInventory;
    private static HashMap<UUID, PerkType> scrapPerkCache;

    public ScrapUI() {
        this.scrapInventory = Bukkit.createInventory(null, 9, "Confirm Scrap");
        loadItems();
        scrapPerkCache = new HashMap<UUID, PerkType>();
    }

    private void loadItems() {
        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        assert confirmMeta != null;
        confirmMeta.setDisplayName("§a§lCONFIRM");
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        assert cancelMeta != null;
        cancelMeta.setDisplayName("§c§lCANCEL");
        cancel.setItemMeta(cancelMeta);

        scrapInventory.setItem(3, cancel);
        scrapInventory.setItem(5, confirm);
    }

    public void open(Player player, PerkType perkType) {
        scrapPerkCache.put(player.getUniqueId(), perkType); // Save for event handler
        player.openInventory(scrapInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Confirm Scrap")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        // Get the perkType getting scrapped and player perk instance
        PerkType perkType = scrapPerkCache.get(player.getUniqueId());
        PlayerPerks playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());

        switch (clickedItem.getType()) {
            case GREEN_WOOL: // Confirm
                int points = playerPerks.scrapPerks(perkType);
                player.sendMessage("§6§lPERKS §8» §7You have scrapped the perk " + perkType.getItem().getItemMeta().getDisplayName() + " §7for " + points + " points!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                break;
            case RED_WOOL: // Cancel
                player.sendMessage("§6§lPERKS §8» §7Scrapping cancelled.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                break;
            default:
                return;
        }

        // Return to previous UI
        UI ui = new UI(PlayerPerks.getPlayerPerks(player.getUniqueId()), false);
        Inventory perkMenu = ui.getPerkMenu();
        player.openInventory(perkMenu);
    }
}
