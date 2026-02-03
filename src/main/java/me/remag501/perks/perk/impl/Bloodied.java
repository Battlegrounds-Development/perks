package me.remag501.perks.perk.impl;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.registry.PerkRegistry;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.model.PerkProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloodied perk - Grants strength when health drops below threshold.
 * Threshold increases with stars: 20%/30%/40%
 */
public class Bloodied extends Perk {

    // Track per-player state
    private final Map<UUID, PlayerBloodiedState> playerStates = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> healthCheckTasks = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final PerkManager perkManager;

    public Bloodied(Plugin plugin, PerkManager perkManager) {
        super(PerkType.BLOODIED);
        this.plugin = plugin;
        this.perkManager = perkManager;
    }

    @Override
    public void onEnable(Player player, int stars) {
        UUID uuid = player.getUniqueId();

        // Initialize player state
        PlayerBloodiedState state = new PlayerBloodiedState(stars);
        playerStates.put(uuid, state);

        // Start periodic health check
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> checkHealthAndApplyEffect(player),
                0L,
                200L // Check every 10 seconds
        );
        healthCheckTasks.put(uuid, task);

        player.sendMessage("§6§l(!) §6Bloodied activated! Threshold: " + (int)(state.healthThreshold * 100) + "%");
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();

        // Remove bloodied effect if active
        PlayerBloodiedState state = playerStates.get(uuid);
        if (state != null && state.isBloodied && player.isOnline()) {
            removeBloodiedEffect(player, state);
        }

        // Clean up all player-specific data
        BukkitTask task = healthCheckTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        playerStates.remove(uuid);
    }


    /**
     * Check player's health and apply/remove bloodied effect as needed.
     */
    private void checkHealthAndApplyEffect(Player player) {
        if (!player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerBloodiedState state = playerStates.get(uuid);
        if (state == null) {
            return;
        }

        // Check if still equipped
        PerkProfile profile = perkManager.getProfile(uuid);
        if (!profile.isActive(getType())) {
            return;
        }

        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double healthPercent = currentHealth / maxHealth;

        if (currentHealth > 0 && healthPercent <= state.healthThreshold) {
            // Health is below threshold - apply bloodied
            if (!state.isBloodied) {
                applyBloodiedEffect(player, state);
            }
        } else {
            // Health is above threshold - remove bloodied
            if (state.isBloodied) {
                removeBloodiedEffect(player, state);
            }
        }
    }

    /**
     * Apply the bloodied strength effect.
     */
    private void applyBloodiedEffect(Player player, PlayerBloodiedState state) {
        PotionEffect existingEffect = player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);

        // Save existing strength effect (from kits, etc.)
        if (existingEffect != null) {
            // Don't apply if they already have stronger strength
            if (existingEffect.getAmplifier() > state.amplifier) {
                return;
            }
            state.savedDuration = existingEffect.getDuration();
            state.savedAmplifier = existingEffect.getAmplifier();
        }

        // Apply bloodied strength
        state.isBloodied = true;
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INCREASE_DAMAGE,
                Integer.MAX_VALUE,
                state.amplifier,
                false,
                false
        ));

        player.sendMessage("§c§l(!) §cYou feel the strength of bloodied rage!");
    }

    /**
     * Remove the bloodied strength effect.
     */
    private void removeBloodiedEffect(Player player, PlayerBloodiedState state) {
        state.isBloodied = false;

        PotionEffect currentEffect = player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);

        // Only remove if it's our bloodied effect
        if (currentEffect != null &&
                currentEffect.getAmplifier() == state.amplifier &&
                currentEffect.getDuration() > 500) {

            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

            // Restore previous strength effect if there was one
            if (state.savedDuration > 0) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.INCREASE_DAMAGE,
                        state.savedDuration,
                        state.savedAmplifier,
                        false,
                        false
                ));
            }
        }

        // Reset saved effect
        state.savedDuration = 0;
        state.savedAmplifier = 0;

        player.sendMessage("§7Your strength fades as you heal.");
    }

    // ==================== EVENT HANDLERS ====================

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check health immediately when damaged
        Bukkit.getScheduler().runTask(
                plugin,
                () -> checkHealthAndApplyEffect(player)
        );
    }

    @EventHandler
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check health immediately when healed
        Bukkit.getScheduler().runTask(
                plugin,
                () -> checkHealthAndApplyEffect(player)
        );
    }

    @EventHandler
    public void onPlayerLoseEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Recheck health when effects change
        Bukkit.getScheduler().runTask(
                plugin,
                () -> checkHealthAndApplyEffect(player)
        );
    }

    // ==================== INNER CLASS ====================

    /**
     * Stores per-player bloodied state.
     */
    private static class PlayerBloodiedState {
        final double healthThreshold;
        final int amplifier;
        boolean isBloodied;
        int savedDuration;
        int savedAmplifier;

        PlayerBloodiedState(int stars) {
            this.amplifier = 0; // Strength I
            this.isBloodied = false;
            this.savedDuration = 0;
            this.savedAmplifier = 0;

            // Set threshold based on stars
            switch (stars) {
                case 1:
                    this.healthThreshold = 0.2; // 20%
                    break;
                case 2:
                    this.healthThreshold = 0.3; // 30%
                    break;
                case 3:
                    this.healthThreshold = 0.4; // 40%
                    break;
                default:
                    this.healthThreshold = 0.2;
                    break;
            }
        }
    }
}