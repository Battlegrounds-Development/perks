package me.remag501.perks.model;

import me.remag501.perks.perk.PerkType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerkProfile {
    private final UUID uuid;
    private int perkPoints;
    // Map of Perk -> How many "cards" they own (1-3)
    private final Map<PerkType, Integer> ownedCount = new HashMap<>();
    // Map of Perk -> How many stars are currently active (1-3)
    private final Map<PerkType, Integer> equippedStars = new HashMap<>();

    public PerkProfile(UUID uuid) {
        this.uuid = uuid;
    }

    // Getters and helper methods for counts
    public int getStars(PerkType type) { return equippedStars.getOrDefault(type, 0); }
    public int getOwned(PerkType type) { return ownedCount.getOrDefault(type, 0); }
    public boolean isEquipped(PerkType type) { return equippedStars.containsKey(type); }

    // Logic for adding/removing purely affects the numbers here
}