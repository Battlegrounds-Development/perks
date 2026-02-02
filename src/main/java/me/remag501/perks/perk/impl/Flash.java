//package me.remag501.perks.perk.impl;
//
//import me.remag501.perks.perk.Perk;
//import me.remag501.perks.perk.PerkType;
//import me.remag501.perks.manager.PerkManager;
//import me.remag501.perks.model.PerkProfile;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitTask;
//
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class Flash extends Perk {
//
//    private final Map<UUID, BukkitTask> weaknessTasks = new ConcurrentHashMap<>();
//
//    public Flash() {
//        super(null);
//    }
//
//    @Override
//    public void onEnable(Player player, int stars) {
//        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
//
//        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
//                Bukkit.getPluginManager().getPlugin("Perks"),
//                () -> applyWeakness(player),
//                2400L, 2400L
//        );
//        weaknessTasks.put(player.getUniqueId(), task);
//    }
//
//    @Override
//    public void onDisable(Player player) {
//        player.removePotionEffect(PotionEffectType.SPEED);
//        BukkitTask task = weaknessTasks.remove(player.getUniqueId());
//        if (task != null) task.cancel();
//    }
//
//    private void applyWeakness(Player player) {
//        PerkProfile profile = PerkManager.getInstance().getProfile(player.getUniqueId());
//        if (profile != null && profile.isEquipped(PerkType.FLASH)) {
//            player.sendMessage("§c§l(!) §cYou feel weak from running too fast!");
//            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
//        }
//    }
//}