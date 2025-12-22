package me.remag501.perks.perktypes;

import me.remag501.perks.core.Perk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyHunter extends Perk {

    public BountyHunter(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return; // No killer, ignore

        UUID uuid = killer.getUniqueId();
        BountyHunter perk = (BountyHunter) getPerk(uuid);
        if (perk == null) return; // Player doesn't have the perk equipped

        // Execute command to gain money
        killer.sendMessage("§a§l(!) §aYou collected $5000 for neutralizing a player!");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + killer.getName() + " 5000");
    }
}
