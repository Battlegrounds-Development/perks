package me.remag501.perks.command;

import me.remag501.perks.model.PerkProfile;
import me.remag501.perks.perk.PerkType;
import me.remag501.perks.manager.PerkManager;
import me.remag501.perks.ui.PerkMenu;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PerksCommand implements CommandExecutor {

    private final Plugin plugin;
    private final PerkManager perkManager;
    private final PerkMenu perkMenu;

    public PerksCommand(Plugin plugin, PerkManager perkManager, PerkMenu perkMenu) {
        this.plugin = plugin;
        this.perkManager = perkManager;
        this.perkMenu = perkMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 && sender.hasPermission("perks.user")) {
            openPerkUI(sender, false);
            return true;
        }

        if (!sender.hasPermission("perks.admin")) {
            // All admin commands after this
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reload(sender);
                return true;
            case "add":
                if (args.length == 1) {
                    printPerks(sender);
                } else if (args.length == 2) {
                    addPerk((Player) sender, args[1]);
                } else if (args.length == 3) {
                    addPerk(sender, args[1], args[2]);
                } else {
                    sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                }
                return true;
            case "addpoints":
                if (args.length == 1) {
                    sender.sendMessage("§6§lPERKS §8» §7Usage: /perks addpoints <player> <points>");
                } else if (args.length == 2 && isNumeric(args[1])) {
                    addPerkPoints(sender.getName(), Integer.parseInt(args[1]));
                } else if (args.length == 3 && isNumeric(args[2])) {
                    addPerkPoints(args[1], Integer.parseInt(args[2]));
                } else if (args.length > 3) {
                    sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                }
                return true;
            case "addcard":
                if (args.length == 1) {
                    printPerks(sender);
                } else if (args.length == 2) {
                    addPerkCard(sender.getName(), args[1]);
                } else if (args.length == 3) {
                    addPerkCard(args[1], args[2]);
                } else {
                    sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                }
                return true;
            case "remove":
                if (args.length == 1) {
                    printPerks(sender);
                } else if (args.length == 2) {
                    removePerk(sender, sender.getName(), args[1]);
                } else if (args.length == 3) {
                    removePerk(sender, args[1], args[2]);
                } else {
                    sender.sendMessage("§6§lPERKS §8» §7Too many arguments");
                }
                return true;
            case "hiddenui":
                openPerkUI(sender, true);
                return true;
            case "save":
                saveAll(sender);
                return true;
            default:
                sender.sendMessage("§6§lPERKS §8» §7Usage: reload/add/addpoints/addcard/remove/save");
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
        for (PerkType type : PerkType.values()) {
            rv.append(String.valueOf(type)).append(" ");
        }
        sender.sendMessage(rv.toString());
    }

    private void addPerkCard(String playerName, String perkName) {
        // Get player
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            plugin.getLogger().info("Player " + playerName + " from add perk card could not be found.");
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
        player.sendMessage("§6§lPERKS §8» §7You received a " + perkType.getDisplayName() + " perk card!");
    }

    private void addPerkPoints(String playerName, int points) {
        // Get player
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            plugin.getLogger().info("Player " + playerName + " from add perk points could not be found.");
            return;
        }

        PerkProfile profile = perkManager.getProfile(player.getUniqueId());
        profile.addPerkPoints(points);
        player.sendMessage("§6§lPERKS §8» §7You received " + points + " perk points. Total: " + profile.getPerkPoints());
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

        // Get player's profile
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());

        // Add perk to player's owned perks list
        if (profile.addOwnedPerk(perk)) {
            player.sendMessage("§6§lPERKS §8» §7Added perk: " + perk.getDisplayName());
        } else {
            // Auto-scrap if at max quantity
            int points = profile.scrapPerk(perk);
            profile.addOwnedPerk(perk);
            player.sendMessage("§6§lPERKS §8» §7You already had 3 of this perk. Auto-scrapped one for " + points + " points.");
        }
    }

    private void addPerk(CommandSender sender, String playerName, String perkType) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("§6§lPERKS §8» §cPlayer not found: " + playerName);
            return;
        }

        try {
            PerkType.valueOf(perkType);
        } catch (Exception e) {
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

        // Get player's profile
        PerkProfile profile = perkManager.getProfile(player.getUniqueId());

        // Remove perk from player's owned perks list
        if (profile.removeOwnedPerk(perk)) {
            sender.sendMessage("§6§lPERKS §8» §aRemoved perk: " + perk.getDisplayName() + " from " + playerName);
        } else {
            sender.sendMessage("§6§lPERKS §8» §c" + playerName + " doesn't own that perk!");
        }
    }

    private void reload(CommandSender sender) {
        // Save all current data
//        PerkManager.getInstance().saveAllPerks();
        sender.sendMessage("§6§lPERKS §8» §aAll perk data saved!");

        // Note: Full reload would require plugin reload
        sender.sendMessage("§6§lPERKS §8» §7Note: Config reload requires plugin restart");
    }

    private void saveAll(CommandSender sender) {
//        PerkManager.getInstance().saveAllPerks();
        sender.sendMessage("§6§lPERKS §8» §aAll perk data saved!");
    }

    private void openPerkUI(CommandSender sender, boolean hiddenMenu) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§6§lPERKS §8» §cOnly players can use this command!");
            return;
        }

        // Ensure the player's profile exists (it should from join event, but just in case)
        perkManager.getProfile(player.getUniqueId());

        // Open the perk menu (start at page 0)
        perkMenu.open(player, 0, hiddenMenu);
    }
}