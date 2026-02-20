//package me.remag501.perks.perk.impl;
//
//import me.remag501.perks.perk.Perk;
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.entity.ArmorStand;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//
//public class Concussion extends Perk {
//
//    public Concussion(ItemStack perkItem) {
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
//    // New encapsulated method to handle the perk effect
//    private void handleConcussionEffect(LivingEntity entity) {
//        Player player = Bukkit.getPlayer(this.player);
//        ItemStack itemInHand = player.getInventory().getItemInMainHand();
//
//        if (itemInHand.getType() == Material.AIR) {
//            entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
//        }
//    }
//
//    // Event listener to set entity on fire when punched with an empty hand
//    @EventHandler
//    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
//        // Check if the damager is a player
//        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof LivingEntity entity) {
//            if (entity instanceof ArmorStand)
//                return; // Ignore damage to armor stands
//            // Check if the player has the perk enabled
//            Concussion perk = (Concussion) getPerk(damager.getUniqueId());
//            if (perk == null) return; // Player does not have the perk enabled
//
//            // Delegate behavior to the perk instance
//            perk.handleConcussionEffect(entity);
//        }
//    }
//}
