package me.remag501.perks.perk.impl;

import me.remag501.perks.perk.Perk;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Berserker extends Perk {
    private static final Map<UUID, Queue<Double>> fistDamageLog = new HashMap<>();
    private static final long TRACK_DURATION = 60; // 3 seconds

    public Berserker(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
        fistDamageLog.put(this.player, new ArrayDeque<>());
    }

    @Override
    public void onDisable() {
        fistDamageLog.remove(player);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Berserker perk = (Berserker) getPerk(uuid);
        if (perk == null) return; // Player doesn't have Berserker equipped

        ItemStack weapon = player.getInventory().getItemInMainHand();
        double damage = event.getDamage();

        // Track fist damage
        if (weapon.getType() == Material.AIR) {
            fistDamageLog.get(uuid).add(damage);
            Bukkit.getScheduler().runTaskLaterAsynchronously(
                    Bukkit.getPluginManager().getPlugin("perks"),
                    () -> {
                        if (!fistDamageLog.get(uuid).isEmpty()) fistDamageLog.get(uuid).remove();
                    }, TRACK_DURATION
            );
            return;
        }

        // If using an axe, apply bonus damage
        if (weapon.getType().toString().endsWith("_AXE")) {
            double bonusDamage = fistDamageLog.get(uuid).stream().mapToDouble(Double::doubleValue).sum();
            if (bonusDamage > 1)
                event.setDamage(damage * bonusDamage);
            if (bonusDamage > 4) {
                // Play sound effect for Berserk hit
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 5.0f, 1.5f);
                player.sendMessage("§aYou landed a strong Beserk hit for " + (int) (bonusDamage * damage) + " damage!");

                // Play particle effects for a cool impact
                Location loc = event.getEntity().getLocation();
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 0.5, 0.5, 0.1);
                loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 5, 0.2, 0.2, 0.2, 0.1);
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 20, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.RED, 1.5f));
            }

            // Clear tracked fist damage after applying bonus
            fistDamageLog.get(uuid).clear();
        }
    }
}
