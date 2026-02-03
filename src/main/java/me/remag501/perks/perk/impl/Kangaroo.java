package me.remag501.perks.perk.impl;

import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.model.PerkRegistry;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.model.PerkProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kangaroo perk - allows double jumping with a cooldown.
 * This perk needs cooldowns, so it manages them internally.
 */
public class Kangaroo extends Perk {

    private static final long COOLDOWN_TIME = 30 * 1000; // 30 seconds

    // This perk needs cooldowns, so it stores them here
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public Kangaroo(String id, PerkType type) {
        super(id, type);
    }

    @Override
    public void onEnable(Player player, int stars) {
        // Enable flight for double jump detection
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(true);
        }
    }

    @Override
    public void onDisable(Player player) {
        // Disable flight when perk is removed
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        cooldowns.remove(player.getUniqueId());

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!isPlayerUsingPerk(player)) {
            return;
        }

        // Re-enable flight when player lands
        if (player.isOnGround() && !isOnCooldown(player.getUniqueId())) {
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                player.setAllowFlight(true);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (!isPlayerUsingPerk(player)) {
            return;
        }

        // Only process in survival/adventure mode
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Cancel the event to prevent actual flight
        event.setCancelled(true);

        // Check if player is airborne and can double jump
        if (!player.isOnGround()) {
            if (isOnCooldown(player.getUniqueId())) {
                player.sendMessage("§c§l(!) §cDouble jump is on cooldown!");
                return;
            }

            performDoubleJump(player);
        }
    }

    private void performDoubleJump(Player player) {
        UUID uuid = player.getUniqueId();

        // Disable flight temporarily
        player.setFlying(false);
        player.setAllowFlight(false);

        // Calculate jump velocity
        Vector jumpVelocity = player.getVelocity();
        jumpVelocity.normalize();
        jumpVelocity.multiply(1.5); // Forward velocity
        jumpVelocity.setY(1.0); // Upward velocity
        player.setVelocity(jumpVelocity);

        // Visual and audio feedback
        player.sendMessage("§a§l(!) §aYou used your double jump!");
        player.getWorld().spawnParticle(
                Particle.EXPLOSION_LARGE,
                player.getLocation(),
                20, 0.5, 0.5, 0.5, 0.1
        );

        // Set cooldown
        cooldowns.put(uuid, System.currentTimeMillis());

        // Schedule cooldown expiry notification
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isPlayerUsingPerk(player)) {
                    player.sendMessage("§a§l(!) §aDouble jump is ready!");
                }
            }
        }.runTaskLater(PerkRegistry.getInstance().getPlugin(), 600L); // 30 seconds
    }

    private boolean isOnCooldown(UUID uuid) {
        Long lastUsed = cooldowns.get(uuid);
        if (lastUsed == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastUsed) < COOLDOWN_TIME;
    }

    private boolean isPlayerUsingPerk(Player player) {
        PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());
        return profile.isEquipped(getType());
    }
}