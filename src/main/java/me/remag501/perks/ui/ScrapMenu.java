//package me.remag501.perks.ui;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//
//public class ScrapMenu {
//
//    public static void open(Player player) {
//        Inventory inv = Bukkit.createInventory(null, 9, "Confirm Scrap");
//
//        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
//        ItemMeta confirmMeta = confirm.getItemMeta();
//        if (confirmMeta != null) {
//            confirmMeta.setDisplayName("§a§lCONFIRM");
//            confirm.setItemMeta(confirmMeta);
//        }
//
//        ItemStack cancel = new ItemStack(Material.RED_WOOL);
//        ItemMeta cancelMeta = cancel.getItemMeta();
//        if (cancelMeta != null) {
//            cancelMeta.setDisplayName("§c§lCANCEL");
//            cancel.setItemMeta(cancelMeta);
//        }
//
//        inv.setItem(3, cancel);
//        inv.setItem(5, confirm);
//
//        player.openInventory(inv);
//    }
//}