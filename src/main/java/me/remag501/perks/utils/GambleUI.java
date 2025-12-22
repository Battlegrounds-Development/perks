package me.remag501.perks.utils;

import me.remag501.perks.core.PerkType;
import me.remag501.perks.core.PlayerPerks;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GambleUI implements Listener {

//    private Player player;
    private Inventory rollInventory;
//    private PlayerPerks playerPerks;

    public GambleUI() {
//        this.player = player;
//        this.playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());
        rollInventory = Bukkit.createInventory(null, 27, "Roll for Perks");
    }

    private void loadRollItems(Player player) {
//        for (int i = 0; i < 27; i++) {
//            rollInventory.setItem(i, new ItemStack(Material.PAPER)); // Example items for rolling
//        }
        PlayerPerks playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());
        int perkPoints = playerPerks.getPerkPoints();
        ItemStack commonButton = Items.createItem(Material.WHITE_STAINED_GLASS_PANE, "§f§lCOMMON", "common", true, "§7Costs 2 out of " + perkPoints + " perk points.");
        ItemStack uncommonButton = Items.createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§lUNCOMMON", "uncommon", true, "§7Costs 4 out of " + perkPoints + " perk points.");
        ItemStack rareButton = Items.createItem(Material.BLUE_STAINED_GLASS_PANE, "§1§lRARE", "rare", true, "§7Costs 7 out of " + perkPoints + " perk points.");
        ItemStack legendaryButton = Items.createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6§lLEGENDARY", "legendary", true, "§7Costs 10 out of " + perkPoints + " perk points.");
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
    }

    public void open(Player player) {
        loadRollItems(player);
        player.openInventory(rollInventory);
    }

    private void prevUI(Player player) {
        UI ui = new UI(PlayerPerks.getPlayerPerks(player.getUniqueId()), false);
        Inventory perkMenu = ui.getPerkMenu();
        player.openInventory(perkMenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Roll for Perks")) {
            event.setCancelled(true); // Prevent taking items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null) {
                if (clickedItem.getType() == Material.ARROW) {
                    prevUI(((Player) event.getWhoClicked()).getPlayer());
                    return;
                }
                switch (clickedItem.getItemMeta().getDisplayName()) {
                    case "COMMON":
                        rollPerk(player, 0, 2);
                        break;
                    case "UNCOMMON":
                        rollPerk(player, 1, 4);
                        break;
                    case "RARE":
                        rollPerk(player, 2, 7);
                        break;
                    case "LEGENDARY":
                        rollPerk(player, 3, 10);
                        break;
                    default:
                        break;
                }
                rollInventory = event.getInventory();
                loadRollItems(player); // Update UI
            }
        }
    }

    public void triggerTotemAnimation(Player player, PerkType perkType) {
        // Close the player's inventory
        player.closeInventory();

        // Create a custom totem with model data 123
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();

        // Get cmd from perktype
        int customModelData = perkType.getItem().getItemMeta().getCustomModelData();

        if (meta != null) {
//            meta.setDisplayName(ChatColor.GOLD + "Custom Perk Totem");
            meta.setCustomModelData(customModelData); // Assign custom model data

            // Add lore to the item
//            List<String> lore = new ArrayList<>();
//            lore.add(ChatColor.GRAY + "A rare totem infused with perk energy.");
//            meta.setLore(lore);

            // Store custom NBT data for resource pack mapping
//            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "perk_totem");
//            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "perk_totem_data");

            totem.setItemMeta(meta);
        }

        // Cache the players on hand
        ItemStack cache = player.getInventory().getItemInMainHand();
        int slot = player.getInventory().getHeldItemSlot();

        // Add the totem to the player’s inventory
        player.getInventory().setItem(slot, totem);
        player.playEffect(EntityEffect.TOTEM_RESURRECT); // Trigger animation
        player.getInventory().setItem(slot, cache);


        // ✅ Play the totem sound
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

//        player.sendMessage(ChatColor.GREEN + "You rolled a custom perk!");

        // ✅ Reopen the inventory after 3 seconds
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Perks"), () -> {
            // Open the perk UI after delay
            GambleUI rollUI = new GambleUI();
            rollUI.open(player);
        }, 45L); // 60 ticks = 3 seconds
    }




    private boolean rollPerk(Player player, int rarity, int cost) {
        // Charge player perks, if it fails then it won't roll
        PlayerPerks playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());
        if (!playerPerks.decreasePerkPoints(cost))
            return false; // Not enough points
        // Determine whether player rolls higher rarity
        int firstRoll = (int) (Math.random() * 100) + 1;
        if (firstRoll > 95)
            rarity += 2;
        else if (firstRoll > 80)
            rarity++;
        rarity = (rarity < 4) ? rarity : 3; // Floor rarity to 3
        // Find the possible perks
        List<PerkType> possiblePerks = PerkType.getPerksByRarity(rarity);
        // Roll random perk and add to user
        int randomIndex = (int) (Math.random() * possiblePerks.size());
        PerkType perkType = possiblePerks.get(randomIndex);
        // Animation for rolling perk
        triggerTotemAnimation(player, perkType);
        // Add perk to player
        playerPerks.addOwnedPerks(perkType);
        player.sendMessage("You obtained the perk: " + perkType.getItem().getItemMeta().getDisplayName());
        return true;
    }
}
