//package me.remag501.perks.manager;
//
//import me.remag501.perks.perk.PerkType;
//import me.remag501.perks.ui.GambleMenu;
//import org.bukkit.Bukkit;
//import org.bukkit.EntityEffect;
//import org.bukkit.Material;
//import org.bukkit.Sound;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.plugin.Plugin;
//
//import java.util.List;
//
//public class GambleManager {
//
//    private final Plugin plugin;
//
//    public GambleManager(Plugin plugin) {
//        this.plugin = plugin;
//    }
//
//    public void rollPerk(Player player, int rarity, int cost) {
//        PerkManager pm = PerkManager.getPlayerPerks(player.getUniqueId());
//
//        if (!pm.decreasePerkPoints(cost)) {
//            player.sendMessage("§cYou don't have enough points!");
//            return;
//        }
//
//        // RNG Logic
//        int roll = (int) (Math.random() * 100) + 1;
//        if (roll > 95) rarity += 2;
//        else if (roll > 80) rarity++;
//
//        int finalRarity = Math.min(rarity, 3);
//        List<PerkType> possible = PerkType.getPerksByRarity(finalRarity);
//        PerkType rolledType = possible.get((int) (Math.random() * possible.size()));
//
//        // Process Result
//        pm.addOwnedPerks(rolledType);
//        triggerTotemAnimation(player, rolledType);
//        player.sendMessage("§6§lPERKS §8» §7You obtained: " + rolledType.getItem().getItemMeta().getDisplayName());
//    }
//
//    private void triggerTotemAnimation(Player player, PerkType perkType) {
//        player.closeInventory();
//
//        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
//        ItemMeta meta = totem.getItemMeta();
//        int cmd = perkType.getItem().getItemMeta().getCustomModelData();
//
//        if (meta != null) {
//            meta.setCustomModelData(cmd);
//            totem.setItemMeta(meta);
//        }
//
//        ItemStack cache = player.getInventory().getItemInMainHand();
//        int slot = player.getInventory().getHeldItemSlot();
//
//        player.getInventory().setItem(slot, totem);
//        player.playEffect(EntityEffect.TOTEM_RESURRECT);
//        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
//        player.getInventory().setItem(slot, cache);
//
//        // Reopen Menu
//        Bukkit.getScheduler().runTaskLater(plugin, () -> GambleMenu.open(player), 45L);
//    }
//}