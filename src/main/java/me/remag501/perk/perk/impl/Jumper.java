package me.remag501.perk.perk.impl;

import me.remag501.core.api.task.TaskService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.manager.PerkManager;
import me.remag501.perk.model.PerkProfile;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Jumper extends Perk {

	private static final int SLOWNESS_INTERVAL = 1800; // 1.5 minutes
	private static final int SLOWNESS_DURATION = 100; // 5 seconds

	private final TaskService taskService;
	private final PerkManager perkManager;

	public Jumper(TaskService taskService, PerkManager perkManager) {
		super(PerkType.JUMPER);
		this.taskService = taskService;
		this.perkManager = perkManager;
	}

	@Override
	public void onEnable(Player player, int stars) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, false, false));

		taskService.subscribe(player.getUniqueId(), getType().getId(), SLOWNESS_INTERVAL, SLOWNESS_INTERVAL, (ticks) -> {
			applySlowness(player);
			return false;
		});
	}

	@Override
	public void onDisable(Player player) {
		taskService.stopTask(player.getUniqueId(), getType().getId());
		player.removePotionEffect(PotionEffectType.JUMP_BOOST);
		player.removePotionEffect(PotionEffectType.SLOWNESS);
	}

	private void applySlowness(Player player) {
		if (!player.isOnline()) {
			return;
		}

		PerkProfile profile = perkManager.getProfile(player.getUniqueId());
		if (!profile.isActive(getType())) {
			return;
		}

		player.sendMessage(BGSColor.NEGATIVE + "You feel tired from jumping!");
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOWNESS_DURATION, 0, false, true));
	}
}
