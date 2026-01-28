package me.remag501.perks.perk.impl;

import me.remag501.perks.perk.Perk;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class TheWorldPerk extends Perk {

    private static Map<UUID, TheWorldPerk> activePerks = new HashMap<>();

    private Player player;
    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("Perks");
    private static boolean timeStopped = false;
    private static List<Entity> frozenEntities = new ArrayList<>();
    private static Set<LivingEntity> delayedDeaths = new HashSet<>();
    private static Map<Entity, Vector> entityVelocities = new HashMap<>();
    private static List<Creeper> creepers = new ArrayList<>();
    private static Map<TNTPrimed, Integer> frozenTNT = new HashMap<>();
    private static Boolean[] rules = new Boolean[5];
    private static int stopTimeTaskId = -1; // Can be changed to instance in future
    private static int tickSpeed = 0;

    public TheWorldPerk(ItemStack item) {
        super(item);
    }

    @Override
    public void onEnable() {
        activePerks.put(super.player, this);
        this.player = Bukkit.getPlayer(super.player);
//        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.sendMessage(ChatColor.YELLOW + "「The World!」perk activated.");
    }

    @Override
    public void onDisable() {
        activePerks.remove(super.player);
        player.sendMessage(ChatColor.YELLOW + "「The World!」perk deactivated.");
        resumeTime(player);
        frozenEntities.clear();
//        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
//        event.getPlayer().sendMessage("Perks: " + activePerks);
        TheWorldPerk perk = activePerks.get(event.getPlayer().getUniqueId());
        if (perk == null) return;
        // The player chatting has the world perk active
        String message = event.getMessage();
        if (message.equalsIgnoreCase("ZA WARUDO") && !timeStopped) {
            // Schedule a synchronous task to interact with the world
            // Call your world-interacting logic here
            Bukkit.getScheduler().runTask(plugin, () -> stopTime(event.getPlayer()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!timeStopped)
            return;
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            // Check if the entity will "die" from this damage
            if (livingEntity.getHealth() - event.getFinalDamage() <= 0) {
                // Prevent entity from dying
                event.setCancelled(true);
                livingEntity.setInvulnerable(true); // Prevent further hits
                delayedDeaths.add(livingEntity); // Store the entity for delayed death
            }
        }

        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            if (!activePerks.containsKey(damageByEntityEvent.getDamager().getUniqueId())) { // Should not happen, but just in case
                event.setCancelled(true);
                return;
            }
            // Check if the player punches with fist, if so increase the knockback significantly
            if (damageByEntityEvent.getDamager() instanceof Player damager && damager.getInventory().getItemInMainHand().getType() == Material.AIR) {
                // Get the entity being damaged
                Entity damagedEntity = damageByEntityEvent.getEntity();
                if (!(damagedEntity instanceof Player))
                    return;
                // Retrieve the current velocity of the damaged entity
                Vector currentVelocity = entityVelocities.get(event.getEntity());
                // Calculate the new velocity; you can adjust the factor as needed
                double knockbackStrength = 10.0; // Adjust this value for the strength of the knockback
                Vector newVelocity = new Vector(-Math.sin(Math.toRadians(damager.getLocation().getYaw())) * knockbackStrength, // Knockback in the opposite X direction
                        currentVelocity.getY()+0.25,
                        Math.cos(Math.toRadians(damager.getLocation().getYaw())) * knockbackStrength); // Knockback in the Z direction

                // Set the new velocity to the damaged entity
                entityVelocities.put(event.getEntity(), newVelocity);
            }
        } else {
            event.setCancelled(true);
        }
    }

    private void handleEntityDamage(EntityDamageEvent event) {

    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof Creeper creeper && timeStopped) {
            event.setCancelled(true);
            creepers.add(creeper);
        }
    }

    @EventHandler
    public void onFluidFlow(BlockFromToEvent event) {
        // Check if time is stopped
        if (timeStopped) {
            // Cancel fluid movement
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (timeStopped) {
            Player movingPlayer = event.getPlayer();
            TheWorldPerk perk = activePerks.get(movingPlayer.getUniqueId());
            // If the moving player is not the player who activated the perk
            if (perk == null) {
                // Set the new location to the old one to prevent movement
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (timeStopped && !activePerks.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (timeStopped && event.getWhoClicked() instanceof Player player && !activePerks.containsKey(player.getUniqueId())) {
            event.setCancelled(true); // Cancel inventory interaction
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (timeStopped && !activePerks.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true); // Prevent opening the inventory
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (timeStopped && !activePerks.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true); // Prevent commands
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot use commands while time is stopped!");
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (timeStopped && !activePerks.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true); // Prevent the player from dropping items
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop items while time is stopped!");
        }
    }

    @EventHandler
    public void onRedstoneSignal(BlockRedstoneEvent event) {
        if (timeStopped) {
            // Freeze redstone by forcing the old current state
            event.setNewCurrent(event.getOldCurrent());
        }
    }


    private void stopTime(Player player) {
        timeStopped = true;
//        player.setGravity(false);
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setFlySpeed(0.03f);
        }
        player.sendMessage(ChatColor.YELLOW + "「The World!」Time has stopped for 5 seconds.");
        // Save previous gamerules before changing them
        rules[0] = player.getWorld().getGameRuleValue(GameRule.DISABLE_RAIDS);
        rules[1] = player.getWorld().getGameRuleValue(GameRule.DO_MOB_SPAWNING);
        rules[2] = player.getWorld().getGameRuleValue(GameRule.DO_FIRE_TICK);
        rules[3] = player.getWorld().getGameRuleValue(GameRule.DO_WEATHER_CYCLE);
        tickSpeed = player.getWorld().getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
        // Change gamerules so the world's time also stops
        player.getWorld().setGameRule(GameRule.DISABLE_RAIDS, false);
        player.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        player.getWorld().setGameRule(GameRule.DO_FIRE_TICK, false);
        player.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        player.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        // Get all entities in the player's world
        List<Entity> worldEntities = player.getWorld().getEntities();

        for (Entity entity : worldEntities) {
            // Ensure the entity is not the player themselves and is a living entity
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                ((LivingEntity) entity).setAI(false);
                frozenEntities.add(entity);
            }
        }

        // Schedule a repeating task every 10 ticks to check for new entities
        stopTimeTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (timeStopped) {
                freezeEntitiesInWorld(player.getWorld(), player);
            }
        }, 0L, 2L); // Repeats every 1 ticks (0L delay to start immediately)

        // Resume time after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                resumeTime(player);
                player.sendMessage(ChatColor.YELLOW + "Time resumes!");
            }
        }.runTaskLater(plugin, 100L); // 5 seconds (100 ticks)
    }

    private void resumeTime(Player player) {
        // Cancel the repeating task to freeze new entities
        if (stopTimeTaskId != -1) {
            Bukkit.getScheduler().cancelTask(stopTimeTaskId);
            stopTimeTaskId = -1;
        }
        timeStopped = false; // Resume time
        // Revert gamerules to their previous values
        player.getWorld().setGameRule(GameRule.DISABLE_RAIDS, rules[0]);
        player.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, rules[1]);
        player.getWorld().setGameRule(GameRule.DO_FIRE_TICK, rules[2]);
        player.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, tickSpeed);
        player.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, rules[3]);
        // Allow all entity to move again
        for (Entity entity : frozenEntities) {
            // Unfreeze living entities
            if (entity != null && entity.isValid() && entity instanceof LivingEntity) {
                ((LivingEntity) entity).setAI(true);
            }
            // Allow entities to move
            entity.setGravity(true);
            // Restore flight abilities before velocity
            if (entity instanceof Player enemy) {
                enemy.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);
                enemy.setFlying(player.getGameMode() == GameMode.CREATIVE);
                enemy.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            // Restore the original velocity from the map
            Vector originalVelocity = entityVelocities.get(entity);
            if (originalVelocity != null) {
                entity.setVelocity(originalVelocity); // Resume entity movement
            }

        }
        frozenEntities.clear();
        entityVelocities.clear();
        // Allow entities frozen in death animation to "die" naturally
        for (LivingEntity entity : delayedDeaths) {
            if (!entity.isDead()) {
                entity.setInvulnerable(false);
                entity.setHealth(0);
            }
        }
        delayedDeaths.clear(); // Clear after handling deaths
        for (TNTPrimed tnt : frozenTNT.keySet()) {
            if (tnt.isValid()) {
                // Restore the original fuse time
                int remainingFuse = frozenTNT.get(tnt);
                tnt.setFuseTicks(remainingFuse);
            }
        }
        // Ignite creepers that were stopped
        for (Creeper creeper: creepers) {
            creeper.ignite();
        }
        creepers.clear();
        frozenTNT.clear();
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlySpeed(0.1f);
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    private void freezeEntitiesInWorld(World world, Player player) {
        List<Entity> worldEntities = player.getWorld().getEntities();
        // Iterate over all entities in the player's world including new ones
        for (Entity entity : worldEntities) {
            // Skip already frozen entities
            if (frozenEntities.contains(entity) || entity.equals(player)) {
                continue;
            }
            // Freeze entities and players in the player's world
            entityVelocities.put(entity, entity.getVelocity());
            entity.setGravity(false);
            entity.setVelocity(new Vector(0, 0, 0)); // Stop entity movement
            // Handle other players
            if (entity instanceof Player enemy) {
                enemy.setAllowFlight(true);
                enemy.setFlying(true);
                enemy.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true, false));
            }
            frozenEntities.add(entity);
            // Check if primed tnt exists
            if (entity instanceof TNTPrimed tnt) {
                // Store the current fuse time
                frozenTNT.put(tnt, tnt.getFuseTicks());
                tnt.setFuseTicks(Integer.MAX_VALUE); // Prevent explosion during stopped time
            }
        }
    }

}

