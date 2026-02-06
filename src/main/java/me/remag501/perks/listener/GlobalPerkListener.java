package me.remag501.perks.listener;

import me.remag501.bgscore.api.TaskHelper;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.registry.WorldRegistry;
import me.remag501.perks.service.ItemService;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlobalPerkListener {

    private final PerkManager perkManager;
    private final PerkRegistry perkRegistry;
    private final WorldRegistry worldRegistry;
    private final ItemService itemService;

    public GlobalPerkListener(TaskHelper taskHelper, PerkManager perkManager, PerkRegistry perkRegistry, WorldRegistry worldRegistry, ItemService itemService) {
        this.perkManager = perkManager;
        this.perkRegistry = perkRegistry;
        this.worldRegistry = worldRegistry;
        this.itemService = itemService;

        // 1. Join & Quit
        taskHelper.subscribe(PlayerJoinEvent.class).handler(this::handleJoin);
        taskHelper.subscribe(PlayerQuitEvent.class).handler(this::handleQuit);

        // 2. World Movement & Respawn
        taskHelper.subscribe(PlayerChangedWorldEvent.class).handler(this::handleWorldChange);
        taskHelper.subscribe(PlayerRespawnEvent.class).handler(e -> checkAllowedWorld(e.getPlayer()));

        // 3. Combat & Death
        taskHelper.subscribe(PlayerDeathEvent.class).handler(this::handleDeath);
    }

    private void handleJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        perkManager.handlePlayerJoin(player);
        checkAllowedWorld(player);
    }

    private void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        disablePlayerPerks(player);
        perkManager.handlePlayerQuit(player);
    }

    private void handleWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        checkAllowedWorld(player);

        String worldName = player.getWorld().getName().toLowerCase();
        if (worldRegistry.DISABLED_WORLDS.contains(worldName) || worldName.startsWith(worldRegistry.BUNKER_PREFIX)) {
            handlePerkExtraction(player);
        }
    }

    private void handleDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        disablePlayerPerks(player);

        String worldName = player.getWorld().getName();
        if (!worldRegistry.DROP_WORLDS.contains(worldName) || worldRegistry.DISABLED_WORLDS.contains(worldName)) {
            return;
        }

        processPerkDrop(event, player);
    }

    // --- Internal Logic (The "Machinery") ---

    private void handlePerkExtraction(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<PerkType> collectedPerks = itemService.itemsToPerks(inventory);

        if (collectedPerks.isEmpty()) return;

        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        for (PerkType perkType : collectedPerks) {
            ItemMeta meta = perkRegistry.getPerkItem(perkType).getItemMeta();
            char colorCode = meta.getLore().get(0).charAt(1);
            String itemName = "§" + colorCode + "§l" + meta.getDisplayName();

            player.sendMessage("§aYou have obtained " + itemName);
            profile.addOwnedPerk(perkType);
        }
    }

    private void processPerkDrop(PlayerDeathEvent event, Player player) {
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        Map<PerkType, Integer> equippedPerks = profile.getEquippedPerks();

        if (equippedPerks.isEmpty()) return;

        List<PerkType> equippedList = new ArrayList<>(equippedPerks.keySet());
        PerkType droppedType = equippedList.get((int) (Math.random() * equippedList.size()));
        int stars = equippedPerks.get(droppedType);

        ItemStack perkItem = itemService.getPerkCard(droppedType);
        if (droppedType.isStarPerk()) {
            for (int i = 0; i < stars; i++) {
                event.getDrops().add(perkItem.clone());
            }
        } else {
            event.getDrops().add(perkItem);
        }

        profile.removeOwnedPerk(droppedType);
        String starInfo = droppedType.isStarPerk() ? " §e" + "★".repeat(stars) : "";
        player.sendMessage("§cYou have lost the perk " + perkItem.getItemMeta().getDisplayName() + starInfo);
    }

    private void checkAllowedWorld(Player player) {
        String name = player.getWorld().getName().toLowerCase();
        if (worldRegistry.DISABLED_WORLDS.contains(name) || name.startsWith(worldRegistry.BUNKER_PREFIX)) {
            disablePlayerPerks(player);
        } else {
            enablePlayerPerks(player);
        }
    }

    private void disablePlayerPerks(Player player) {
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        profile.getEquippedPerks().forEach((type, stars) -> {
            perkRegistry.getPerk(type).onDisable(player);
        });
    }

    private void enablePlayerPerks(Player player) {
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        profile.getEquippedPerks().forEach((type, stars) -> {
            perkRegistry.getPerk(type).onEnable(player, stars);
        });
    }
}