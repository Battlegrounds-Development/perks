package me.remag501.perks.commands;

import me.remag501.perks.perktypes.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerksCompleter implements TabCompleter {

    private Plugin plugin;

    public PerksCompleter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("perks.admin"))
            return completions;

        if (args.length == 1) {
            // Base subcommands
            completions.addAll(Arrays.asList("reload", "add", "addpoints", "addcard", "remove", "hiddenui"));
        }

        // --- Handle second argument ---
        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add":
                case "addcard":
                case "remove":
                    // Show BOTH player names and perk names
                    for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
                    for (PerkType perk : PerkType.values()) completions.add(perk.name());
                    break;

                case "addpoints":
                    for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
                    break;
            }
        }

        // --- Handle third argument ---
        else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "add":
                case "addcard":
                case "remove":
                    // If 2nd argument is a player, then suggest perk types
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target != null) {
                        for (PerkType perk : PerkType.values()) {
                            completions.add(perk.name());
                        }
                    }
                    break;

                case "addpoints":
                    completions.add("<points>");
                    break;
            }
        }

        // Return partial matches
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .sorted()
                .toList();
    }

}
