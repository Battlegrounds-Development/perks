package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.UUID;

public class Feral extends Perk {

	private final EventService eventService;
	private final PackMaster packMaster;

	public Feral(EventService eventService, PackMaster packMaster) {
		super(PerkType.FERAL);
		this.eventService = eventService;
		this.packMaster = packMaster;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();
		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getDamager() instanceof Player p && p.getUniqueId().equals(uuid))
				.handler(event -> {
					List<UUID> wolves = packMaster.getSummonedWolves(uuid);
					int numWolves = wolves.size();
					if (numWolves == 0) return;
					double dmgMult = 1.0 + numWolves * 0.05;
					event.setDamage(event.getDamage() * dmgMult);
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}

