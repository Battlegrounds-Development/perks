package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class XPFarm extends Perk {

	private final EventService eventService;

	public XPFarm(EventService eventService) {
		super(PerkType.XP_FARM);
		this.eventService = eventService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();
		eventService.subscribe(PlayerDeathEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> {
					Player killer = event.getEntity().getKiller();
					return killer != null && killer.getUniqueId().equals(uuid);
				})
				.handler(event -> {
					Player killer = event.getEntity().getKiller();
					if (killer == null) return;
					killer.sendMessage(BGSColor.POSITIVE + "You received 300xp for neutralizing a player!");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clv addEXP 300 " + killer.getName());
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}

