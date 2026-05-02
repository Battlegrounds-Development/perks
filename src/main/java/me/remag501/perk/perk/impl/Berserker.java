package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Berserker extends Perk {

	private static final double DAMAGE_INCREASE_PER_HIT = 0.10;
	private static final int LOG_WINDOW_HITS = 5;

	private final Map<UUID, Queue<Double>> fistDamageLog = new ConcurrentHashMap<>();
	private final EventService eventService;

	public Berserker(EventService eventService) {
		super(PerkType.BERSERKER);
		this.eventService = eventService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();
		fistDamageLog.put(uuid, new ArrayDeque<>());

		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getDamager() instanceof Player p && p.getUniqueId().equals(uuid))
				.handler(event -> {
					Queue<Double> log = fistDamageLog.get(uuid);
					if (log != null) {
						log.offer(event.getDamage());
						if (log.size() > LOG_WINDOW_HITS) {
							log.poll();
						}
						double bonusDamage = log.size() * DAMAGE_INCREASE_PER_HIT;
						double newDamage = event.getDamage() * (1.0 + bonusDamage);
						event.setDamage(newDamage);
						player.sendMessage(BGSColor.POSITIVE + "Berserker stacked! (" + log.size() + "/5)");
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		fistDamageLog.remove(player.getUniqueId());
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}
