package me.remag501.perks.perktypes.impl;

import me.remag501.perks.perktypes.Perk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;

public class SneakAttack extends Perk {

    // Perk Constant
    private static final double DAMAGE_MULTIPLIER = 1.50; // 150% damage
    // Angle Constant: Defines how far 'behind' the player must be.
    // 90 degrees (pi/2 radians) means exactly behind. We use 0.9 radians (~51 degrees) as a generous back arc.
    private static final double MAX_BEHIND_ANGLE_RADIANS = 0.9;

    public SneakAttack(ItemStack perkItem) {
        // This perk is assumed not to be a star perk for simplicity, but can be adjusted.
        super(perkItem, false);
    }

    // --- Per-Player Lifecycle Hooks ---

    @Override
    public void onEnable() {
        // activatePlayer() is handled upstream.
    }

    @Override
    public void onDisable() {
        // deactivatePlayer() is handled upstream. No unique cleanup needed.
    }

    // --- Event Handling (On the PROTOTYPE Listener) ---

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        UUID uuid = player.getUniqueId();

        // 1. Centralized Lookup: Get the active, cloned instance
        SneakAttack perk = (SneakAttack) getPerk(uuid);
        if (perk == null) return;

        // 2. Check "First Hit" Condition
        // If the victim has recently taken damage (i.e., not the first hit of a combo), skip.
        if (victim.getNoDamageTicks() > victim.getMaximumNoDamageTicks() / 2) {
            return;
        }

        // 3. Check "Behind" Condition
        if (isAttackerBehindVictim(player, victim)) {
            // Apply the damage multiplier
            double newDamage = event.getDamage() * DAMAGE_MULTIPLIER;
            event.setDamage(newDamage);

            // Visual/Feedback
//            player.sendMessage("§4SNEAK ATTACK! §c" + (int)((DAMAGE_MULTIPLIER - 1.0) * 100) + "% Bonus Damage.");
            player.sendMessage("§a§l(!) §aYou landed a back shot!");
            player.getWorld().playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
        }
    }


    // --- Instance Methods for Logic (Called on the CLONED instance) ---

    /**
     * Determines if the attacker is positioned behind the victim.
     * * @param attacker The player doing the attacking.
     * @param victim The entity being attacked.
     * @return true if the attacker is within the victim's back arc.
     */
    private boolean isAttackerBehindVictim(Player attacker, LivingEntity victim) {
        // 1. Get the victim's forward vector (where it's looking)
        Vector victimDirection = victim.getLocation().getDirection().normalize();

        // 2. Get the vector from the victim TO the attacker
        Vector victimToAttacker = attacker.getLocation().toVector()
                .subtract(victim.getLocation().toVector())
                .normalize();

        // 3. The dot product of two normalized vectors equals the cosine of the angle between them.
        // If the vectors are pointing in the same direction, dot product is 1 (angle 0).
        // If the attacker is behind the victim, the two vectors should be pointing
        // in opposite directions, resulting in a dot product close to -1.
        double dotProduct = victimDirection.dot(victimToAttacker);

        // We want the angle of separation, so we can use the dot product itself.
        // A dot product close to -1 means the victim's direction is opposite the attack direction.

        // Let's calculate the actual angle to compare:
        double angle = Math.acos(dotProduct);

        // If the angle is GREATER than a specific threshold (e.g., 2.24 radians, or ~128 degrees),
        // the attacker is in the 100-degree arc behind the victim.
        double behindThreshold = Math.PI - MAX_BEHIND_ANGLE_RADIANS; // e.g., Math.PI - 0.9 = 2.24 radians

        return angle > behindThreshold;
    }
}