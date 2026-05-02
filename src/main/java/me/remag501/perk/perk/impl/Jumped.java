package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Jumped extends Perk {

	private static final long FIRST_HIT_WINDOW_MILLIS = 500;
	private static final Map<UUID, Long> LAST_HIT_TIME = new ConcurrentHashMap<>();

	private final EventService eventService;
	private final PackMaster packMaster;

	public Jumped(EventService eventService, PackMaster packMaster) {
		super(PerkType.JUMPED);
		this.eventService = eventService;
		this.packMaster = packMaster;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID playerId = player.getUniqueId();
		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(playerId)
				.namespace(getType().getId())
				.filter(event -> event.getDamager() instanceof Player p && p.getUniqueId().equals(playerId))
				.handler(event -> handleFirstHit(event, player));
	}

	private void handleFirstHit(EntityDamageByEntityEvent event, Player player) {
		UUID playerId = player.getUniqueId();
		long currentTime = System.currentTimeMillis();
		Long last = LAST_HIT_TIME.get(playerId);
		if (last != null && (currentTime - last) < FIRST_HIT_WINDOW_MILLIS) return;
		LAST_HIT_TIME.put(playerId, currentTime);

		if (!(event.getEntity() instanceof LivingEntity victim)) return;

		List<UUID> wolves = packMaster.getSummonedWolves(playerId);
		if (wolves.isEmpty()) return;

		Location targetLoc = victim.getLocation().add(0.0, 0.5, 0.0);
		int teleported = 0;
		for (UUID wolfId : wolves) {
			Entity e = Bukkit.getEntity(wolfId);
			if (e instanceof org.bukkit.entity.Wolf w && w.isTamed() && player.equals(w.getOwner())) {
				w.teleport(targetLoc);
				teleported++;
			}
		}

		if (teleported > 0) {
			player.sendMessage(BGSColor.POSITIVE + "JUMPED! " + (victim.getCustomName() != null ? victim.getCustomName() : victim.getType().name()) + " is being jumped by " + teleported + " wolves!");
		}
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}
