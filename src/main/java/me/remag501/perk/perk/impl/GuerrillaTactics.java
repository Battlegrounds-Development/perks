//package me.remag501.perks.perk.impl;
//
//import me.remag501.perks.Perks;
//import me.remag501.perks.perk.Perk;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.player.PlayerToggleSneakEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitTask;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class GuerrillaTactics extends Perk {
//
//    // Unique state for THIS cloned instance: Stores the currently running task ID
//    private BukkitTask stealthTask;
//
//    // Perk Constants
//    private static final long INITIAL_SNEAK_MILLIS = 3000L; // Still requires 3 seconds of channeling
//    private static final int INVISIBILITY_REAPPLY_TICKS = 40; // 2 seconds (20 ticks/sec * 2 sec)
//    private static final int INVISIBILITY_DURATION_TICKS = 45; // Apply 2.25 seconds to ensure overlap
//    private static final int RADIUS = 1;
//
//    // Unique static state for the Prototype Listener: Tracks when the sneak started for the channel check
//    private static final Map<UUID, Long> SNEAK_START_TIME = new ConcurrentHashMap<>();
//
//    // Define floral materials
//    private static final Set<Material> FLORAL_MATERIALS = Collections.unmodifiableSet(createFloralMaterials());
//
//    public GuerrillaTactics(ItemStack perkItem) {
//        super(perkItem);
//    }
//
//    // --- Per-Player Lifecycle Hooks ---
//
//    @Override
//    public void onEnable() {
//        // activatePlayer() is handled upstream.
//    }
//
//    @Override
//    public void onDisable() {
//        // Stop the task if it's running and clean up effects
//        this.cancelStealthTask();
//
//        Player player = org.bukkit.Bukkit.getPlayer(this.player);
//        if (player != null) {
//            player.removePotionEffect(PotionEffectType.INVISIBILITY);
//        }
//        SNEAK_START_TIME.remove(this.player);
//    }
//
//    // --- Event Handling (On the PROTOTYPE Listener) ---
//
//    @EventHandler(ignoreCancelled = true)
//    public void onSneak(PlayerToggleSneakEvent event) {
//        Player player = event.getPlayer();
//        UUID uuid = player.getUniqueId();
//
//        // 1. Centralized Lookup: Get the active, cloned instance
//        GuerrillaTactics perk = (GuerrillaTactics) getPerk(uuid);
//        if (perk == null) return;
//
//        if (event.isSneaking()) {
//            // Player STARTING to sneak: Begin channeling
//
//            if (!perk.isInTacticalPosition(player.getLocation())) {
////                player.sendMessage("§eYou must be concealed to channel Guerrilla Tactics.");
//                return;
//            }
//
//            // Start the initial 3-second charge-up timer
//            SNEAK_START_TIME.put(uuid, System.currentTimeMillis());
//            player.sendMessage("§a§l(!) §aChanneling stealth...");
//
//            // IMPORTANT: Start a recurring task to check the channel
//            // This task will run every 5 ticks (0.25s) for responsiveness.
//            perk.stealthTask = Bukkit.getScheduler().runTaskTimer(Perks.getPlugin(), () -> {
//                perk.checkAndActivateContinuous(player);
//            }, 0L, 5L);
//
//        } else {
//            // Player STOPPING sneak: Cancel everything
//            perk.cancelStealthTask();
//        }
//    }
//
//
//    // --- Instance Methods for Logic (Called on the CLONED instance) ---
//
//    /**
//     * Checks if the 3s channel is complete. If so, starts applying the continuous invis effect.
//     */
//    private void checkAndActivateContinuous(Player player) {
//        UUID uuid = player.getUniqueId();
//        Long startTime = SNEAK_START_TIME.get(uuid);
//
//        // 1. Check for Channel Completion
//        if (startTime != null) {
//            if (System.currentTimeMillis() - startTime >= INITIAL_SNEAK_MILLIS) {
//                // Channel SUCCESS! Clear the tracking map and start the continuous effect mode.
//                SNEAK_START_TIME.remove(uuid);
//                player.sendMessage("§a§l(!) §aConcealment achieved!");
//
//                // Re-schedule the task to run less frequently for the active effect
//                this.stealthTask.cancel();
//                this.stealthTask = Bukkit.getScheduler().runTaskTimer(Perks.getPlugin(), () -> {
//                    this.applyContinuousEffect(player);
//                }, 0L, INVISIBILITY_REAPPLY_TICKS); // Runs every 2 seconds
//            }
//            // If the player moved or lost concealment during the channel, stop.
//            else if (!player.isSneaking() || !isInTacticalPosition(player.getLocation())) {
//                player.sendMessage("§c§l(!) §cChannel interrupted.");
//                cancelStealthTask();
//            }
//            return;
//        }
//
//        // 2. Continuous Effect Mode Check (Only runs if startTime is null)
//        if (!player.isSneaking() || !isInTacticalPosition(player.getLocation())) {
//            // Conditions broken: Player moved, stopped sneaking, or left the bush.
//            player.sendMessage("§c§l(!) §cConcealment broken!");
//            cancelStealthTask();
//        }
//    }
//
//    /**
//     * Applies the short invisibility effect.
//     */
//    private void applyContinuousEffect(Player player) {
//        // Apply a short duration effect that is refreshed before it expires
//        player.addPotionEffect(new PotionEffect(
//                PotionEffectType.INVISIBILITY,
//                INVISIBILITY_DURATION_TICKS, // 2.25 seconds duration
//                0, // Amplifier
//                false, // Ambient
//                false // Show particles
//        ));
//    }
//
//    /**
//     * Stops the running stealth task and removes invisibility.
//     */
//    private void cancelStealthTask() {
//        if (this.stealthTask != null && !this.stealthTask.isCancelled()) {
//            this.stealthTask.cancel();
//            this.stealthTask = null;
//
////            Player player = org.bukkit.Bukkit.getPlayer(this.player);
////            if (player != null) {
////                player.removePotionEffect(PotionEffectType.INVISIBILITY);
////            }
//        }
//        SNEAK_START_TIME.remove(this.player);
//    }
//
//
//    /**
//     * Checks if the player is next to any of the defined FLORAL_MATERIALS.
//     */
//    private boolean isInTacticalPosition(Location location) {
//        int baseY = location.getBlockY();
//
//        for (int x = -RADIUS; x <= RADIUS; x++) {
//            for (int z = -RADIUS; z <= RADIUS; z++) {
//                // Check current Y and the block above/below
//                for (int y = -1; y <= 1; y++) {
//                    Material material = location.getWorld().getBlockAt(
//                            location.getBlockX() + x,
//                            baseY + y,
//                            location.getBlockZ() + z
//                    ).getType();
//
//                    if (FLORAL_MATERIALS.contains(material)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    private static Set<Material> createFloralMaterials() {
//        Set<Material> materials = new HashSet<>();
//        materials.add(Material.TALL_GRASS);
//        materials.add(Material.GRASS);
//        materials.add(Material.FERN);
//        materials.add(Material.OAK_LEAVES);
//        materials.add(Material.POPPY);
//        materials.add(Material.DANDELION);
//        // Add other hiding spots like bushes or leaves
//        return materials;
//    }
//}