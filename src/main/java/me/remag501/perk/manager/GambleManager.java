package me.remag501.perk.manager;

import me.remag501.perk.model.PerkProfile;
import me.remag501.perk.perk.PerkType;
import me.remag501.perk.registry.PerkRegistry;
import me.remag501.perk.ui.GambleMenu;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class GambleManager {

    private final Plugin plugin;
    private final PerkManager perkManager;
    private final PerkRegistry perkRegistry;
    private final GambleMenu gambleMenu;

    public GambleManager(Plugin plugin, PerkManager perkManager, PerkRegistry perkRegistry, GambleMenu gambleMenu) {
        this.plugin = plugin;
        this.perkManager = perkManager;
        this.perkRegistry = perkRegistry;
        this.gambleMenu = gambleMenu;
    }

    public void rollPerk(Player player, int rarity, int cost) {
        PerkProfile perkProfile = perkManager.getProfile(player.getUniqueId());

        if (!perkProfile.spendPerkPoints(cost)) {
            player.sendMessage("§cYou don't have enough points!");
            return;
        }

        // RNG Logic
        int roll = (int) (Math.random() * 100) + 1;
        if (roll > 95) rarity += 2;
        else if (roll > 80) rarity++;

        int finalRarity = Math.min(rarity, 3);
        List<PerkType> possible = perkRegistry.getPerksByRarity(finalRarity);
        PerkType rolledType = possible.get((int) (Math.random() * possible.size()));

        // Process Result
        if (perkProfile.addOwnedPerk(rolledType)) {
            player.sendMessage("§6§lPERKS §8» §7You obtained: " + perkRegistry.getPerkItem(rolledType).getItemMeta().getDisplayName());
        } else {
            int points = perkProfile.scrapPerk(rolledType);
            perkProfile.addOwnedPerk(rolledType);
            player.sendMessage("§6§lPERKS §8» §7You already had 3 of this perk. Auto-scrapped one for " + points + " points.");
        }

        triggerTotemAnimation(player, rolledType);

    }

    private void triggerTotemAnimation(Player player, PerkType perkType) {
        player.closeInventory();

        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        int cmd = perkType.getCustomModelData();

        if (meta != null) {
            meta.setCustomModelData(cmd);
            totem.setItemMeta(meta);
        }

        ItemStack cache = player.getInventory().getItemInMainHand();
        int slot = player.getInventory().getHeldItemSlot();

        player.getInventory().setItem(slot, totem);
        player.playEffect(EntityEffect.TOTEM_RESURRECT);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        player.getInventory().setItem(slot, cache);

        // Reopen Menu
        Bukkit.getScheduler().runTaskLater(plugin, () -> gambleMenu.open(player), 45L);
    }
}