package me.remag501.perks.perktypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import me.remag501.perks.core.Perk;
import org.bukkit.inventory.ItemStack;

public class Serendipity extends Perk {

    public Serendipity(ItemStack item) {
        super(item);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getDamager() instanceof Player) return; // Perk does not apply to players

        UUID uuid = player.getUniqueId();
        Serendipity perk = (Serendipity) getPerk(uuid);
        if (perk == null) return; // Player doesn't have the perk equipped

        if (ThreadLocalRandom.current().nextDouble() < 0.20) {
            event.setCancelled(true); // Negate damage
            player.sendMessage("§a§l(!) §aSerendipity activated! You took no damage.");
        }
    }
}

