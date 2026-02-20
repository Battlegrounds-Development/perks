package me.remag501.perk.util;

import me.remag501.perk.perk.PerkType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemUtil {

    public static void updateStarCount(ItemStack item, PerkType type, int stars) {
        if (!type.isStarPerk() || stars == 0) {
            return; // Not a star perk or not equipped
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Build star string
        List<String> loreList = meta.getLore();
        if (loreList == null) loreList = new ArrayList<>();

        StringBuilder starStr = new StringBuilder("§6");
        for (int i = 0; i < 3; i++) {
            if (i < stars) {
                starStr.append("★");
            } else {
                starStr.append("☆");
            }
        }

        if (meta.hasEnchants()) { // Checks if equipped
            loreList.add(1, starStr.toString());
        } else {
            loreList.add(0, starStr.toString());
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);
    }


    public static void updateRequirements(ItemStack item, Map<PerkType, Integer> equippedPerks, PerkType perkType) {
        List<List<PerkType>> requirements = perkType.getRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return; // Perk has no requirements
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> loreList = meta.getLore();
        if (loreList == null) loreList = new ArrayList<>();

        // Build lore for requirements
        loreList.add(""); // Add line break

        // Create a copy of equipped perks to track which ones we've "used"
        Set<PerkType> availablePerks = new HashSet<>(equippedPerks.keySet());

        for (List<PerkType> requirementGroup : requirements) {
            StringBuilder requirementString = new StringBuilder();

            // Check if the player has at least one required perk from this group
            boolean meetsRequirement = false;
            for (PerkType perkRequired : requirementGroup) {
                requirementString.append(perkRequired.getDisplayName()).append(", ");

                if (availablePerks.contains(perkRequired)) {
                    availablePerks.remove(perkRequired); // Prevent double-dipping requirements
                    meetsRequirement = true;
                }
            }

            // Insert prefix based on whether player meets requirement
            if (meetsRequirement) {
                loreList.add("§f§aRequirements: ");
                requirementString.insert(0, "§a + ");
            } else {
                loreList.add("§f§cRequirements: ");
                requirementString.insert(0, "§c - ");
            }

            // Remove the trailing ", "
            if (requirementString.length() > 2) {
                requirementString.delete(requirementString.length() - 2, requirementString.length());
            }

            loreList.add(requirementString.toString());
        }

        // Update lore
        meta.setLore(loreList);
        item.setItemMeta(meta);
    }

    // Helper to replace your 'char colorCode' logic
    public static String getRarityColor(int rarity) {
        return switch (rarity) {
            case 1 -> "§a"; // Uncommon
            case 2 -> "§b"; // Rare
            case 3 -> "§6"; // Legendary
            default -> "§f"; // Common
        };
    }

}
