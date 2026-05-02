package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.task.TaskService;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.UUID;

public class WolfBounded extends Perk {

	private final EventService eventService;
	private final TaskService taskService;
	private final PackMaster packMaster;

	public WolfBounded(EventService eventService, TaskService taskService, PackMaster packMaster) {
		super(PerkType.WOLF_BOUNDED);
		this.eventService = eventService;
		this.taskService = taskService;
		this.packMaster = packMaster;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID ownerId = player.getUniqueId();

		// Damage sharing for wolves owned by this player
		eventService.subscribe(EntityDamageEvent.class)
				.owner(ownerId)
				.namespace(getType().getId())
				.filter(event -> {
					Entity e = event.getEntity();
					return (e instanceof Wolf wolf && wolf.getOwner() != null && ownerId.equals(wolf.getOwner().getUniqueId()));
				})
				.handler(event -> {
					Entity e = event.getEntity();
					if (!(e instanceof Wolf wolf)) return;
					double dmg = event.getDamage();
					List<UUID> wolves = packMaster.getSummonedWolves(ownerId);
					if (wolves.isEmpty()) return;

					double shared = dmg / wolves.size();
					for (UUID id : wolves) {
						if (id.equals(wolf.getUniqueId())) continue;
						Entity other = Bukkit.getEntity(id);
						if (other instanceof Wolf w && w.isValid()) {
							double newHealth = w.getHealth() - shared;
							if (newHealth > 0) w.setHealth(newHealth);
						}
					}

					// set event damage to shared amount
					event.setDamage(shared);
				});

		// Periodic heal + particles
		taskService.subscribe(ownerId, getType().getId(), 0, 20, (ticks) -> {
			List<UUID> wolves = packMaster.getSummonedWolves(ownerId);
			for (UUID id : wolves) {
				Entity e = Bukkit.getEntity(id);
				if (e instanceof Wolf w) {
					double health = Math.min(w.getHealth() + 0.5, w.getMaxHealth());
					w.setHealth(health);
					w.getWorld().spawnParticle(Particle.FIREWORK, w.getLocation(), 1, 0.5, 0.5, 0.5, 0.01);
				}
			}
			return false;
		});
	}

	@Override
	public void onDisable(Player player) {
		UUID ownerId = player.getUniqueId();
		taskService.stopTask(ownerId, getType().getId());
		eventService.unregisterListener(ownerId, getType().getId());
	}
}

