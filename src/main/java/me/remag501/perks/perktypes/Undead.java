package me.remag501.perks.perktypes;

import me.remag501.perks.core.Perk;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Undead extends Perk {

    private static final int ABSORPTION_DURATION_SECONDS = 60;
    private static final int ABSORPTION_HEARTS = 4;
    private static final long ASSIST_WINDOW_MILLIS = 5000L; // 5 seconds

    // --- STATIC TRACKING STRUCTURE (Local to this perk class) ---

    // Helper class to store the necessary data points
    private static class AttackData {
        final UUID attackerId;
        final long timestamp;

        AttackData(UUID attackerId) {
            this.attackerId = attackerId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // Tracks: Victim UUID -> Last Player Attacker Data (UUID and Time)
    private static final Map<UUID, AttackData> LAST_ATTACK_DATA = new ConcurrentHashMap<>();

    // -----------------------------------------------------------

    public Undead(ItemStack perkItem) {
        super(perkItem);
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

    // --- TRACKING LISTENER: Records when a player with the perk hits a mob/player ---
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Check if the attacker has the Undead perk (using the prototype instance for the check)
        if (getPerk(attacker.getUniqueId()) instanceof Undead) {
            // Log the latest attack data for this victim
            LAST_ATTACK_DATA.put(victim.getUniqueId(), new AttackData(attacker.getUniqueId()));
        }
    }

    // --- DEATH EVENT HANDLER: Checks for third-party kills ---
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        UUID victimId = victim.getUniqueId();

        // 1. Get the last recorded attacker data and cleanup the tracking map
        AttackData data = LAST_ATTACK_DATA.remove(victimId);
        if (data == null) return;

        // 2. Check the 5-second assist window
        if (System.currentTimeMillis() - data.timestamp > ASSIST_WINDOW_MILLIS) {
            return;
        }

        // 3. Check if the final blow was NOT a direct kill by the recorded attacker
        if (victim.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            UUID killerId = damageEvent.getDamager().getUniqueId();

            // If the killer's ID matches the recorded attacker's ID, it was a direct kill, not an assist.
            if (data.attackerId.equals(killerId)) {
                return;
            }
        }

        // --- If we reach here, the recorded player assisted within the 5-second window ---

        // 4. Validate and reward the assist player
        Player assistPlayer = Bukkit.getPlayer(data.attackerId);
        if (assistPlayer == null) return;

        // 5. Final check: Ensure the assist player still has the Undead perk (safety check)
        // We must fetch the *cloned instance* to call the grantAbsorption method on it.
        Undead perk = (Undead) getPerk(data.attackerId);
        if (perk == null) return;

        // 6. Success: Grant the absorption hearts to the perk holder
        perk.grantAbsorption(assistPlayer);

        assistPlayer.sendMessage("§a§l(!) §aThe shadows reward your assist! Gained §d" + ABSORPTION_HEARTS + " §aAbsorption Hearts.");
    }

    /**
     * Grants the player a temporary absorption effect.
     * Called on the CLONED instance (perk).
     */
    private void grantAbsorption(Player player) {
        player.removePotionEffect(PotionEffectType.ABSORPTION);

        // Apply the new, strong absorption effect (Amplifier is level - 1)
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION,
                ABSORPTION_DURATION_SECONDS * 20,
                ABSORPTION_HEARTS / 4 - 1,
                false,
                true
        ));
    }
}