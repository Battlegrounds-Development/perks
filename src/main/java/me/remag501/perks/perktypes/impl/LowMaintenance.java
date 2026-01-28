package me.remag501.perks.perktypes.impl;

import me.remag501.perks.perktypes.Perk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LowMaintenance extends Perk {

    private static final int SATURATION_DURATION = 300; // 15 seconds (300 ticks)
    private static final long TASK_INTERVAL = 2400L; // 2 minutes (2400 ticks)
    private static final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public LowMaintenance(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
        Player player = Bukkit.getPlayer(this.player);
        if (!activeTasks.containsKey(player.getUniqueId())) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    applySaturation(player);
                }
            }.runTaskTimer(Bukkit.getPluginManager().getPlugin("Perks"), 0L, TASK_INTERVAL);

            activeTasks.put(this.player, task);
//            player.sendMessage("Low Maintenance Perk activated! You will periodically gain Saturation.");
        }
    }

    @Override
    public void onDisable() {
        if (activeTasks.containsKey(this.player)) {
            activeTasks.get(this.player).cancel();
            activeTasks.remove(this.player);
//            Bukkit.getPlayer(player).sendMessage("Low Maintenance Perk deactivated! No more periodic Saturation.");
        }
    }

    /**
     * Applies a Saturation effect to the player.
     */
    private void applySaturation(Player player) {
        if (player != null && player.isOnline()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, SATURATION_DURATION, 0)); // Saturation I for 15s
            player.sendMessage("You feel well-fed thanks to Low Maintenance!");
        }
    }
}
