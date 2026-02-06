package me.remag501.perks.perk;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Abstract base class for all perks.
 * Perks are stateless and shared across all players (Singleton/Flyweight pattern).
 * Only contains logic - no player-specific data.
 */
public abstract class Perk {

    private final PerkType type;

    protected Perk(PerkType type) {
        this.type = type;
    }

    public PerkType getType() {
        return type;
    }

    /**
     * Called when this perk is enabled for a player.
     * @param player The player enabling the perk
     * @param stars The number of stars for this perk (1-3)
     */
    public abstract void onEnable(Player player, int stars);

    /**
     * Called when this perk is disabled for a player.
     * @param player The player disabling the perk
     */
    public abstract void onDisable(Player player);

//    /**
//     * Optional cleanup when player leaves or unequips.
//     * Override this if your perk needs to clean up player-specific data.
//     * @param playerUUID The player's UUID
//     */
//    public void cleanup(UUID playerUUID) {
//        // Default: no cleanup needed
//        // Perks like Kangaroo can override this to clear cooldowns
//    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Perk)) return false;
        return ((Perk) obj).type.equals(this.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}