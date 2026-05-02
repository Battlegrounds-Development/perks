package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.task.TaskService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.manager.PerkManager;
import me.remag501.perk.model.PerkProfile;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Resistant extends Perk {

	private static final int CHECK_INTERVAL = 200;
	private static final int AMPLIFIER = 0;

	private final Map<UUID, PlayerResistantState> playerStates = new ConcurrentHashMap<>();

	private final EventService eventService;
	private final TaskService taskService;
	private final PerkManager perkManager;

	public Resistant(EventService eventService, TaskService taskService, PerkManager perkManager) {
		super(PerkType.RESISTANT);
		this.eventService = eventService;
		this.taskService = taskService;
		this.perkManager = perkManager;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();
		playerStates.put(uuid, new PlayerResistantState(thresholdForStars(stars)));

		taskService.subscribe(uuid, getType().getId(), 0, CHECK_INTERVAL, (ticks) -> {
			checkHealthAndApplyEffect(player);
			return false;
		});

		eventService.subscribe(EntityDamageEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getEntity().getUniqueId().equals(uuid))
				.handler(event -> taskService.delay(1, () -> checkHealthAndApplyEffect(player)));

		eventService.subscribe(EntityRegainHealthEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getEntity().getUniqueId().equals(uuid))
				.handler(event -> taskService.delay(1, () -> checkHealthAndApplyEffect(player)));

		eventService.subscribe(EntityPotionEffectEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> event.getEntity().getUniqueId().equals(uuid))
				.handler(event -> taskService.delay(1, () -> checkHealthAndApplyEffect(player)));

		checkHealthAndApplyEffect(player);
	}

	@Override
	public void onDisable(Player player) {
		UUID uuid = player.getUniqueId();

		PlayerResistantState state = playerStates.get(uuid);
		if (state != null && state.active) {
			removeResistantEffect(player, state);
		}

		taskService.stopTask(uuid, getType().getId());
		eventService.unregisterListener(uuid, getType().getId());
		playerStates.remove(uuid);
	}

	private void checkHealthAndApplyEffect(Player player) {
		if (!player.isOnline()) {
			return;
		}

		UUID uuid = player.getUniqueId();
		PlayerResistantState state = playerStates.get(uuid);
		if (state == null) {
			return;
		}

		PerkProfile profile = perkManager.getProfile(uuid);
		if (!profile.isActive(getType())) {
			return;
		}

		double healthPercent = player.getHealth() / player.getMaxHealth();
		if (player.getHealth() > 0 && healthPercent <= state.healthThreshold) {
			if (!state.active) {
				applyResistantEffect(player, state);
			}
		} else if (state.active) {
			removeResistantEffect(player, state);
		}
	}

	private void applyResistantEffect(Player player, PlayerResistantState state) {
		PotionEffect existing = player.getPotionEffect(PotionEffectType.RESISTANCE);
		if (existing != null) {
			if (existing.getAmplifier() > AMPLIFIER) {
				return;
			}
			state.savedDuration = existing.getDuration();
			state.savedAmplifier = existing.getAmplifier();
		}

		state.active = true;
		player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, AMPLIFIER, false, false));
		player.sendMessage(BGSColor.POSITIVE + "You are resistant due to low HP!");
	}

	private void removeResistantEffect(Player player, PlayerResistantState state) {
		state.active = false;

		PotionEffect current = player.getPotionEffect(PotionEffectType.RESISTANCE);
		if (current != null && current.getAmplifier() == AMPLIFIER && current.getDuration() > 500) {
			player.removePotionEffect(PotionEffectType.RESISTANCE);

			if (state.savedDuration > 0) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, state.savedDuration, state.savedAmplifier, false, false));
			}
		}

		state.savedDuration = 0;
		state.savedAmplifier = 0;
		player.sendMessage(BGSColor.NEGATIVE + "Your resistance fades as you heal.");
	}

	private double thresholdForStars(int stars) {
		return switch (stars) {
			case 2 -> 0.25;
			case 3 -> 0.30;
			default -> 0.20;
		};
	}

	private static class PlayerResistantState {
		final double healthThreshold;
		boolean active;
		int savedDuration;
		int savedAmplifier;

		PlayerResistantState(double healthThreshold) {
			this.healthThreshold = healthThreshold;
		}
	}
}
