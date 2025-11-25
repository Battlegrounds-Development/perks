package me.remag501.perks.perktypes;

import me.remag501.perks.core.Perk;
import me.remag501.perks.core.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Feral extends Perk {

    private PackMaster packMasterInstance;

    public Feral(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
        packMasterInstance = (PackMaster) getPerk(this.player, PerkType.PACK_MASTER);
    }

    @Override
    public void onDisable() {
        packMasterInstance = null;
    }

    @EventHandler
    public void onFeralHit(EntityDamageByEntityEvent event) {
        // Check if player has perk
        if (!(event.getDamager() instanceof Player player)) return;
        Feral perk = (Feral) getPerk(player.getUniqueId());
        if (perk == null) return;

        // Get number of wolves
        int numWolves = perk.packMasterInstance.getSummonedWolves().size();
        if (numWolves == 0) return; // Dont calculate if no wolves

        // Set dmg based on number of wolves
        double dmgMult = 1.0 + numWolves * 0.05;
        event.setDamage(event.getDamage() * dmgMult);
//        player.sendMessage("You got a multiplier of " + dmgMult);
    }

}
