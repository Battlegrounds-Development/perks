//package me.remag501.perks.perk.impl;
//
//import me.remag501.perks.Perks;
//import me.remag501.perks.perk.Perk;
//import me.remag501.perks.perk.PerkType;
//import me.remag501.perks.manager.PerkManager;
//import me.remag501.perks.model.PerkProfile;
//import org.bukkit.*;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class Berserker extends Perk {
//    private final Map<UUID, Queue<Double>> fistDamageLog = new ConcurrentHashMap<>();
//
//    public Berserker(Perks plugin) {
//        super(plugin, null);
//    }
//
//    @Override
//    public void onEnable(Player player, int stars) {
//        fistDamageLog.put(player.getUniqueId(), new ArrayDeque<>());
//    }
//
//    @EventHandler
//    public void onEntityHit(EntityDamageByEntityEvent event) {
//        if (!(event.getDamager() instanceof Player player)) return;
//
//        // Dependency Injection: Use the plugin instance to get the manager
//        PerkProfile profile = plugin.getPerkManager().getProfile(player.getUniqueId());
//        if (profile == null || !profile.isEquipped(PerkType.BERSERKER)) return;
//
//        // ... damage logic ...
//
//        // Use injected plugin for tasks instead of Bukkit.getPlugin(...)
//        Bukkit.getScheduler().runTaskLater(plugin, () -> log.poll(), 60L);
//    }
//
//    @Override
//    public void onDisable(Player player) {
//        fistDamageLog.remove(player.getUniqueId());
//    }
//}