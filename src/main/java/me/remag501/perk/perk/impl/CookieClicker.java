package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CookieClicker extends Perk {

	private final EventService eventService;

	public CookieClicker(EventService eventService) {
		super(PerkType.COOKIE_CLICKER);
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
					killer.sendMessage(BGSColor.POSITIVE + "How tasty!");
					World world = event.getEntity().getWorld();
					world.dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.COOKIE, 2));
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}

