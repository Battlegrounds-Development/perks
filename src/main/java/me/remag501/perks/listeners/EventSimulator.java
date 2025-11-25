package me.remag501.perks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

/**
 * Simulates a PlayerDeathEvent when a player clicks a sign that reads "fakekill <playername>"
 * on the first line. The clicking player is the implied killer, and the specified player is the victim.
 */
public class EventSimulator implements Listener {

    private static final String FAKE_KILL_COMMAND = "fakekill";

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only trigger on a right-click block action
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        BlockState state = clickedBlock.getState();

        // 1. Check if the clicked block is a Sign
        if (state instanceof Sign sign) {
            Player killer = event.getPlayer();
            String rawSignText = sign.getLine(0);

            if (rawSignText == null) return;

            // 2. Parse the command from the sign
            String command = ChatColor.stripColor(rawSignText).trim();
            if (!command.toLowerCase().startsWith(FAKE_KILL_COMMAND + " ")) {
                return; // Disregard signs that don't match the format
            }

            String[] parts = command.split(" ");
            if (parts.length < 2) {
                killer.sendMessage(ChatColor.RED + "SIMULATOR ERROR: Sign must specify a victim (e.g., /fakekill John).");
                return;
            }

            String victimName = parts[1];

            // 3. Find the victim (must be a real, online player)
            Player victim = Bukkit.getPlayer(victimName);

            if (victim == null) {
                killer.sendMessage(ChatColor.RED + "SIMULATOR ERROR: Victim '" + victimName + "' not found or is offline.");
                event.setCancelled(true);
                return;
            }

            // Prevent the killer from setting themselves as the victim, as it can corrupt their state.
//            if (victim.equals(killer)) {
//                killer.sendMessage(ChatColor.RED + "SIMULATOR: Congrats you killed yourself");
//                event.setCancelled(true);
//            }

            // --- ARITIFICAL EVENT FIRING ---

            victim.damage(0.1, killer);

            // 4. Create a new PlayerDeathEvent instance, using the found victim.
            PlayerDeathEvent deathEvent = new PlayerDeathEvent(
                    victim, // The real, non-clicking victim
                    new ArrayList<>(), // Drops (empty list)
                    0, // Exp (0)
                    null // Initial death message
            );

            // 5. Set a custom message indicating the simulated kill context.
            deathEvent.setDeathMessage(ChatColor.AQUA + victim.getName() +
                    " was fake-killed by " + killer.getName() + " for perk testing.");

            // Fire the event. The PackMaster listener will check deathEvent.getEntity().getKiller().
            // NOTE: For PackMaster to work, the victim's last damage cause *should* be set to the killer,
            // but for a PoC test, firing the event is usually sufficient.
            Bukkit.getPluginManager().callEvent(deathEvent);

            killer.sendMessage(ChatColor.GREEN + "SIMULATOR: Fired PlayerDeathEvent. Victim: " +
                    ChatColor.YELLOW + victim.getName());

            event.setCancelled(true); // Prevent any default sign interaction
        }
    }
}