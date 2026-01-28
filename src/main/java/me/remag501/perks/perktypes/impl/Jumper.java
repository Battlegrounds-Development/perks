package me.remag501.perks.perktypes.impl;

import me.remag501.perks.perktypes.Perk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Jumper extends Perk {
    private BukkitTask slownessTask;

    public Jumper(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
        Player player = Bukkit.getPlayer(this.player);
        // Apply Jump Boost I effect when the perk is enabled

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0)); // Jump Boost I

        // Schedule a repeating task that applies Slowness every 1.5 minutes (1800 ticks)
        slownessTask = Bukkit.getScheduler().runTaskTimer(
                player.getServer().getPluginManager().getPlugin("Perks"),
                () -> applySlowness(player),
                1800L, 1800L // Runs every 1.5 minutes
        );
    }

    @Override
    public void onDisable() {
        Player player = Bukkit.getPlayer(this.player);

        // Remove Jump Boost and cancel the slowness task when the perk is disabled
        player.removePotionEffect(PotionEffectType.JUMP);

        if (slownessTask != null) {
            slownessTask.cancel();
        }
    }

    private void applySlowness(Player player) {
        if (player.isOnline()) {
            player.sendMessage("You feel tired from jumping!");
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0)); // Slowness I for 5 seconds
        }
    }
}
