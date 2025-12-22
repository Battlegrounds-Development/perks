package me.remag501.perks.perktypes;

import me.remag501.perks.core.Perk;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PackMaster Perk:
 * Effect: A wolf is summoned upon killing a player.
 * Requirements: Must track summoned wolves and despawn them on perk unequip.
 */
public class PackMaster extends Perk {

    // Unique state for THIS cloned instance: List of wolves summoned by this perk
    private final List<UUID> summonedWolves;

    public PackMaster(ItemStack perkItem) {
        super(perkItem);
        summonedWolves = new ArrayList<>();
    }

    // --- Per-Player Lifecycle Hooks ---

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        // Despawn all owned wolves when the perk is deactivated
        this.despawnAllWolves();
    }

    // --- Event Handling (On the PROTOTYPE Listener) ---

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return; // Not killed by a player

        UUID killerUuid = killer.getUniqueId();

        // 1. Centralized Lookup: Get the active, cloned instance for the killer
        PackMaster perk = (PackMaster) getPerk(killerUuid);
        if (perk == null) return; // Killer doesn't have the perk equipped

        // 2. Summon the wolf
        perk.summonWolf(killer);
    }

    @EventHandler
    public void onWolfDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Wolf wolf))
            return;

        AnimalTamer tamer = wolf.getOwner();
        if (tamer != null) {
            PackMaster perk = (PackMaster) getPerk(tamer.getUniqueId());
            if (perk != null) {
                perk.summonedWolves.remove(entity.getUniqueId());
            }
        }
    }

    /**
     * Spawns a tamed wolf for the player and registers it to this perk instance.
     */
    private void summonWolf(Player owner) {

        if (this.summonedWolves.size() >= 10) {
            owner.playSound(owner, Sound.ENTITY_WOLF_GROWL, 5, 0);
            Wolf minWolf = null;
            double minHP = 100;

            // Find lowest hp wolf
            for (UUID wolfUUID: summonedWolves) {
                Wolf wolfEntity = (Wolf) Bukkit.getEntity(wolfUUID);
                if (wolfEntity.getHealth() < minHP) {
                    minWolf = wolfEntity;
                    minHP = minWolf.getHealth();
                }
            }

            // Heal lowest hp wolf
            minWolf.setHealth(20);
            owner.sendMessage("§c§l(!) §cYou have reached the maximum amount of wolves. Lowest HP wolf has been healed!");
            return;
        }

        // Spawn the wolf at the owner's location
        Location location = owner.getLocation();
        Wolf wolf = (Wolf) location.getWorld().spawnEntity(location, EntityType.WOLF);

        // Tame the wolf and set the owner
        wolf.setTamed(true);
        wolf.setOwner(owner);
//        wolf.setCollarColor(DyeColor.BROWN); // Optional: differentiate perk wolves

        // Add visual feedback (sound/message)
        owner.sendMessage("§a§l(!) §aA new wolf joins your pack!");
        owner.playSound(owner, Sound.ENTITY_WOLF_WHINE, 1, 0);

        // Add the wolf's UUID to the instance state for tracking/cleanup
        this.summonedWolves.add(wolf.getUniqueId());
    }

    /**
     * Despawns all wolves tracked by this specific perk instance.
     */
    private void despawnAllWolves() {
        if (this.summonedWolves.isEmpty()) return;

        // Iterate through all UUIDs and attempt to find and remove the entity
        for (UUID wolfUuid : this.summonedWolves) {
            Entity entity = Bukkit.getEntity(wolfUuid);
            if (entity != null && entity.isValid() && entity.getType() == EntityType.WOLF) {
                entity.remove();
            }
        }

        // Clear the list after removal
        this.summonedWolves.clear();
    }

    public List<UUID> getSummonedWolves() {
        return summonedWolves;
    }
}