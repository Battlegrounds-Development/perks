package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Serendipity extends Perk {

	private static final double PROC_CHANCE = 0.20;

	private final EventService eventService;

	public Serendipity(EventService eventService) {
		super(PerkType.SERENDIPITY);
		this.eventService = eventService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();

		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getEntity().getUniqueId().equals(uuid))
				.filter(event -> !(event.getDamager() instanceof Player))
				.handler(event -> {
					if (ThreadLocalRandom.current().nextDouble() < PROC_CHANCE) {
						event.setCancelled(true);
						player.sendMessage(BGSColor.POSITIVE + "Serendipity activated! You took no damage.");
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}
