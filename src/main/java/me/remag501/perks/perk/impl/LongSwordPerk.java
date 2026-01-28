package me.remag501.perks.perk.impl;

import me.remag501.perks.perk.Perk;
import org.bukkit.inventory.ItemStack;

public class LongSwordPerk extends Perk {

    public LongSwordPerk(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
//        player.sendMessage("Your perk has been activated!");

        // Grant the player a speed boost
//        PotionEffect speedBoost = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true); // Duration is very long, level 2 speed
//        player.addPotionEffect(speedBoost);

    }

    @Override
    public void onDisable() {
//        player.sendMessage("Your perk has been disabled!");
//
//        // Remove the speed boost effect
//        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
