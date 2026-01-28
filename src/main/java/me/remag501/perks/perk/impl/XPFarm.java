package me.remag501.perks.perk.impl;

import me.remag501.perks.perk.Perk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class XPFarm extends Perk {
//    private static final Map<UUID, XPFarm> perkInstances = new HashMap<>();

    public XPFarm(ItemStack perkItem) {
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
        XPFarm perk = (XPFarm) getPerk(uuid);
        if (perk == null) return; // Player doesn't have the perk equipped

        // Execute command to gain xp
        killer.sendMessage("§a§l(!) §aYou received 300xp for neutralizing a player!");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clv addEXP 300 " + killer.getName());

    }
}
