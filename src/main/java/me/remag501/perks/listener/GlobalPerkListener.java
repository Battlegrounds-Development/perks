package me.remag501.perks.listener;

import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.util.ItemUtil;
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
import java.util.Map;

import static me.remag501.perks.util.WorldUtil.*;

public class GlobalPerkListener implements Listener {


    private void checkAllowedWorld(Player player) {
        String newWorld = player.getWorld().getName().toLowerCase();

        // Check if the world allows perks
        if (DISABLED_WORLDS.contains(newWorld) || newWorld.startsWith(BUNKER_PREFIX)) {
            // Disable player's perks
            disablePlayerPerks(player);
        } else {
            // Re-enable player's perks
            enablePlayerPerks(player);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        checkAllowedWorld(player);

        // Convert perk cards into player perks
        String worldName = player.getWorld().getName().toLowerCase();
        if (DISABLED_WORLDS.contains(worldName) || worldName.startsWith(BUNKER_PREFIX)) {
            PlayerInventory inventory = player.getInventory();
            List<PerkType> collectedPerks = ItemUtil.itemsToPerks(inventory); // Get perks, and removes perk cards

            if (collectedPerks.isEmpty()) {
                return; // Player extracted no perks
            }

            // Give perks to player
            PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());
            for (PerkType perkType : collectedPerks) {
                // Create colored string for perk name
                ItemMeta itemMeta = perkType.getItem().getItemMeta();
                String firstLine = itemMeta.getLore().get(0);
                char colorCode = firstLine.charAt(1);
                String itemName = "§" + colorCode + "§l" + itemMeta.getDisplayName();

                player.sendMessage("§aYou have obtained " + itemName); // Notify player
                profile.addOwnedPerk(perkType);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Turn off perks
        disablePlayerPerks(event.getEntity());

        Player player = event.getEntity();
        String worldName = player.getWorld().getName();

        if (!DROP_WORLDS.contains(worldName)) {
            // Player is not in a drop perk world so they keep it
            return;
        }

        if (!DISABLED_WORLDS.contains(worldName)) {
            // Player died in region where they lose perk
            PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());
            Map<PerkType, Integer> equippedPerks = profile.getEquippedPerks();

            if (equippedPerks.isEmpty()) {
                return; // Player has no perks equipped, they lose nothing
            }

            // Pick a random perk to drop
            List<PerkType> equippedList = new ArrayList<>(equippedPerks.keySet());
            int droppedIndex = (int) (Math.random() * equippedList.size());
            PerkType droppedType = equippedList.get(droppedIndex);
            int stars = equippedPerks.get(droppedType);

            // Convert to item and put in drops
            ItemStack perkItem = ItemUtil.getPerkCard(droppedType);
            List<ItemStack> drops = event.getDrops();

            if (droppedType.isStarPerk()) {
                // Drop all star perks
                for (int i = 0; i < stars; i++) {
                    drops.add(perkItem.clone());
                    profile.removeOwnedPerk(droppedType);
                }
            } else {
                drops.add(perkItem);
                profile.removeOwnedPerk(droppedType);
            }

            // Message the player
            String starInfo = droppedType.isStarPerk() ? " §e" + "★".repeat(stars) : "";
            player.sendMessage("§cYou have lost the perk " + perkItem.getItemMeta().getDisplayName() + starInfo);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        checkAllowedWorld(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player's perks
        PerkManager.getInstance().handlePlayerJoin(player);

        // Check if player can enable their perks
        checkAllowedWorld(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Disable perks first
        disablePlayerPerks(player);

        // Save and cleanup
        PerkManager.getInstance().handlePlayerQuit(player);
    }

    private void disablePlayerPerks(Player player) {
        PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());
        Map<PerkType, Integer> equippedPerks = profile.getEquippedPerks();

        // Disable each equipped perk
        for (Map.Entry<PerkType, Integer> entry : equippedPerks.entrySet()) {
            PerkType type = entry.getKey();
            int stars = entry.getValue();

            type.getPerk().onDisable(player);
        }
    }

    private void enablePlayerPerks(Player player) {
        PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());
        Map<PerkType, Integer> equippedPerks = profile.getEquippedPerks();

        // Enable each equipped perk with correct star count
        for (Map.Entry<PerkType, Integer> entry : equippedPerks.entrySet()) {
            PerkType type = entry.getKey();
            int stars = entry.getValue();

            type.getPerk().onEnable(player, stars);
        }
    }
}