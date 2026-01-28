package me.remag501.perks.perk.impl;

import me.remag501.perks.perk.Perk;
import me.remag501.perks.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Jumped extends Perk {

    // Assuming you have a way to define this static type, e.g., in your PerkType enum.
    private static final PerkType PACK_MASTER_TYPE = PerkType.PACK_MASTER;

    // State to track the last hit time for the "first hit" logic (spam prevention)
    private static final long FIRST_HIT_WINDOW_MILLIS = 500;
    private static final java.util.Map<UUID, Long> LAST_HIT_TIME = new ConcurrentHashMap<>();

    public Jumped(ItemStack perkItem) {
        // You would pass the requirement list here: List<List<PerkType>> containing PackMaster
        super(perkItem);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        LAST_HIT_TIME.clear(); // Clean up static state on server reload/disable
    }

    // --- Event Handling (On the PROTOTYPE Listener) ---

    @EventHandler(ignoreCancelled = true)
    public void onFirstHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        UUID playerId = player.getUniqueId();

        // 1. Centralized Lookup: Get the active, cloned instance of JUMPED
        // (This ensures the player has THIS perk equipped)
        // NOTE: Uses the player's own PerkType for the lookup.
        Jumped jumpedPerk = (Jumped) getPerk(playerId);
        if (jumpedPerk == null) return;

        // 2. Check "First Hit" Condition (Spam prevention)
        long currentTime = System.currentTimeMillis();
        Long lastHit = LAST_HIT_TIME.get(playerId);
        if (lastHit != null && (currentTime - lastHit) < FIRST_HIT_WINDOW_MILLIS) {
            return;
        }
        LAST_HIT_TIME.put(playerId, currentTime);


        // 3. CORE PROOF OF CONCEPT: Access the required PackMaster instance.
        // This is the cleanest way to retrieve the instance and its state.
        PackMaster packMaster = (PackMaster) Perk.getPerk(playerId, PACK_MASTER_TYPE);

        if (packMaster != null) {
            // 4. Use the state from the required perk to perform the action
            jumpedPerk.teleportWolvesToTarget(player, packMaster, victim);
        }
    }

    /**
     * Teleports all wolves owned by the player (as tracked by PackMaster) to the victim.
     */
    private void teleportWolvesToTarget(Player owner, PackMaster packMaster, LivingEntity target) {
        if (packMaster.getSummonedWolves().isEmpty()) {
            return;
        }

        // Get the target location (slightly above the entity to avoid clipping)
        Location targetLoc = target.getLocation().add(0.0, 0.5, 0.0);

        int teleportCount = 0;

        // Iterate through the UUIDs provided by the PackMaster instance's state
        for (UUID wolfId : packMaster.getSummonedWolves()) {
            Entity entity = Bukkit.getEntity(wolfId);

            // Safety check: ensure it's a valid, tamed wolf owned by the player
            if (entity instanceof Wolf wolf && wolf.isTamed() && owner.equals(wolf.getOwner())) {
                wolf.teleport(targetLoc);
                teleportCount++;
            }
        }

        if (teleportCount > 0) {
            owner.sendMessage("§bJUMPED! §c" + target.getName() + " is being jumped by " + teleportCount + " wolves!");
        }
    }
}