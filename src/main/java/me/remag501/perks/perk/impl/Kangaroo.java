package me.remag501.perks.perk.impl;

import me.remag501.bgscore.api.TaskHelper;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Kangaroo extends Perk {

    private static final long COOLDOWN_TIME = 30 * 1000;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private final TaskHelper taskHelper;
    private final PerkManager perkManager;

    public Kangaroo(TaskHelper taskHelper, PerkManager perkManager) {
        super(PerkType.KANGAROO);
        this.taskHelper = taskHelper;
        this.perkManager = perkManager;
    }

    @Override
    public void onEnable(Player player, int stars) {
        UUID uuid = player.getUniqueId();

        // 1. Setup Flight
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(true);
        }

        // 2. Register SURGICAL Listeners
        // These only fire for THIS player, and we tag them with "kangaroo" for easy removal
        taskHelper.subscribe(PlayerMoveEvent.class)
                .owner(uuid)
                .namespace(getType().getId())
                .handler(this::handleMove);

        taskHelper.subscribe(PlayerToggleFlightEvent.class)
                .owner(uuid)
                .namespace(getType().getId())
                .handler(this::handleToggleFlight);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();

        // 1. Cleanup Flight
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        // 2. Surgical Removal
        // This tells the Core EventBus: "Delete all kangaroo listeners for this UUID"
        taskHelper.unregisterListener(uuid, getType().getId());

        cooldowns.remove(uuid);
    }

    private void handleMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOnGround() && !isOnCooldown(player.getUniqueId())) {
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                player.setAllowFlight(true);
            }
        }
    }

    private void handleToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;

        event.setCancelled(true);

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
        player.setFlying(false);
        player.setAllowFlight(false);

        Vector jumpVelocity = player.getVelocity().normalize().multiply(1.5).setY(1.0);
        player.setVelocity(jumpVelocity);

        player.sendMessage("§a§l(!) §aYou used your double jump!");
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);

        cooldowns.put(uuid, System.currentTimeMillis());

        // Using TaskHelper instead of BukkitRunnable
        taskHelper.delay(600, () -> {
            if (perkManager.getProfile(uuid).isActive(getType())) {
                player.sendMessage("§a§l(!) §aDouble jump is ready!");
            }
        });
    }

    private boolean isOnCooldown(UUID uuid) {
        Long lastUsed = cooldowns.get(uuid);
        return lastUsed != null && (System.currentTimeMillis() - lastUsed) < COOLDOWN_TIME;
    }
}