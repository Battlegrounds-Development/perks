package me.remag501.perk.model;

import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import me.remag501.perk.registry.PerkRegistry;
import me.remag501.perk.registry.WorldRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Simplified PerkProfile - no PerkInstance dependency.
 * Stars are stored here, cooldowns are in each Perk.
 */
public class PerkProfile {

    private final UUID playerUUID;
    private final Map<PerkType, Integer> ownedPerks; // PerkType -> quantity
    private final Map<PerkType, Integer> equippedPerks; // PerkType -> stars (1-3)
    private int perkPoints;
    private PerkType pendingScrap;

    private final PerkRegistry perkRegistry;
    private final WorldRegistry worldRegistry;

    public PerkProfile(PerkRegistry perkRegistry, WorldRegistry worldRegistry, UUID playerUUID) {
        this.perkRegistry = perkRegistry;
        this.worldRegistry = worldRegistry;
        this.playerUUID = playerUUID;
        this.ownedPerks = new HashMap<>();
        this.equippedPerks = new HashMap<>();
        this.perkPoints = 0;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    // ==================== OWNED PERKS ====================

    public boolean addOwnedPerk(PerkType type) {
        int currentQuantity = ownedPerks.getOrDefault(type, 0);
        if (currentQuantity >= 3) {
            return false;
        }
        ownedPerks.put(type, currentQuantity + 1);
        return true;
    }

    public boolean removeOwnedPerk(PerkType type) {
        int currentQuantity = ownedPerks.getOrDefault(type, 0);
        if (currentQuantity <= 0) {
            return false;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        boolean equipped = isEquipped(type); // Cache the status

        if (currentQuantity == 1) {
            ownedPerks.remove(type);
            equippedPerks.remove(type);

            // Only disable logic if they were actually using it
            if (equipped && isPerkWorld(player)) {
                perkRegistry.getPerk(type).onDisable(player);
            }
        } else {
            ownedPerks.put(type, currentQuantity - 1);

            // Downgrade stars only if equipped
            if (type.isStarPerk() && equipped) {
                int currentStars = equippedPerks.get(type);
                equippedPerks.put(type, currentStars - 1);
            }

            // Only toggle gameplay logic if equipped AND in a perk world
            if (equipped && isPerkWorld(player)) {
                perkRegistry.getPerk(type).onDisable(player);
                // Re-enable with the new (lower) star count
                perkRegistry.getPerk(type).onEnable(player, equippedPerks.get(type));
            }
        }
        return true;
    }

    public int getOwnedQuantity(PerkType type) {
        return ownedPerks.getOrDefault(type, 0);
    }

    public boolean ownsPerk(PerkType type) {
        return ownedPerks.containsKey(type);
    }

    public List<PerkType> getOwnedPerksList() {
        List<PerkType> list = new ArrayList<>();
        for (Map.Entry<PerkType, Integer> entry : ownedPerks.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public Map<PerkType, Integer> getOwnedPerks() {
        return new HashMap<>(ownedPerks);
    }

    // ==================== EQUIPPED PERKS ====================

    public boolean equipPerk(PerkType type, Player player) {
        // Check if at max equipped perks
        if (equippedPerks.size() >= 5 && !equippedPerks.containsKey(type)) {
            return false;
        }

        // Check if player owns the perk
        if (!ownsPerk(type)) {
            return false;
        }

        // Check requirements
        if (!hasRequirements(type)) {
            return false;
        }

        Integer equippedPerkCount = equippedPerks.get(type);
        int ownedPerkCount = ownedPerks.get(type);

        // Handle star perks
        if (equippedPerkCount != null && type.isStarPerk() && equippedPerkCount < 3) {

            if (equippedPerkCount >= ownedPerkCount) {
                player.sendMessage("§c§l(!) §7You need more of this perk to unlock more stars!");
                return false;
            }

            int newStars = equippedPerkCount + 1;
            equippedPerks.put(type, newStars);

            // Re-enable with new star count
            Perk perk = perkRegistry.getPerk(type);

            if (isPerkWorld(player)) {
                perk.onDisable(player);
                perk.onEnable(player, newStars);
            }

            return true;
        }

        // Already equipped and not a star perk
        if (equippedPerkCount != null) {
            return false;
        }

        // Equip new perk
        equippedPerks.put(type, 1); // Start with 1 star
        Perk perk = perkRegistry.getPerk(type);

        if (isPerkWorld(player)) {
            perk.onEnable(player, 1);
        }

        return true;
    }

    public boolean unequipPerk(PerkType type, Player player) {
        Integer stars = equippedPerks.get(type);
        if (stars == null) {
            return false;
        }

        // Handle star perks
        if (type.isStarPerk() && stars > 1) {
            int newStars = stars - 1;
            equippedPerks.put(type, newStars);

            // Re-enable with new star count
            Perk perk = perkRegistry.getPerk(type);


            if (isPerkWorld(player)) {
                perk.onDisable(player);
                perk.onEnable(player, newStars);
            }
            return true;
        }

        // Remove the perk
        equippedPerks.remove(type);
        Perk perk = perkRegistry.getPerk(type);

        if (isPerkWorld(player)) {
            perk.onDisable(player);
        }

        // Check for dependent perks and remove them
        removeDependentPerks(type, player);

        return true;
    }

    public boolean isActive(PerkType type) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (!isPerkWorld(player)) {
            return false;
        }

        return isEquipped(type);
    }

    public boolean isEquipped(PerkType type) {
        return equippedPerks.containsKey(type);
    }

    public int getEquippedStars(PerkType type) {
        return equippedPerks.getOrDefault(type, 0);
    }

    public List<PerkType> getEquippedPerksList() {
        return new ArrayList<>(equippedPerks.keySet());
    }

    public Map<PerkType, Integer> getEquippedPerks() {
        return new HashMap<>(equippedPerks);
    }

    // ==================== REQUIREMENTS ====================

    private boolean hasRequirements(PerkType type) {
        List<List<PerkType>> requirements = type.getRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        Set<PerkType> equippedSet = new HashSet<>(equippedPerks.keySet());

        for (List<PerkType> requirementGroup : requirements) {
            boolean hasOneFromGroup = false;
            for (PerkType required : requirementGroup) {
                if (equippedSet.contains(required)) {
                    equippedSet.remove(required);
                    hasOneFromGroup = true;
                    break;
                }
            }
            if (!hasOneFromGroup) {
                return false;
            }
        }
        return true;
    }

    private void removeDependentPerks(PerkType removedType, Player player) {
        List<PerkType> toRemove = new ArrayList<>();

        for (PerkType equipped : equippedPerks.keySet()) {
            if (!hasRequirements(equipped)) {
                toRemove.add(equipped);
            }
        }

        for (PerkType type : toRemove) {
            if (type.isStarPerk()) {
                equippedPerks.put(type, 1); // Reset to 1 star
            }
            equippedPerks.remove(type);

            Perk perk = perkRegistry.getPerk(type);

            if (isPerkWorld(player)) {
                perk.onDisable(player);
            }

        }
    }

    // ==================== PERK POINTS ====================

    public int getPerkPoints() {
        return perkPoints;
    }

    public void addPerkPoints(int amount) {
        this.perkPoints += amount;
    }

    public boolean spendPerkPoints(int amount) {
        if (perkPoints >= amount) {
            perkPoints -= amount;
            return true;
        }
        return false;
    }

    // ==================== SCRAP ====================

    public int scrapPerk(PerkType type) {
        if (!removeOwnedPerk(type)) {
            return 0;
        }

        int pointsGained = calculateScrapValue(type);
        addPerkPoints(pointsGained);
        return pointsGained;
    }

    private int calculateScrapValue(PerkType type) {
        int rarity = type.getRarity();
        switch (rarity) {
            case 0: return 1;
            case 1: return 2;
            case 2: return 3;
            case 3: return 5;
            default: return 0;
        }
    }

    public void setPendingScrap(PerkType type) {
        this.pendingScrap = type;
    }

    public PerkType getPendingScrap() {
        return pendingScrap;
    }

    // ==================== UTILITY ====================

    public void clearEquippedPerks(Player player) {
        for (PerkType type : equippedPerks.keySet()) {
            Perk perk = perkRegistry.getPerk(type);

            if (isPerkWorld(player)) {
                perk.onDisable(player);
            }
        }
        equippedPerks.clear();
    }

    private boolean isPerkWorld(Player player) {
        // If the player is null (offline), they aren't in ANY world,
        // so the perk logic shouldn't run.
        if (player == null) return false;

        String worldName = player.getWorld().getName();
        return !worldRegistry.DISABLED_WORLDS.contains(worldName) && !worldName.startsWith(worldRegistry.BUNKER_PREFIX);
    }

}