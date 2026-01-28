package me.remag501.perks.perktypes.impl;

import me.remag501.perks.perktypes.Perk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CookieClicker extends Perk {

    public CookieClicker(ItemStack perkItem) {
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
        CookieClicker perk = (CookieClicker) getPerk(uuid);
        if (perk == null) return; // Player doesn't have the perk equipped

        // Drop two cookies at the victim's location
        killer.sendMessage("§a§l(!) §aHow tasty!");
        World world = event.getEntity().getWorld();
        world.dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.COOKIE, 2));
    }
}
