package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PackMaster extends Perk {

	private final EventService eventService;
	private final Map<UUID, List<UUID>> summonedWolves = new ConcurrentHashMap<>();

	public PackMaster(EventService eventService) {
		super(PerkType.PACK_MASTER);
		this.eventService = eventService;

		// Global listener to clean up dead wolves and remove them from owner tracking
		eventService.subscribe(EntityDeathEvent.class)
				.handler(event -> {
					Entity entity = event.getEntity();
					if (!(entity instanceof Wolf wolf)) return;
					if (wolf.getOwner() == null) return;
					UUID ownerId = wolf.getOwner().getUniqueId();
					List<UUID> list = summonedWolves.get(ownerId);
					if (list != null) {
						list.remove(wolf.getUniqueId());
					}
				});
	}

	@Override
	public void onEnable(Player player, int stars) {
		summonedWolves.put(player.getUniqueId(), new CopyOnWriteArrayList<>());

		UUID uuid = player.getUniqueId();
		// Subscribe to their kills (per-player) to summon wolves
		eventService.subscribe(PlayerDeathEvent.class)
				.owner(uuid)
				.namespace(getType().getId())
				.filter(event -> {
					Player killer = event.getEntity().getKiller();
					return killer != null && killer.getUniqueId().equals(uuid);
				})
				.handler(event -> {
					Player killer = event.getEntity().getKiller();
					if (killer == null) return;
					summonWolf(killer);
				});
	}

	@Override
	public void onDisable(Player player) {
		UUID uuid = player.getUniqueId();
		despawnAllWolves(uuid);
		summonedWolves.remove(uuid);
		eventService.unregisterListener(uuid, getType().getId());
	}

	private void summonWolf(Player owner) {
		UUID ownerId = owner.getUniqueId();
		List<UUID> list = summonedWolves.computeIfAbsent(ownerId, k -> new CopyOnWriteArrayList<>());

		if (list.size() >= 10) {
			owner.playSound(owner.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 1f);
			// find lowest hp wolf and heal
			Wolf minWolf = null;
			double minHP = Double.MAX_VALUE;
			for (UUID id : list) {
				Entity e = Bukkit.getEntity(id);
				if (e instanceof Wolf w && w.isValid()) {
					if (w.getHealth() < minHP) {
						minHP = w.getHealth();
						minWolf = w;
					}
				}
			}
			if (minWolf != null) {
				minWolf.setHealth(minWolf.getMaxHealth());
				owner.sendMessage(BGSColor.NEGATIVE + "You have reached the maximum wolves; healed lowest-HP wolf.");
			}
			return;
		}

		Location loc = owner.getLocation();
		Wolf wolf = (Wolf) owner.getWorld().spawnEntity(loc, EntityType.WOLF);
		wolf.setTamed(true);
		wolf.setOwner(owner);

		owner.sendMessage(BGSColor.POSITIVE + "A new wolf joins your pack!");
		owner.playSound(owner.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1f, 1f);

		list.add(wolf.getUniqueId());
	}

	private void despawnAllWolves(UUID ownerId) {
		List<UUID> list = summonedWolves.get(ownerId);
		if (list == null || list.isEmpty()) return;
		for (UUID id : list) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
		}
		list.clear();
	}

	public List<UUID> getSummonedWolves(UUID ownerId) {
		return summonedWolves.getOrDefault(ownerId, List.of());
	}
}

