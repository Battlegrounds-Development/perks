package me.remag501.perks.perk;

import me.remag501.perks.registry.PerkRegistry;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enum containing metadata for all perk types.
 * Does NOT contain logic - only data.
 */
public enum PerkType {
    BLOODIED(
            "BLOODIED",
            "Bloodied",
            4017,
            2,
            "When HP drops below 20/30/40% gain Strength I",
            true, // Star perk
            null
    ),

    FLASH(
            "FLASH",
            "Flash",
            4012,
            1,
            "Speed I but gain Weakness I every 2 minutes",
            false,
            null
    ),
    KANGAROO(
            "KANGAROO",
            "Kangaroo",
            4014,
            3,
            "Double jump once every thirty seconds",
            false,
            List.of(List.of(PerkType.FLASH))
    );

    private final String id;
    private final String displayName;
    private final int customModelData;
    private final int rarity;
    private final String description;
    private final boolean isStarPerk;
    private final List<List<PerkType>> requirements;

    PerkType(String id, String displayName, int customModelData, int rarity,
             String description, boolean isStarPerk, List<List<PerkType>> requirements) {
        this.id = id;
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.rarity = rarity;
        this.description = description;
        this.isStarPerk = isStarPerk;
        this.requirements = requirements == null ? Collections.emptyList() : requirements;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public int getRarity() {
        return rarity;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStarPerk() {
        return isStarPerk;
    }

    public List<List<PerkType>> getRequirements() {
        return requirements;
    }

    /**
     * Get the Perk instance from the registry.
     */
    public Perk getPerk() {
        return PerkRegistry.getInstance().getPerk(this);
    }

    /**
     * Get the ItemStack representation of this perk.
     */
    public ItemStack getItem() {
        return PerkRegistry.getInstance().getPerkItem(this);
    }

    public static List<PerkType> getPerksByRarity(int rarity) {
        List<PerkType> perks = new ArrayList<>();
        for (PerkType type : PerkType.values()) {
            if (type.getRarity() == rarity) {
                perks.add(type);
            }
        }
        return perks;
    }

    public static List<PerkType> perkTypesByPage(int page) {
        List<PerkType> perks = new ArrayList<>();
        int count = 0, passed = 0;

        for (PerkType type : PerkType.values()) {
            if (type.getRarity() != -1) { // Item is not hidden
                if (passed / 14 == page) {
                    perks.add(type);
                    count++;
                } else {
                    passed++;
                }

                if (count == 14) {
                    break;
                }
            }
        }
        return perks;
    }
}