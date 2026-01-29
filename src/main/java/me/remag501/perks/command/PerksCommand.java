package me.remag501.perks.command;

import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.util.ItemUtil;
import me.remag501.perks.listener.PerkMenuListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PerksCommand implements CommandExecutor {

    private Plugin plugin;
    private Map<String, String> messages;
    private Map<UUID, PerkManager> playerPerks;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player) && !args[0].equalsIgnoreCase("hiddenui")) {
//            sender.sendMessage("§6§lPERKS §8» §7This command can only be executed by players.");
//            return true;
//        }
        if (args.length == 0 && sender.hasPermission("perks.user")) {
            openPerkUI(sender, false);
            return true;
        }

        if (!sender.hasPermission("perks.admin")) // All admin command after this
            return true;

        switch (args[0].toLowerCase()) {
            case "reload":
                reload();
                return true;
            case "add":
                if (args.length == 1)
                    printPerks(sender);
                else if (args.length == 2)
                    addPerk((Player) sender, args[1]);
                else if (args.length == 3)
                    addPerk(sender, args[1], args[2]);
                else sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                return true;
            case "addpoints":
                if (args.length == 1)
                    sender.sendMessage("§6§lPERKS §8» §7Usage: /perks addpoints <player> <points>");
                else if (args.length == 2 && isNumeric(args[1]))
                    addPerkPoints(sender.getName(), Integer.parseInt(args[1]));
                else if (args.length == 3 && isNumeric(args[2]))
                    addPerkPoints(args[1], Integer.parseInt(args[2]));
                else if (args.length > 3)
                    sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                return true;
            case "addcard":
                if (args.length == 1)
                    printPerks(sender);
                else if (args.length == 2)
                    addPerkCard(sender.getName(), args[1]);
                else if (args.length == 3)
                    addPerkCard(args[1], args[2]);
                else sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                return true;
            case "remove":
                if (args.length == 1)
                    printPerks(sender);
                else if (args.length == 2)
                    removePerk(sender, sender.getName(), args[1]);
                else if (args.length == 3)
                    removePerk(sender, args[1], args[2]);
                else sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                return true;
            case "hiddenui":
                openPerkUI(sender, true);
                return true;
            default:
                sender.sendMessage("§6§lPERKS §8» §7Usage: reload/add/addpoints/addcard/remove");
                return true;
        }

    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private void printPerks(CommandSender sender) {
        sender.sendMessage("You need to specify a perk type");
        StringBuilder rv = new StringBuilder();
        for (PerkType type: PerkType.values()) {
            rv.append(String.valueOf(type)).append(" ");
        }
        sender.sendMessage(rv.toString());
    }

    private void addPerkCard(String playerName, String perkName) {

        // Get PlayerPerks object
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getPluginManager().getPlugin("Perks").getLogger().info("Player " + playerName + " from add perk card could not be found.");
            return;
        }

        // Get perk from command arguments
        PerkType perkType;
        try {
            perkType = PerkType.valueOf(perkName);
        } catch (Exception e) {
            player.sendMessage("§6§lPERKS §8» §cInvalid perk type: " + perkName);
            return;
        }

        // Get the perk card itemstack and give to player
        player.getInventory().addItem(ItemUtil.getPerkCard(perkType));

    }

    private void addPerkPoints(String playerName, int points) {
        // Get PlayerPerks object
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getPluginManager().getPlugin("Perks").getLogger().info("Player " + playerName + " from add perk points could not be founds.");
            return;
        }
        PerkManager perkManager = PerkManager.getPlayerPerks(player.getUniqueId());
        perkManager.addPerkPoints(points);
        player.sendMessage("§6§lPERKS §8» §7You recieved " + points + " perk points.");
    }

    private void addPerk(Player player, String perkName) {
        // Get perk from command arguments
        PerkType perk;
        try {
            perk = PerkType.valueOf(perkName);
        } catch (Exception e) {
            player.sendMessage("§6§lPERKS §8» §cInvalid perk type: " + perkName);
            return;
        }
        // Gets object of PlayerPerks from UUID
        PerkManager perkManager = PerkManager.getPlayerPerks(player.getUniqueId());
//        if (playerPerks == null) {
//            playerPerks = new PlayerPerks(((Player) player).getUniqueId());
//        } Add perks should not instantinate PlayerPerks
        // Add perk to players owned perks list
        if(perkManager.addOwnedPerks(perk))
            player.sendMessage("§6§lPERKS §8» §7Added perk: " + perkName);
//        else
//            player.sendMessage("You have cannot have more than three perk cards");
    }

    private void addPerk(CommandSender sender, String playerName, String perkType) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("§6§lPERKS §8» §cPlayer not found: " + playerName);
            return;
        }
        try {PerkType.valueOf(perkType);}
        catch (Exception e) {
            sender.sendMessage("§6§lPERKS §8» §cInvalid perk type: " + perkType);
            return;
        }
        addPerk(player, perkType);
    }

    private void removePerk(CommandSender sender, String playerName, String perkType) {
        // Get player from player name
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("§6§lPERKS §8» §cPlayer not found: " + playerName);
            return;
        }
        // Get perk from command arguments
        PerkType perk;
        try {
            perk = PerkType.valueOf(perkType);
        } catch (Exception e) {
            sender.sendMessage("§6§lPERKS §8» §cInvalid perk type: " + perkType);
            return;
        }
        sender.sendMessage("§6§lPERKS §8» §cRemoved perk: " + perkType);
        // Gets object of PlayerPerks from UUID
        PerkManager perkManager = PerkManager.getPlayerPerks(player.getUniqueId());
        if (perkManager == null) {
            perkManager = new PerkManager(player.getUniqueId());
        }
        // Remove perk from players owned perks list
        perkManager.removeOwnedPerk(perk);
    }

    private void reload() {
        // Load from config file and player data
        // Perk data class describes what perks each player has available
//        PlayerPerks.savePerks();
    }

    private void openPerkUI(CommandSender sender, boolean hiddenMenu) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§6§lPERKS §8» §cOnly players can use this command!");
            return;
        }

        Player player = (Player) sender;
        // Open the inventory for the player
        PerkManager perks = PerkManager.getPlayerPerks(player.getUniqueId());
        if (perks == null) {
            perks = new PerkManager(player.getUniqueId());
        }
        PerkMenuListener ui = new PerkMenuListener(PerkManager.getPlayerPerks(player.getUniqueId()), hiddenMenu);
        Inventory perkMenu = ui.getPerkMenu();
        player.openInventory(perkMenu);
    }

    public PerksCommand(Plugin plugin) {
        this.plugin = plugin;
    }


}
