package me.remag501.perk.perk.impl;

import me.remag501.bgscore.api.BGSApi;
import me.remag501.bgscore.api.ability.AbilityDisplay;
import me.remag501.bgscore.api.ability.AbilityService;
import me.remag501.bgscore.api.event.EventService;
import me.remag501.bgscore.api.task.TaskService;
import me.remag501.perk.manager.PerkManager;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Kangaroo extends Perk {

    private static final String ULT_ID = "perk_kangaroo_ult";
    private static final double MAX_ENERGY = 100.0;

    private final AbilityService abilityService;
    private final EventService eventService;

    public Kangaroo(EventService eventService, TaskService taskService, PerkManager perkManager) {
        super(PerkType.KANGAROO);
        this.eventService = eventService;
        this.abilityService = BGSApi.ability();
    }

    @Override
    public void onEnable(Player player, int stars) {
        abilityService.setupUltimate(player.getUniqueId(), ULT_ID, MAX_ENERGY, AbilityDisplay.XP_BAR);

        // 1. Charge up by sprinting/walking
        eventService.subscribe(PlayerMoveEvent.class)
                .owner(player.getUniqueId())
                .namespace(getType().getId())
                .handler(this::handleCharge);

        // 2. SURPRISE: Trigger by landing (EntityDamageEvent for Fall Damage)
        eventService.subscribe(EntityDamageEvent.class)
                .owner(player.getUniqueId())
                .namespace(getType().getId())
                .handler(this::handleImpactTrigger);
    }

    @Override
    public void onDisable(Player player) {
        abilityService.reset(player.getUniqueId(), ULT_ID);
        eventService.unregisterListener(player.getUniqueId(), getType().getId());
    }

    private void handleCharge(PlayerMoveEvent event) {
        if (!event.getPlayer().isOnGround()) return;

        double dist = event.getFrom().distance(event.getTo());
        if (dist > 0) {
            // Fill the XP bar as they move
            abilityService.addCharge(event.getPlayer().getUniqueId(), ULT_ID, dist * 0.5);
        }
    }

    private void handleImpactTrigger(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        // If the bar is full when you hit the ground...
        if (abilityService.getPercentage(uuid, ULT_ID) >= 0.99) {
            // Cancel the fall damage!
            event.setCancelled(true);

            // Trigger the Bounce
            triggerMegaBounce(player);

            // Reset the bar
            abilityService.setCharge(uuid, ULT_ID, 0);
        }
    }

    private void triggerMegaBounce(Player player) {
        // Launch them forward and UP based on where they are looking
        Vector boost = player.getLocation().getDirection().multiply(2.0).setY(1.5);
        player.setVelocity(boost);

        player.sendMessage("§6§l(!) §e§lIMPACT BOUNCE! §6Energy released.");

        // Visuals
        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1f);
    }
}