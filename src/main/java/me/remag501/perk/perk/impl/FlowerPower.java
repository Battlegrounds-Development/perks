package me.remag501.perk.perk.impl;

import me.remag501.core.api.event.EventService;
import me.remag501.core.api.util.BGSColor;
import me.remag501.perk.perk.Perk;
import me.remag501.perk.perk.PerkType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class FlowerPower extends Perk {

	private final EventService eventService;

	public FlowerPower(EventService eventService) {
		super(PerkType.FLOWER_POWER);
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
				.handler(event -> {
					Player attacker = (Player) event.getDamager();
					int flowerCount = countFloralBlocks(attacker.getLocation());

					if (flowerCount > 0) {
						double multiplier = 1.0 + (0.05 * flowerCount);
						double newDamage = event.getDamage() * multiplier;
						event.setDamage(newDamage);
						attacker.sendMessage(BGSColor.POSITIVE + "Flower Power! +" + String.format("%.0f", (multiplier - 1) * 100) + "% damage");
					}
				});
	}

	@Override
	public void onDisable(Player player) {
		eventService.unregisterListener(player.getUniqueId(), getType().getId());
	}

	private int countFloralBlocks(Location loc) {
		int count = 0;
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					Block block = loc.clone().add(x, y, z).getBlock();
					if (isFloral(block.getType())) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private boolean isFloral(Material material) {
		return switch (material) {
			case DANDELION, POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP,
					PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY, SUNFLOWER, LILAC, ROSE_BUSH,
					PEONY, FERN, LARGE_FERN, TALL_GRASS, MOSS_BLOCK, MOSS_CARPET -> true;
			default -> false;
		};
	}
}
