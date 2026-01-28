package me.remag501.perks.perk.impl;

import me.remag501.perks.perk.Perk;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class GoldenExperiencePerk extends Perk {

    public GoldenExperiencePerk(ItemStack item) {
        super(item);
    }

    private List<LivingEntity> enemies;
    private Player player;
    private Plugin plugin;

    @Override
    public void onEnable() {
        player.sendMessage("Golden Experience perk activated! Summon frogs to fight for you.");
        // Register event listeners
        this.player = player;
        this.enemies = new ArrayList<LivingEntity>();
        plugin = Bukkit.getPluginManager().getPlugin("Perks");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        player.sendMessage("Golden Experience perk deactivated.");
        // Unregister event listeners
        BlockBreakEvent.getHandlerList().unregister(this);
        EntityTargetLivingEntityEvent.getHandlerList().unregister(this);
        // Remove all summons
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                // Check if the entity has the specific metadata key
                if (entity.hasMetadata("SpawnedFromBlock")) {
                    // Remove the entity
                    ((LivingEntity) entity).setHealth(0);
                }
            }
        }
        // Clear list of enemies
        enemies.clear();
    }

//    @EventHandler
//    public void onBlockBreak(BlockBreakEvent event) {
//        Player player = event.getPlayer();
//        // Prevent the block from dropping items
//        event.setDropItems(false);
//        // Summon a frog at the block's location
//        Location loc = event.getBlock().getLocation().add(0.5, 1, 0.5);
//        // Get entity type based on the block type
//        EntityType type = getBlockType(event.getBlock().getType());
//        Mob mob = (Mob) player.getWorld().spawnEntity(loc, type);
//        mob.setMetadata("GoldenExperience", new FixedMetadataValue(plugin, true));
//        mob.setMetadata("SpawnedFromBlock", new FixedMetadataValue(plugin, event.getBlock().getType()));
//
//        player.sendMessage("Life has been created!");
//
//        // Set frog AI to attack nearby hostile mobs and players
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (!mob.isValid()) {
//                    cancel();
//                    return;
//                }
//                // Get the nearest hostile mob or player
//                LivingEntity nearestTarget = null;
//                // Remove dead entities from enemies list
//                for (LivingEntity enemy: enemies) {
//                    if (enemy.isDead())
//                        enemies.remove(enemy);
//                }
//                // Find the nearest target entity
//                if (enemies != null) {
//                    nearestTarget = enemies.stream()
//                            .filter(entity -> entity instanceof Monster || entity instanceof Player && !entity.equals(player))
//                            .findFirst()
//                            .orElse(null);
//                }
//
//                // If there's a hostile mob or player nearby, set the entity to target it
//                if (nearestTarget != null) {
//                    mob.setTarget(nearestTarget);
//                }
//            }
//        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("Perks"), 0L, 20L); // Check every second
//    }
//
//    public EntityType getBlockType(Material material) {
//        plugin.getLogger().info(material.toString());
//        return switch (material) {
//            case OAK_LOG, OAK_WOOD, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG -> EntityType.BEE;
//            case GOLD_BLOCK -> EntityType.PIGLIN_BRUTE;
//            case GOLD_ORE -> EntityType.PIGLIN;
//            case IRON_BLOCK -> EntityType.IRON_GOLEM;
//            case DIAMOND_BLOCK, NETHERITE_BLOCK -> EntityType.WARDEN;
//            case STONE_BRICKS, CRACKED_STONE_BRICKS, COBBLESTONE -> EntityType.SILVERFISH;
//            case COAL_ORE, COPPER_ORE -> EntityType.BAT;
//            case REDSTONE_ORE -> EntityType.MUSHROOM_COW;
//            case GRASS, DIRT, GRAVEL, SAND -> EntityType.SLIME;
//            case SANDSTONE, RED_SANDSTONE -> EntityType.DONKEY;
//            case TNT -> EntityType.CREEPER;
//            case SPONGE -> EntityType.GUARDIAN;
//            case NETHERRACK -> EntityType.MAGMA_CUBE;
//            case BONE_BLOCK, HAY_BLOCK, OAK_PLANKS, BLACKSTONE -> EntityType.WOLF;
//            case FIRE_CHARGE, FIRE -> EntityType.BLAZE;
//            default -> EntityType.FROG;
//        };
//    }
//
//    @EventHandler
//    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
//    // Prevent frogs from attacking the player who summoned them
//        if (event.getEntity().hasMetadata("GoldenExperience")) {
//            LivingEntity target = event.getTarget();
//            if (target == null)
//                return;
//            if (target.getUniqueId().equals(this.player.getUniqueId())) {
//                event.setCancelled(true);
//            } else if (target.hasMetadata("GoldenExperience")) {
//                event.setCancelled(true);
//            }
//        }
//    }
//
//    @EventHandler
//    public void onEntityDeath(EntityDeathEvent event) {
//
////        enemies.remove(event.getEntity()); not reliable
//        if (event.getEntity().hasMetadata("GoldenExperience")) {
//            player.sendMessage("Golden experience has been undone");
//            event.getDrops().clear(); // Clear the drops
//            event.setDroppedExp(0); // Remove any dropped experience
//            Location location = event.getEntity().getLocation();
////            Block block = event.getEntity().getMetadata("SpawnedFromBlock");
//            List<MetadataValue> metadataValues = event.getEntity().getMetadata("SpawnedFromBlock");
//            for (MetadataValue value : metadataValues) {
//                if (value.getOwningPlugin().equals(plugin)) {
//                    // Cast the stored value to a Block (or Location, if that’s what you stored)
//                    Material material = (Material) value.value();
//                    // Now you can use the material object
//                    Block block = location.getBlock();
//                    block.setType(material); // Replace the block with the material
//                }
//            }
//
//            if (event.getEntity() instanceof Slime || event.getEntity() instanceof MagmaCube) {
//                ((Slime) event.getEntity()).setSize(0); // Make the slime smaller
//            }
//            event.getEntity().remove();
//        }
//    }
//
//    @EventHandler
//    public void onEntityAttacked(EntityDamageByEntityEvent event) {
//        if (event.getDamager() instanceof Player && event.getDamager().equals(player)) {
//
//            if (event.getEntity() instanceof LivingEntity && !event.getEntity().hasMetadata("GoldenExperience")) {
//                if (enemies.contains(event.getEntity())) {
//                    enemies.remove((LivingEntity) event.getEntity());
//                    enemies.add(0, (LivingEntity) event.getEntity());
//                }
//                enemies.add((LivingEntity) event.getEntity());
//            }
//        }
//    }
//
//    private boolean hasPerkEnabled(Player player) {
//        // Your logic to check if the player has this perk enabled
//        return true; // For now, assume perk is enabled
//    }
}
