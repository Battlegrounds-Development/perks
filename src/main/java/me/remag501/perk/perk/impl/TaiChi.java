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
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaiChi extends Perk {

	private static final Map<UUID, Long> fistStartTimes = new ConcurrentHashMap<>();
	private final EventService eventService;

	public TaiChi(EventService eventService) {
		super(PerkType.TAI_CHI);
		this.eventService = eventService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();

		// Track held-fist timing on hotbar switch
		eventService.subscribe(PlayerItemHeldEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getPlayer().getUniqueId().equals(uuid))
				.handler(event -> {
					Player p = event.getPlayer();
					if (p.getInventory().getItem(event.getNewSlot()) == null || p.getInventory().getItem(event.getNewSlot()).getType() == Material.AIR) {
						fistStartTimes.put(uuid, System.currentTimeMillis());
					} else {
						fistStartTimes.remove(uuid);
					}
				});

		// Apply effect on hit if held long enough
		eventService.subscribe(EntityDamageByEntityEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getDamager() instanceof Player p && p.getUniqueId().equals(uuid))
				.handler(event -> {
					if (!(event.getEntity() instanceof LivingEntity entity)) return;
					if (entity instanceof ArmorStand) return;
					Player damager = (Player) event.getDamager();
					Long started = fistStartTimes.get(uuid);
					if (started != null && System.currentTimeMillis() - started >= 3000) {
						entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
						entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
						damager.sendMessage(BGSColor.POSITIVE + "Tai Chi activated!");
						// reset tracking so it's not applied continuously
						fistStartTimes.remove(uuid);
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		fistStartTimes.remove(player.getUniqueId());
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}
}

