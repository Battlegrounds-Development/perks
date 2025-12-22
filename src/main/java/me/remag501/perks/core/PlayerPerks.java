package me.remag501.perks.core;

import me.remag501.perks.utils.ConfigUtil;
import me.remag501.perks.utils.Items;
import me.remag501.perks.listeners.PerkChangeListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerPerks {

    private static HashMap<UUID, PlayerPerks> playersPerks = new HashMap<>();
    private UUID playerUUID;
//    private Set<PlayerPerkInstance> equippedPerks;
//    private List<Perk> ownedPerks;
    private Map<PerkType, Perk> equippedPerks;
    private Map<PerkType, Perk> ownedPerks;
    private int perkPoints;

    public static PlayerPerks getPlayerPerks(UUID playerUUID) {
        return playersPerks.get(playerUUID);
    }

    public void loadPerks(Player player) {
        String playerID = player.getUniqueId().toString();
        ConfigUtil perkConfig = new ConfigUtil(Bukkit.getPluginManager().getPlugin("Perks"), "perks.yml");
        List<String> equippedPerks = perkConfig.getConfig().getStringList(playerID + "_equipped");
        List<String> ownedPerks = perkConfig.getConfig().getStringList(playerID + "_owned");
        String perkPointString = ownedPerks.remove(ownedPerks.size() - 1); // Pops last item O(1) for perk points
        this.perkPoints = Integer.parseInt(perkPointString);
        // Convert to Perk ArrayList and set this update this instance to existing perks
        for (String perk: ownedPerks) // Owned Perks have to be called first
            this.addOwnedPerks(PerkType.valueOf(perk));
        for (String perk: equippedPerks)
            this.addEquippedPerk(PerkType.valueOf(perk));
    }

    public static void savePerks() {
        ConfigUtil perkConfigUtil = new ConfigUtil(Bukkit.getPluginManager().getPlugin("Perks"), "perks.yml");
        FileConfiguration perkConfig = perkConfigUtil.getConfig();
        // Iterate through set of perks for each player
        for (UUID playerID: playersPerks.keySet()) {
            PlayerPerks playerPerk = playersPerks.get(playerID);
            String playerIDString = playerID.toString();
            // Convert to list to set in the config file
            List<String> save = new ArrayList<>();
            for (Perk perk: playerPerk.getEquippedPerks()) {
                if (perk.isStarPerk()) {
                    for (int i = 0; i < perk.getStars(); i++)
                        save.add(Items.getPerkID(perk.getItem()));
                } else
                    save.add(Items.getPerkID(perk.getItem()));
            }
            perkConfig.set(playerIDString + "_equipped", save);
            // Add owned perks to config
            save = new ArrayList<>();
            for (Perk perk: playerPerk.getOwnedPerks())
                save.add(Items.getPerkID(perk.getItem()));
            save.add(playerPerk.perkPoints + ""); // Save perk points under owned
            perkConfig.set(playerIDString + "_owned", save);
        }
        // Save the config to the file
        perkConfigUtil.save();
    }

    // Return list for ease of UI handling, requires optimization in future
    public List<Perk> getEquippedPerks() {
        return new ArrayList<>(equippedPerks.values()); // Keys and values are same amount, values easier since they are perk object already
    }

    // Return list for ease of UI handling, requires optimization in future
    public List<Perk> getOwnedPerks() {
        ArrayList<Perk> rv = new ArrayList<Perk>();
        for (Perk perkInstance: ownedPerks.values()) { // Values contain the quantities
            for (int i = 0; i < perkInstance.getQuantity(); i++)
                rv.add(perkInstance); // Should work with clone of object due to having same ItemStack pointer
        }
        return rv;
    }

    // Should only be on player join
    public PlayerPerks(UUID playerUUID) {
        equippedPerks = new HashMap<PerkType, Perk>();
        ownedPerks = new HashMap<PerkType, Perk>();
        playersPerks.put(playerUUID, this);
        this.playerUUID = playerUUID;
        if (playerUUID != null)
            loadPerks(Bukkit.getPlayer(playerUUID));
    }

    public boolean removeEquippedPerk(PerkType perkType) {
        Perk perk = equippedPerks.get(perkType); // Check if perk is equipped
        if (perk == null) return false;

        // If star perk with more than 1 star, just remove one star
        if (perk.getStars() > 1) {
            if (!perk.decreaseStar())
                return false; // Increase star fails
            // Re instantiate the perk with new stars
        } else {
            // Disable the perk before removing it
            equippedPerks.remove(perkType);
        }

        // Check if another perk depends on this one, now that this perk is removed
//        Set<PerkType> equippedSet = equippedPerks.keySet();
//        for (PerkType equippedPerk: equippedSet) {
//            List<List<PerkType>> requirements = equippedPerk.getPerk().getRequirements();
//            if (requirements != null) {
//                List<PerkType> equippedPerkTypes = new ArrayList<>(equippedPerks.keySet());
//                for (List<PerkType> requirementList: requirements) {
//                    boolean foundPerk = false;
//                    for (PerkType requirement: requirementList) {
//                        if (equippedPerkTypes.contains(requirement)) {
//                            equippedPerkTypes.remove(requirement); // Prevent perk from filling two requirements
//                            foundPerk = true;
//                            break;
//                        }
//                    }
//                    if (!foundPerk) {
//                        Perk equippedPerkInstance = equippedPerks.get(equippedPerk);
//                        // Check if star perk and reset stars
//                        if (equippedPerkInstance.isStarPerk())
//                            equippedPerkInstance.setStar(1);
//                        // Disable the perk if the player is in world
//                        boolean inWorld = false;
//                        for (String world: PerkChangeListener.enabledWorlds) {
//                            if (Bukkit.getPlayer(playerUUID).getWorld().getName().equalsIgnoreCase(world)) {
//                                inWorld = true;
//                                break;
//                            }
//                        }
//                        if (!inWorld) {
//                            equippedPerks.remove(equippedPerk); // Player does not have required perk
//                            break;
//                        }
//                        equippedPerkInstance.onDisable();
//                        equippedPerks.remove(equippedPerk); // Player does not have required perk
//                        break; // Check if more than one perk depend on removed perk
//                    }
//                }
//            }
//        }

        Iterator<PerkType> iterator = equippedPerks.keySet().iterator();
        while (iterator.hasNext()) {
            PerkType equippedPerk = iterator.next();
            List<List<PerkType>> requirements = equippedPerk.getPerk().getRequirements();

            if (requirements != null) {
                List<PerkType> equippedPerkTypes = new ArrayList<>(equippedPerks.keySet());

                for (List<PerkType> requirementList : requirements) {
                    boolean foundPerk = false;

                    for (PerkType requirement : requirementList) {
                        if (equippedPerkTypes.contains(requirement)) {
                            equippedPerkTypes.remove(requirement); // Prevent perk from filling two requirements
                            foundPerk = true;
                            break;
                        }
                    }

                    if (!foundPerk) {
                        Perk equippedPerkInstance = equippedPerks.get(equippedPerk);

                        // Check if star perk and reset stars
                        if (equippedPerkInstance.isStarPerk())
                            equippedPerkInstance.setStar(1);

                        // Disable the perk if the player is in world
                        boolean inWorld = false;
                        for (String world : PerkChangeListener.enabledWorlds) {
                            if (Bukkit.getPlayer(playerUUID).getWorld().getName().equalsIgnoreCase(world)) {
                                inWorld = true;
                                break;
                            }
                        }

                        if (!inWorld) {
                            iterator.remove(); // Safe removal using iterator
                            break;
                        }

                        equippedPerkInstance.deactivatePlayer();
                        equippedPerkInstance.onDisable();
                        iterator.remove(); // Safe removal using iterator
                        break; // Check if more than one perk depends on removed perk
                    }
                }
            }
        }



        // Message the player
        Player player = Bukkit.getPlayer(playerUUID);
        player.sendMessage("§6§lPERKS §8» §7You have deequipped the perk " + perkType.getItem().getItemMeta().getDisplayName());

        // Check if they are in the correct world before disabling
        boolean inWorld = false;
        for (String world: PerkChangeListener.enabledWorlds) {
            if (Bukkit.getPlayer(playerUUID).getWorld().getName().equalsIgnoreCase(world)) {
                inWorld = true;
                break;
            }
        }
        if (!inWorld)
            return true;
        perk.onDisable();
        perk.deactivatePlayer();
        if (perk.isStarPerk() == true && perk.getStars() > 1) {
            perk.onEnable();
            perk.activatePlayer();
        }
        return true;
    }

    public boolean addEquippedPerk(PerkType perkType) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (equippedPerks.size() >= 5)
            return false;
        Perk perkInstance = equippedPerks.get(perkType);

        // Check if perk has requirements equipped
        List<List<PerkType>> requirements = perkType.getPerk().getRequirements();
        if (requirements != null) {
            List<PerkType> equippedPerkTypes = new ArrayList<>(equippedPerks.keySet());
            for (List<PerkType> requirementList: requirements) {
                boolean foundPerk = false;
                for (PerkType requirement: requirementList) {
                    if (equippedPerkTypes.contains(requirement)) {
                        equippedPerkTypes.remove(requirement); // Prevent perk from filling two requirements
                        foundPerk = true;
                        break;
                    }
                }
                if (!foundPerk)
                    return foundPerk; // Player does not have required perk
            }
        }

        // Check if star perk
        if (perkInstance != null && perkInstance.isStarPerk() && perkInstance.getStars() < 3) {
            if (!perkInstance.increaseStar())
                return false; // Increase star fails
        }

        // Check if perk is already equipped
        else if (perkInstance != null)
            return false;

        // Add perks to set
        perkInstance = ownedPerks.get(perkType);
        if (perkInstance == null) return false; // Just in case
        equippedPerks.put(perkType, perkInstance);
        player.sendMessage("§6§lPERKS §8» §7You have equipped the perk " + perkType.getItem().getItemMeta().getDisplayName());

        // Only enables if the player is in the correct world
        boolean inWorld = false;
        for (String world: PerkChangeListener.enabledWorlds) {
            if (Bukkit.getPlayer(playerUUID).getWorld().getName().equalsIgnoreCase(world)) {
                inWorld = true;
                break;
            }
        }
        if (!inWorld)
            return true; // Equipped but not enabled
        perkInstance.onEnable();
        perkInstance.activatePlayer();

        return true;
    }

    public boolean addOwnedPerks(PerkType perkType) {
        Player player = Bukkit.getPlayer(playerUUID);
        Perk perkInstance = ownedPerks.get(perkType);

        if (perkInstance != null) { // Perk already owned
            if (!perkInstance.addCount()) { // Perk deck is full, scrap one to add later
                int points = scrapPerks(perkType);
                perkInstance.addCount(); // Add again since scrap removes a perk
                player.sendMessage("§6§lPERKS §8» §7You do not have enough storage for this perk, so it was automatically converted to " + points + " perk points.");
                return false;
            }
            return true;

        } else {
            perkInstance = perkType.getPerk().clone();
            perkInstance.setPlayer(player);
            ownedPerks.put(perkType, perkInstance);
            return true;
        }
    }

    public int scrapPerks(PerkType perkType) {
        removeOwnedPerk(perkType);
        int perkAdd = 0;
        switch (Items.getRarity(perkType)) {
            case 0:
                perkAdd = 1;
                break;
            case 1:
                perkAdd = 2;
                break;
            case 2:
                perkAdd = 3;
                break;
            case 3:
                perkAdd = 5;
                break;
            default:
                break;
        }
        perkPoints += perkAdd;
        return perkAdd;
    }

    public void removeOwnedPerk(PerkType perkType) {
        // Doesnt need count checker since remove leaves array the same if the value is not found
        // O(1) complexity
        if (ownedPerks.get(perkType) == null)
            return; // No perk found
        Perk perk = ownedPerks.get(perkType);
        if (perk.getQuantity() > 1) {
            perk.lowerCount();
            if (perk.getStars() > perk.getQuantity())
                removeEquippedPerk(perkType);
        }
        else { // Removes from HashMap and unequips
            ownedPerks.remove(perkType);
            removeEquippedPerk(perkType);
        }
    }

    public void addPerkPoints(int count) {
        perkPoints += count;
    }

    public int getPerkPoints() {
        return perkPoints;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean decreasePerkPoints(int points) {
        if (perkPoints >= points)
            perkPoints -= points;
        else
            return false;
        return true;
    }
}
