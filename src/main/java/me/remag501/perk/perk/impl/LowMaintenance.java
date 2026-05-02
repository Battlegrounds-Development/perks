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

public class LowMaintenance extends Perk {

	private static final int SATURATION_DURATION = 300; // 15 seconds
	private static final int TASK_INTERVAL = 2400; // 2 minutes

	private final TaskService taskService;
	private final PerkManager perkManager;

	public LowMaintenance(TaskService taskService, PerkManager perkManager) {
		super(PerkType.LOW_MAINTENANCE);
		this.taskService = taskService;
		this.perkManager = perkManager;
	}

	@Override
	public void onEnable(Player player, int stars) {
		taskService.subscribe(player.getUniqueId(), getType().getId(), 0, TASK_INTERVAL, (ticks) -> {
			applySaturation(player);
			return false;
		});
	}

	@Override
	public void onDisable(Player player) {
		taskService.stopTask(player.getUniqueId(), getType().getId());
		player.removePotionEffect(PotionEffectType.SATURATION);
	}

	private void applySaturation(Player player) {
		if (!player.isOnline()) {
			return;
		}

		PerkProfile profile = perkManager.getProfile(player.getUniqueId());
		if (!profile.isActive(getType())) {
			return;
		}

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, SATURATION_DURATION, 0, false, false));
		player.sendMessage(BGSColor.POSITIVE + "You feel well-fed thanks to Low Maintenance!");
	}
}
