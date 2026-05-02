package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Undead extends Perk {

	private static final int ABSORPTION_DURATION_TICKS = 1200;
	private static final int ABSORPTION_HEARTS = 4;
	private static final long ASSIST_WINDOW_MILLIS = 5000L;

	private static class AttackData {
		final UUID attackerId;
		final long timestamp;

		AttackData(UUID attackerId) {
			this.attackerId = attackerId;
			this.timestamp = System.currentTimeMillis();
		}
	}

	private final Map<UUID, AttackData> lastAttack = new ConcurrentHashMap<>();
	private final EventService eventService;

	public Undead(EventService eventService) {
		super(PerkType.UNDEAD);
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
				.handler(event -> lastAttack.put(event.getEntity().getUniqueId(), new AttackData(uuid)));

		eventService.subscribe(EntityDeathEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.handler(event -> {
					UUID victimId = event.getEntity().getUniqueId();
					AttackData data = lastAttack.remove(victimId);
					if (data == null || System.currentTimeMillis() - data.timestamp > ASSIST_WINDOW_MILLIS) {
						return;
					}

					if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent dmg) {
						if (dmg.getDamager().getUniqueId().equals(data.attackerId)) {
							return;
						}
					}

					Player assistor = event.getEntity().getWorld().getPlayers().stream()
							.filter(p -> p.getUniqueId().equals(data.attackerId) && p.isOnline())
							.findFirst()
							.orElse(null);

					if (assistor != null) {
						assistor.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, ABSORPTION_DURATION_TICKS, ABSORPTION_HEARTS / 4 - 1, false, true));
						assistor.sendMessage(BGSColor.POSITIVE + "The shadows reward your assist! Gained §d" + ABSORPTION_HEARTS + " §aAbsorption hearts.");
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
		lastAttack.clear();
	}
}
