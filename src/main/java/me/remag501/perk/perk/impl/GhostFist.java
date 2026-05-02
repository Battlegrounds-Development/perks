package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.task.TaskService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GhostFist extends Perk {

	private final EventService eventService;
	private final TaskService taskService;

	// Track entities currently receiving an echo (prevents infinite recursion)
	private final Set<UUID> echoing = ConcurrentHashMap.newKeySet();

	// Track per-player cooldowns (3 seconds)
	private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
	private static final long COOLDOWN_MS = 3000;

	public GhostFist(EventService eventService, TaskService taskService) {
		super(PerkType.GHOST_FIST);
		this.eventService = eventService;
		this.taskService = taskService;
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

					// Prevent recursion: if this specific damage event is the "echo" itself, ignore it
					if (echoing.contains(entity.getUniqueId())) return;

					Player damager = (Player) event.getDamager();

					// Requirement: Must be an empty hand
					if (damager.getInventory().getItemInMainHand().getType() != Material.AIR)
						return;

					// Cooldown Check
					long now = System.currentTimeMillis();
					long lastUsed = cooldowns.getOrDefault(uuid, 0L);
					if (now - lastUsed < COOLDOWN_MS) return;

					double damage = event.getDamage();

					// Set cooldown immediately upon primary hit to prevent multiple procs
					// from fast clicking before the task even runs.
					cooldowns.put(uuid, now);

					// Schedule delayed echo
					taskService.delay(11, () -> {
						if (entity.isDead()) return;
						UUID entId = entity.getUniqueId();

						try {
							echoing.add(entId); // Start recursion guard
							entity.damage(damage, damager);
							damager.sendMessage(BGSColor.POSITIVE + "Ghost Fist echoes through the air!");
						} finally {
							echoing.remove(entId); // End recursion guard
						}
					});
				});
	}

	@Override
	public void onDisable(Player player) {
		UUID uuid = player.getUniqueId();
		eventService.unregisterListener(uuid, getType().getId());
		cooldowns.remove(uuid);
	}
}