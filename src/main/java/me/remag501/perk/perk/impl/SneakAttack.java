package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class SneakAttack extends Perk {

	private static final double DAMAGE_MULTIPLIER = 1.50;
	private static final double MAX_BEHIND_ANGLE_RADIANS = 0.9;

	private final EventService eventService;

	public SneakAttack(EventService eventService) {
		super(PerkType.SNEAK_ATTACK);
		this.eventService = eventService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();

		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getDamager() instanceof Player p && p.getUniqueId().equals(uuid))
				.filter(event -> event.getEntity() instanceof LivingEntity)
				.handler(event -> {
					Player attacker = (Player) event.getDamager();
					LivingEntity victim = (LivingEntity) event.getEntity();

					if (isAttackerBehindVictim(attacker, victim)) {
						double newDamage = event.getDamage() * DAMAGE_MULTIPLIER;
						event.setDamage(newDamage);
						attacker.sendMessage(BGSColor.POSITIVE + "Back shot! +" + (int)((DAMAGE_MULTIPLIER - 1.0) * 100) + "% damage");
						victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}

	private boolean isAttackerBehindVictim(Player attacker, LivingEntity victim) {
		Vector victimDirection = victim.getLocation().getDirection().normalize();
		Vector victimToAttacker = attacker.getLocation().toVector()
				.subtract(victim.getLocation().toVector())
				.normalize();

		double dotProduct = victimDirection.dot(victimToAttacker);
		double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct)));
		double behindThreshold = Math.PI - MAX_BEHIND_ANGLE_RADIANS;

		return angle > behindThreshold;
	}
}
