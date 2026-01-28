package me.remag501.perks.perktypes.impl;

import me.remag501.perks.perktypes.Perk;
import me.remag501.perks.perktypes.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Kangaroo extends Perk {

    private static final long COOLDOWN_TIME = 30 * 1000; // 30 seconds in milliseconds
//    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private long cooldowns;

    public Kangaroo(ItemStack perkItem, List<List<PerkType>> requirements) {
        super(perkItem, requirements);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        Player player = Bukkit.getPlayer(this.player);
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    /**
     * Handles resetting flight capability when the player lands on the ground.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player eventPlayer = event.getPlayer();
        if (getPerk(eventPlayer.getUniqueId()) == null) return;

        if (eventPlayer.isOnGround() && !hasCooldown(eventPlayer)) {
            eventPlayer.setAllowFlight(true); // Allow flight while on the ground
        }
    }

    /**
     * Handles the double-jump mechanic and cooldown application.
     */
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player eventPlayer = event.getPlayer();
        if (getPerk(eventPlayer.getUniqueId()) == null) return;

        if (!eventPlayer.isOnGround() && !hasCooldown(eventPlayer)) {
            if (eventPlayer.getGameMode() == GameMode.SURVIVAL || eventPlayer.getGameMode() == GameMode.ADVENTURE) {
                eventPlayer.setFlying(false);
                eventPlayer.setAllowFlight(false);
            }

            Vector jumpVelocity = eventPlayer.getVelocity();
            jumpVelocity.normalize();
            jumpVelocity.multiply(1.5); // Adjust forward velocity
            jumpVelocity.setY(1.0); // Adjust upward velocity
            eventPlayer.setVelocity(jumpVelocity);

            eventPlayer.sendMessage("§a§l(!) §aYou used your double jump!");
            playDoubleJumpParticles(eventPlayer);
            startCooldown(eventPlayer);
        } else if (hasCooldown(eventPlayer)) {
            eventPlayer.sendMessage("§c§l(!) §cDouble jump is on cooldown! Wait a bit longer.");
        }

        if (eventPlayer.getGameMode() == GameMode.SURVIVAL || eventPlayer.getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true); // Prevent default flight behavior
        }
    }

    /**
     * Plays a particle effect to signify double jump.
     */
    private void playDoubleJumpParticles(Player player) {
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * Checks if the player is on cooldown.
     */
    private boolean hasCooldown(Player player) {
        if (getPerk(player.getUniqueId()) != null)
            return (System.currentTimeMillis() - cooldowns < COOLDOWN_TIME);
        return false;
    }

    /**
     * Starts the cooldown for the player.
     */
    private void startCooldown(Player player) {
//        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        cooldowns = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPerk(player.getUniqueId()) == null) return;
                player.sendMessage("§a§l(!) §aDouble jump is ready to use again!");
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("Perks"), 600L); // 600 ticks = 30 seconds
    }
}
