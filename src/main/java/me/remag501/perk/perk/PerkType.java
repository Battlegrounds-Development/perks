package me.remag501.perk.perk;

import java.util.Collections;
import java.util.List;

/**
 * Enum containing metadata for all perk types.
 * Does NOT contain logic - only data.
 */
public enum PerkType {
    BLOODIED("BLOODIED", "Bloodied", 4017, 2, "When HP drops below 20/30/40% gain Strength I", true, null),
    FLASH("FLASH", "Flash", 4012, 1, "Speed I but gain Weakness I every 2 minutes", false, null),
    LOW_MAINTENANCE("LOW_MAINTENANCE", "Low Maintenance", 4018, 0, "Gain Saturation I for 15 seconds every 2 minutes", false, null),
    JUMPER("JUMPER", "Jumper", 4013, 1, "Jump Boost I but gain Slowness I every 1.5 minutes", false, null),
    RESISTANT("RESISTANT", "Resistant", 4019, 2, "When HP drops below 20/25/30% gain Resistance I", true, null),
    SERENDIPITY("SERENDIPITY", "Serendipity", 4020, 1, "20% chance to negate non-player damage", false, null),
    OVERDRIVE("OVERDRIVE", "Overdrive", 4021, 1, "Melee hits on mobs apply instant health", false, null),
    HOT_HANDS("HOT_HANDS", "Hot Hands", 4022, 0, "Punching with empty hands sets targets on fire", false, null),
    BERSERKER("BERSERKER", "Berserker", 4023, 2, "Each hit builds stacking damage bonus (up to 5 hits)", false, null),
    SNEAK_ATTACK("SNEAK_ATTACK", "Sneak Attack", 4024, 1, "Back attacks deal 50% extra damage", false, null),
    FLOWER_POWER("FLOWER_POWER", "Flower Power", 4025, 0, "Damage increased by 5% per nearby floral block", false, null),
    GUERRILLA_TACTICS("GUERRILLA_TACTICS", "Guerrilla Tactics", 4026, 2, "Sneak in flora for 3s to gain brief invisibility", false, null),
    UNDEAD("UNDEAD", "Undead", 4027, 2, "Assists grant temporary absorption hearts", false, null),

    // --- Migrated / Legacy perks added ---
    BOUNTY_HUNTER("BOUNTY_HUNTER", "Bounty Hunter", 4030, 1, "Collect currency on player kills", false, null),
    CONCUSSION("CONCUSSION", "Concussion", 4031, 1, "Punching with empty hands inflicts Nausea", false, null),
    COOKIE_CLICKER("COOKIE_CLICKER", "Cookie Clicker", 4032, 0, "Drop cookies on player kills", false, null),
    GHOST_FIST("GHOST_FIST", "Ghost Fist", 4033, 2, "Punch echoes deal a delayed follow-up hit", false, null),
    TAI_CHI("TAI_CHI", "Tai Chi", 4034, 1, "Hold empty hand then strike to apply Wither and Blindness", false, null),
    XP_FARM("XP_FARM", "XP Farm", 4035, 1, "Gain bonus XP on player kills", false, null),

    // Wolf / pet-related perks
    PACK_MASTER("PACK_MASTER", "Pack Master", 4040, 2, "Summon and track wolves on player kills", false, null),
    JUMPED("JUMPED", "Jumped", 4041, 1, "Teleport your wolves to your target on hit", false, List.of(List.of(PerkType.PACK_MASTER))),
    FERAL("FERAL", "Feral", 4042, 1, "Increase damage based on number of wolves", false, List.of(List.of(PerkType.PACK_MASTER))),
    WOLF_BOUNDED("WOLF_BOUNDED", "Wolf Bounded", 4043, 2, "Share incoming damage across your pack", false, List.of(List.of(PerkType.PACK_MASTER))),

    KANGAROO("KANGAROO", "Kangaroo", 4014, 3, "Double jump once every thirty seconds", false, List.of(List.of(PerkType.FLASH)));

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

}