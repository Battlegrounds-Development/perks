package me.remag501.perks.perk.impl;

import me.remag501.bgscore.api.task.TaskService;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.model.PerkProfile;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flash perk - Grants permanent Speed I but applies Weakness I every 2 minutes.
 * Speed boost with a periodic drawback.
 */
public class Flash extends Perk {

    private static final long WEAKNESS_INTERVAL = 2400L; // 2 minutes (2400 ticks)
    private static final int WEAKNESS_DURATION = 80; // 4 seconds (80 ticks)

    // Track weakness application tasks per player
    private final Map<UUID, BukkitTask> weaknessTasks = new ConcurrentHashMap<>();

    private final TaskService taskService;
    private final PerkManager perkManager;

    public Flash(TaskService taskService, PerkManager perkManager) {
        super(PerkType.FLASH);
        this.taskService = taskService;
        this.perkManager = perkManager;
    }

    @Override
    public void onEnable(Player player, int stars) {
        UUID uuid = player.getUniqueId();

        // Apply permanent speed effect
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                0, // Speed I
                false,
                false
        ));

        // Start periodic weakness application
        taskService.subscribe(player.getUniqueId(), getType().getId(), (int) WEAKNESS_INTERVAL, (int) WEAKNESS_INTERVAL, (ticks) -> {
            applyWeakness(player);
            return false;
        });

        player.sendMessage("§a§l(!) §aFlash activated! Speed I applied, but weakness every 2 minutes!");
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();

        // Remove speed effect
        if (player.isOnline()) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }

        // Cancel weakness task
        taskService.stopTask(player.getUniqueId(), getType().getId());
    }

    /**
     * Apply weakness debuff if player still has perk equipped.
     */
    private void applyWeakness(Player player) {
        if (!player.isOnline()) {
            return;
        }

        // Verify player still has perk equipped
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        if (!profile.isActive(getType())) {
            // Perk was unequipped, stop the task
            BukkitTask task = weaknessTasks.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
            return;
        }

        // Apply weakness
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.WEAKNESS,
                WEAKNESS_DURATION,
                0, // Weakness I
                false,
                true // Show particles
        ));

        player.sendMessage("§c§l(!) §cYou feel weak from running too fast!");
    }
}