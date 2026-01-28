package me.remag501.perks.listeners;

import me.remag501.perks.perktypes.Perk;
import me.remag501.perks.perktypes.PerkType;
import me.remag501.perks.manager.PlayerPerks;
import me.remag501.perks.utils.Items;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PerkChangeListener implements Listener {

    public final static List<String> dropWorlds = new ArrayList<>();
    public final static List<String> disabledWorlds = new ArrayList<>();

    private final static String BUNKER_PREFIX = "bunker";


    public PerkChangeListener() {

    }

    private void checkAllowedWorld(Player player) {
        String newWorld = player.getWorld().getName().toLowerCase();

        // Check does not allow the world allows perks
        if (disabledWorlds.contains(newWorld) || newWorld.startsWith(BUNKER_PREFIX)) {
            // Disable player's perks
//            player.sendMessage("Perks disabled");
            disablePlayerPerks(player);
        } else {
            // Re-enable player's perks
//            player.sendMessage("Perks enabled");
            enablePlayerPerks(player);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
//        player.sendMessage("Perks: reached1");
        checkAllowedWorld(player);
        // Convert perk cards into player perks
        String worldName = player.getWorld().getName().toLowerCase();
        if (disabledWorlds.contains(worldName) || worldName.startsWith(BUNKER_PREFIX)) {
            PlayerInventory inventory = player.getInventory();
            List<PerkType> collectedPerks = Items.itemsToPerks(inventory); // Get perks, and removes perk cards
            if (collectedPerks.isEmpty())
                return; // Player extracted no perks
            // Give perks to player
            PlayerPerks playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());
            for (PerkType perkType: collectedPerks) {
                // Create colored string for perk name
                ItemMeta itemMeta = perkType.getItem().getItemMeta();
                String firstLine = itemMeta.getLore().get(0);
                char colorCode = firstLine.charAt(1);
                String itemName = "§" + colorCode + "§l" + itemMeta.getDisplayName();
                player.sendMessage("§aYou have obtained " + itemName); // Notify player
                playerPerks.addOwnedPerks(perkType);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Turn off perk
        disablePlayerPerks(event.getEntity());
        // Player loses on perk at random
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();

        if (!dropWorlds.contains(worldName)) { // Player is not in a drop perk world so they keep it

            return;
        }

        if (!disabledWorlds.contains(worldName)) { // Player died in region where they lose perk
            // Pick a random perk to drop
            PlayerPerks playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());
            List<Perk> equippedPerks = playerPerks.getEquippedPerks();
            if (equippedPerks.size() == 0)
                return; // Player has no perks equipped, they lose nothing
            int droppedIndex = (int) (Math.random() * equippedPerks.size());
            Perk dropped = equippedPerks.get(droppedIndex);
            // Convert to item and put in itemstack
            ItemStack perkItem = Items.getPerkCard(PerkType.getPerkType(dropped));
            List<ItemStack> drops = event.getDrops();
            drops.add(perkItem);
            // Remove perk from equipped perks
            playerPerks.removeOwnedPerk(PerkType.getPerkType(dropped));
            if (dropped.isStarPerk()) { // Drop all star perks
                int stars = dropped.getStars();
                for (int i = 0; i < stars; i++) { // removeOwnedPerks, already takes away a star
                    player.sendMessage("reached");
                    playerPerks.removeOwnedPerk(PerkType.getPerkType(dropped));
                    drops.add(perkItem);
                }
            }
            // Message the player
            player.sendMessage("§cYou have lost the perk " + perkItem.getItemMeta().getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        checkAllowedWorld(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if player has perks loaded
        Player player = event.getPlayer();
        PlayerPerks playerPerks = PlayerPerks.getPlayerPerks(player.getUniqueId());
        if (playerPerks == null)
            new PlayerPerks(((Player) player).getUniqueId());
        // Check if player can enable their perks
        checkAllowedWorld(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        disablePlayerPerks(event.getPlayer());
    }

    private void disablePlayerPerks(Player player) {
//        player.sendMessage("Perks are disabled in this world.");
        // Loop through the player's active perks and disable them
        for (Perk perk : getPlayerActivePerks(player)) {
            perk.onDisable();
        }
    }

    private void enablePlayerPerks(Player player) {
//        player.sendMessage("Perks are enabled in this world.");
        // Loop through the player's active perks and enable them
        for (Perk perk : getPlayerActivePerks(player)) {
//            player.sendMessage("reached " + perk.getItem().getItemMeta().getDisplayName());
            perk.onEnable();
        }
    }

    private List<Perk> getPlayerActivePerks(Player player) {
        return PlayerPerks.getPlayerPerks(player.getUniqueId()).getEquippedPerks();
    }
}
