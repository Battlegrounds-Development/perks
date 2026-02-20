//package me.remag501.perks.perk.impl;
//
//import me.remag501.perks.perk.Perk;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.inventory.ItemStack;
//
//public class FlowerPower extends Perk {
//
//    public FlowerPower(ItemStack perkItem) {
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
//    public void onEntityDamage(EntityDamageByEntityEvent event) {
//        if (!(event.getDamager() instanceof Player player)) return;
//        if (!(event.getEntity() instanceof LivingEntity target)) return;
//
//        // Make sure this player has the FlowerPower perk active
//        FlowerPower perk = (FlowerPower) getPerk(player.getUniqueId());
//        if (perk == null) return;
//
//        double radius = 5.0;
//        int flowerCount = 0;
//
//        // Count all floral blocks around player within a 5x5x5 cube
//        for (int x = -2; x <= 2; x++) {
//            for (int y = -2; y <= 2; y++) {
//                for (int z = -2; z <= 2; z++) {
//                    Block block = player.getLocation().add(x, y, z).getBlock();
//                    if (isFloral(block.getType())) {
//                        flowerCount++;
//                    }
//                }
//            }
//        }
//
//        if (flowerCount > 0) {
//            double multiplier = 1.0 + (0.05 * flowerCount);
//            double newDamage = event.getDamage() * multiplier;
//            event.setDamage(newDamage);
//
//            player.sendMessage("§a§l(!) §aFlower Power! §f+" + String.format("%.2f", (multiplier - 1) * 100) + "% §adamage boost!");
//        }
//    }
//
//    private boolean isFloral(Material material) {
//        return switch (material) {
//            case DANDELION,
//                    POPPY,
//                    BLUE_ORCHID,
//                    ALLIUM,
//                    AZURE_BLUET,
//                    RED_TULIP,
//                    ORANGE_TULIP,
//                    WHITE_TULIP,
//                    PINK_TULIP,
//                    OXEYE_DAISY,
//                    CORNFLOWER,
//                    LILY_OF_THE_VALLEY,
//                    SUNFLOWER,
//                    LILAC,
//                    ROSE_BUSH,
//                    PEONY,
//                    FERN,
//                    LARGE_FERN,
//                    GRASS,
//                    TALL_GRASS,
//                    MOSS_BLOCK,
//                    MOSS_CARPET -> true;
//            default -> false;
//        };
//    }
//}
