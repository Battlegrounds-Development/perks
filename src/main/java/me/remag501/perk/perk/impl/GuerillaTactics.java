package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.task.TaskService;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuerillaTactics extends Perk {

	private static final long CHANNEL_MILLIS = 3000L;
	private static final int INVIS_INTERVAL = 40;
	private static final int INVIS_DURATION = 45;
	private static final int RADIUS = 1;

	private final Map<UUID, Long> sneakStartTime = new ConcurrentHashMap<>();

	private final EventService eventService;
	private final TaskService taskService;

	public GuerillaTactics(EventService eventService, TaskService taskService) {
		super(PerkType.GUERRILLA_TACTICS);
		this.eventService = eventService;
		this.taskService = taskService;
	}

	@Override
	public void onEnable(Player player, int stars) {
		UUID uuid = player.getUniqueId();

		eventService.subscribe(PlayerToggleSneakEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.handler(event -> {
					if (event.isSneaking()) {
						if (!isInTacticalPosition(event.getPlayer().getLocation())) {
							return;
						}
						sneakStartTime.put(uuid, System.currentTimeMillis());
						event.getPlayer().sendMessage("§a§l(!) §aChanneling stealth...");

						taskService.subscribe(uuid, getType().getId(), 0, 5, (ticks) -> {
							checkAndActivate(event.getPlayer());
							return false;
						});
					} else {
						cancelStealth(event.getPlayer());
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		cancelStealth(player);
		sneakStartTime.remove(player.getUniqueId());
		taskService.stopTask(player.getUniqueId(), getType().getId());
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}

	private void checkAndActivate(Player player) {
		UUID uuid = player.getUniqueId();
		Long startTime = sneakStartTime.get(uuid);

		if (startTime != null) {
			if (System.currentTimeMillis() - startTime >= CHANNEL_MILLIS) {
				sneakStartTime.remove(uuid);
				player.sendMessage("§a§l(!) §aConcealment achieved!");
				taskService.subscribe(uuid, getType().getId(), 0, INVIS_INTERVAL, (ticks) -> {
					if (player.isSneaking() && isInTacticalPosition(player.getLocation())) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVIS_DURATION, 0, false, false));
					} else {
						cancelStealth(player);
					}
					return false;
				});
			} else if (!player.isSneaking() || !isInTacticalPosition(player.getLocation())) {
				player.sendMessage("§c§l(!) §cChannel interrupted.");
				cancelStealth(player);
			}
		}
	}

	private void cancelStealth(Player player) {
		sneakStartTime.remove(player.getUniqueId());
		taskService.stopTask(player.getUniqueId(), getType().getId());
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	private boolean isInTacticalPosition(Location loc) {
		int baseY = loc.getBlockY();
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int z = -RADIUS; z <= RADIUS; z++) {
				for (int y = -1; y <= 1; y++) {
					Material mat = loc.getWorld().getBlockAt(loc.getBlockX() + x, baseY + y, loc.getBlockZ() + z).getType();
					if (isFloral(mat)) return true;
				}
			}
		}
		return false;
	}

	private boolean isFloral(Material mat) {
		return switch (mat) {
			case TALL_GRASS, FERN, OAK_LEAVES, POPPY, DANDELION -> true;
			default -> false;
		};
	}
}

