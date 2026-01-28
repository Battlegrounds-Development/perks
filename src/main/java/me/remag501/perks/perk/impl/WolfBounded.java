package me.remag501.perks.perk.impl;

import me.remag501.perks.Perks;
import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class WolfBounded extends Perk {

    private BukkitTask wolfTask;
    private PackMaster packMasterInstance;

    public WolfBounded(ItemStack perkItem) {
        super(perkItem);
    }

    @Override
    public void onEnable() {
        packMasterInstance = (PackMaster) getPerk(this.player, PerkType.PACK_MASTER);
        wolfTask = Bukkit.getScheduler().runTaskTimer(
                Perks.getPlugin(),
                this::wolfBoundedTask,
                0, 20L);

    }

    @Override
    public void onDisable() {
        packMasterInstance = null;
        if (wolfTask != null) {
            wolfTask.cancel();
        }
    }

    @EventHandler
    public void onWolfHit(EntityDamageEvent event) {

        Entity damagedEntity = event.getEntity();
        // Check if the damaged entity is a Wolf. If not, exit.
        if (!(damagedEntity instanceof Wolf wolf)) return;
        // Check if the Wolf has an owner. If not, or if the owner's UUID is null, exit.
        UUID ownerId = wolf.getOwner() != null ? wolf.getOwner().getUniqueId() : null;
        if (ownerId == null) return;
        // Attempt to get the WOLF_BOUNDED perk. If the perk is not found, exit.
        WolfBounded perk = (WolfBounded) getPerk(ownerId, PerkType.WOLF_BOUNDED);
        if (perk == null) return;

        if (event.getDamage() <= 1 || (wolf.getHealth() - event.getDamage()) < 0)
            return; // Check if dmg is too low or kills wolf

        // Trigger event for every player with wolf bounded perk
        double newDmg = perk.handleWolfHit(event.getDamage(), wolf.getUniqueId());

        event.setDamage(newDmg);

    }

    private double handleWolfHit(double dmg, UUID hitWolfId) {
        // Calculate hp pool and dmg
        double sharedDmg = dmg / this.packMasterInstance.getSummonedWolves().size();
        // Deal damage to every wolf except for the one in the event
        for (UUID wolfID: this.packMasterInstance.getSummonedWolves()) {
            if (wolfID == hitWolfId) continue; // Skip wolf already getting hit
            Wolf wolf = (Wolf) Bukkit.getEntity(wolfID);
            if (wolf != null) {
                double newHealth = wolf.getHealth() - sharedDmg;
                if (newHealth > 0) // Wolves should not die from bounded
                    wolf.setHealth(newHealth);
            }
        }
        // Return calculated dmg per wolf
//        Bukkit.getPlayer(this.player).sendMessage("Triggering wolf bounded " + sharedDmg);
        return sharedDmg;
    }

    private void wolfBoundedTask() {
        if (getPerk(this.player) != null) {
            for (UUID wolfId: packMasterInstance.getSummonedWolves()) {
                Wolf wolf = (Wolf) Bukkit.getEntity(wolfId);
                double health = Math.min(wolf.getHealth() + 0.5, 20);
                wolf.setHealth(health);
                wolf.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, wolf.getLocation(), 1, 0.5, 0.5, 0.5, 0.1);
            }

        }
        Bukkit.getLogger().info("test");
    }


//    private void wolfBoundedTask() {
//        if (getPerk(this.player) != null) {
//            // 1. Collect all valid wolf locations
//            List<Location> locations = new ArrayList<>();
//            // Start the chain from the player's eye level location
//            Player player = Bukkit.getPlayer(this.player);
//            locations.add(player.getEyeLocation());
//
//            for (UUID wolfId : packMasterInstance.getSummonedWolves()) {
//                Wolf wolf = (Wolf) Bukkit.getEntity(wolfId);
//                if (wolf != null) {
//                    // Heal the wolf
//                    double health = Math.min(wolf.getHealth() + 0.5, 20);
//                    wolf.setHealth(health);
//
//                    // Add the wolf's location to the chain
//                    locations.add(wolf.getLocation());
//                }
//            }
//
//            // 2. Draw the particle beams between consecutive entities
//            for (int i = 0; i < locations.size() - 1; i++) {
//                Location loc1 = locations.get(i);
//                Location loc2 = locations.get(i + 1);
//
//                // Draw a line between the current point and the next point
//                // Particle: ENCHANTMENT_TABLE (or use whatever particle you like!)
//                // Space: 0.2 (smaller number = smoother line, but more particles)
//                drawParticleLine(loc1, loc2, Particle.ENCHANTMENT_TABLE, 0.1);
//            }
//        }
//    }
//
//    public void drawParticleLine(Location loc1, Location loc2, Particle particle, double space) {
//        // 1. Check if the locations are in the same world
//        if (!loc1.getWorld().equals(loc2.getWorld())) return;
//
//        // 2. Calculate the distance and vector
//        double distance = loc1.distance(loc2);
//        // Get the vector pointing from loc1 to loc2
//        Vector direction = loc2.toVector().subtract(loc1.toVector());
//        // Normalize the vector (make its length 1) and multiply by the desired spacing
//        direction.normalize().multiply(space);
//
//        // 3. Clone the starting location to use as a moving point
//        Location current = loc1.clone();
//
//        // 4. Loop and spawn particles
//        for (double covered = 0; covered < distance; covered += space) {
//            loc1.getWorld().spawnParticle(particle, current, 1); // The '1' is the count
//            current.add(direction); // Move the current location one step closer to loc2
//        }
//    }

}
