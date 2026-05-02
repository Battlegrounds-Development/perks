package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class Overdrive extends Perk {

	private final EventService eventService;

	public Overdrive(EventService eventService) {
		super(PerkType.OVERDRIVE);
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
				.filter(event -> !(event.getEntity() instanceof Player))
				.handler(event -> {
					LivingEntity target = (LivingEntity) event.getEntity();
					target.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0));
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}
