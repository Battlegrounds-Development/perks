//package me.remag501.perks.perk.impl;
//
//import me.remag501.perks.perk.Perk;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//
//import java.util.*;
//
//public class Overdrive extends Perk {
//
//    public Overdrive(ItemStack perkItem) {
//        super(perkItem);
//    }
//
//    @Override
//    public void onEnable() {
//    }
//
//    @Override
//    public void onDisable() {
//    }
//
//    @EventHandler
//    public void onEntityHit(EntityDamageByEntityEvent event) {
//        Entity damager = event.getDamager();
//        if (!(damager instanceof Player player)) return;
//        if (event.getEntity() instanceof Player) return; // Players don't get with hit overdrive
//        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return; // Don't apply overdrive to arrows and other non living entities
//
//        UUID uuid = player.getUniqueId();
//        Overdrive perk = (Overdrive) getPerk(uuid);
//        if (perk == null) return; // Player doesn't have Overdrive equipped
//
//        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 0)); // Instant Heal
//    }
//}
