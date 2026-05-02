package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class HotHandsPerk extends Perk {

	private static final int FIRE_TICKS = 50;

	private final EventService eventService;

	public HotHandsPerk(EventService eventService) {
		super(PerkType.HOT_HANDS);
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
				.filter(event -> !(event.getEntity() instanceof ArmorStand))
				.handler(event -> {
					Player damager = (Player) event.getDamager();
					LivingEntity target = (LivingEntity) event.getEntity();

					if (damager.getInventory().getItemInMainHand().getType() == Material.AIR) {
						target.setFireTicks(FIRE_TICKS);
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}
