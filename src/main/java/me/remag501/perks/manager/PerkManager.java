package me.remag501.perks.manager;

import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.registry.WorldRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import java.util.*;
import java.util.logging.Logger;

/**
 * Singleton manager for all player perk profiles.
 * Clean implementation without PerkInstance - works directly with PerkProfile.
 */
public class PerkManager {

    private final Map<UUID, PerkProfile> profiles;
    private final Logger logger; // Used for .logger
    private final ConfigManager perkConfigManager;
    private final PerkRegistry perkRegistry;
    private final WorldRegistry worldRegistry;

    public PerkManager(Logger logger, PerkRegistry perkRegistry, WorldRegistry worldRegistry, ConfigManager perkConfigManager) {
        this.logger = logger;
        this.perkRegistry = perkRegistry;
        this.worldRegistry = worldRegistry;
        this.perkConfigManager = perkConfigManager;
        this.profiles = new HashMap<>();
    }

    /**
     * Get or create a player's perk profile.
     */
    public PerkProfile getProfile(UUID playerUUID) {
        // k represents the playerUUID being passed into the map
        return profiles.computeIfAbsent(playerUUID, k -> new PerkProfile(this.perkRegistry, this.worldRegistry, k));
    }

    /**
     * Remove a player's profile (e.g., on logout).
     */
    public void removeProfile(UUID playerUUID) {
        profiles.remove(playerUUID);
    }

    /**
     * Check if a profile exists.
     */
    public boolean hasProfile(UUID playerUUID) {
        return profiles.containsKey(playerUUID);
    }

    /**
     * Load a player's perks from config.
     */
    public void loadPerks(Player player) {
        UUID playerUUID = player.getUniqueId();
        PerkProfile profile = getProfile(playerUUID);

        String playerID = playerUUID.toString();
        FileConfiguration config = perkConfigManager.getConfig();

        List<String> equippedList = config.getStringList(playerID + "_equipped");
        List<String> ownedList = config.getStringList(playerID + "_owned");

        if (!ownedList.isEmpty()) {
            // Last item is perk points
            String perkPointString = ownedList.remove(ownedList.size() - 1);
            try {
                int points = Integer.parseInt(perkPointString);
                profile.addPerkPoints(points);
            } catch (NumberFormatException e) {
                logger.warning("Invalid perk points for player " + playerUUID);
            }
        }

        // Load owned perks first
        for (String perkId : ownedList) {
            try {
                PerkType type = PerkType.valueOf(perkId);
                profile.addOwnedPerk(type);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid perk type in owned perks: " + perkId);
            }
        }

        // Load equipped perks (with star counting)
        Map<PerkType, Integer> starCounts = new HashMap<>();
        for (String perkId : equippedList) {
            try {
                PerkType type = PerkType.valueOf(perkId);
                starCounts.put(type, starCounts.getOrDefault(type, 0) + 1);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid perk type in equipped perks: " + perkId);
            }
        }

        // Equip perks with correct star counts
        for (Map.Entry<PerkType, Integer> entry : starCounts.entrySet()) {
            PerkType type = entry.getKey();
            int stars = entry.getValue();

            // Equip the perk
            profile.equipPerk(type, player);

            // If it's a star perk, equip additional times for stars
            if (type.isStarPerk() && stars > 1) {
                for (int i = 1; i < stars && i < 3; i++) {
                    profile.equipPerk(type, player);
                }
            }
        }
    }

    /**
     * Save all player perks to config.
     */
    public void saveAllPerks() {

        FileConfiguration config = perkConfigManager.getConfig();

        for (Map.Entry<UUID, PerkProfile> entry : profiles.entrySet()) {
            UUID playerUUID = entry.getKey();
            PerkProfile profile = entry.getValue();
            saveProfileToConfig(config, playerUUID, profile);
        }

        perkConfigManager.save();
    }

    /**
     * Save a specific player's perks.
     */
    public void savePlayerPerks(UUID playerUUID) {
        PerkProfile profile = profiles.get(playerUUID);
        if (profile == null) {
            return;
        }

        FileConfiguration config = perkConfigManager.getConfig();

        saveProfileToConfig(config, playerUUID, profile);
        perkConfigManager.save();
    }

    /**
     * Helper method to save a profile to config.
     */
    private void saveProfileToConfig(FileConfiguration config, UUID playerUUID, PerkProfile profile) {
        String playerID = playerUUID.toString();

        // Save equipped perks (with stars)
        List<String> equippedList = new ArrayList<>();
        Map<PerkType, Integer> equippedPerks = profile.getEquippedPerks();

        for (Map.Entry<PerkType, Integer> entry : equippedPerks.entrySet()) {
            PerkType type = entry.getKey();
            int stars = entry.getValue();

            if (type.isStarPerk()) {
                // Add one entry per star
                for (int i = 0; i < stars; i++) {
                    equippedList.add(type.getId());
                }
            } else {
                equippedList.add(type.getId());
            }
        }
        config.set(playerID + "_equipped", equippedList);

        // Save owned perks
        List<String> ownedList = new ArrayList<>();
        Map<PerkType, Integer> ownedPerks = profile.getOwnedPerks();

        for (Map.Entry<PerkType, Integer> entry : ownedPerks.entrySet()) {
            PerkType type = entry.getKey();
            int quantity = entry.getValue();

            // Add one entry per quantity
            for (int i = 0; i < quantity; i++) {
                ownedList.add(type.getId());
            }
        }

        // Add perk points at the end
        ownedList.add(String.valueOf(profile.getPerkPoints()));
        config.set(playerID + "_owned", ownedList);
    }

    /**
     * Handle player joining - load their perks.
     */
    public void handlePlayerJoin(Player player) {
        loadPerks(player);
    }

    /**
     * Handle player leaving - save and cleanup.
     */
    public void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        PerkProfile profile = profiles.get(playerUUID);

        if (profile != null) {
            // Disable all equipped perks
            profile.clearEquippedPerks(player);

            // Save the profile
            savePlayerPerks(playerUUID);

            // Remove from memory
            removeProfile(playerUUID);
        }
    }

    /**
     * Get all loaded profiles (for admin commands, etc.)
     */
    public Collection<PerkProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }

}