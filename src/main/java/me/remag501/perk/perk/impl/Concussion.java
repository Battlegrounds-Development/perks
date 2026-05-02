package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class Concussion extends Perk {

	private final EventService eventService;

	public Concussion(EventService eventService) {
		super(PerkType.CONCUSSION);
		this.eventService = eventService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();
		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getDamager() instanceof Player p && p.getUniqueId().equals(uuid))
				.handler(event -> {
					if (!(event.getEntity() instanceof LivingEntity entity)) return;
					if (entity instanceof ArmorStand) return;
					Player damager = (Player) event.getDamager();
					if (damager.getInventory().getItemInMainHand() == null || damager.getInventory().getItemInMainHand().getType() == Material.AIR) {
						// Use NAUSEA if CONFUSION is unavailable on the server API
						entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
						damager.sendMessage(BGSColor.POSITIVE + "Concussion: target disoriented!");
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}

