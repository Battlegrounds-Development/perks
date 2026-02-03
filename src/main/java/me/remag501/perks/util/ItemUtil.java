package me.remag501.perks.util;

import me.remag501.perks.perk.PerkType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ItemUtil {

    // Top of ItemUtil
    public static final NamespacedKey PERK_ID_KEY = new NamespacedKey(
            Bukkit.getPluginManager().getPlugin("Perks"), "unique_id"
    );

    /*

              _____                                _           _
             |  __ \                              | |         | |
             | |  | | ___ _ __  _ __ ___  ___ __ _| |_ ___  __| |
             | |  | |/ _ \ '_ \| '__/ _ \/ __/ _` | __/ _ \/ _` |
             | |__| |  __/ |_) | | |  __/ (_| (_| | ||  __/ (_| |
             |_____/ \___| .__/|_|  \___|\___\__,_|\__\___|\__,_|
                         | |
                         |_|

     Scheduled for removal soon

     */
    public static ItemStack createPerkItem(Material type, String name, String id, int rarity, String... lores) {
        String rarityStr;
        switch (rarity) {
            case 0:
                rarityStr = "§f§lCommon";
                break;
            case 1:
                rarityStr = "§a§lUncommon";
                break;
            case 2:
                rarityStr = "§1§lRare";
                break;
            case 3:
                rarityStr = "§6§lLegendary";
                break;
            case 4:
                rarityStr = "§8§lHidden";
                break;
            default:
                rarityStr = "§7Unknown";
                break;
        }
        // Prepend the lores with rarityStr
        ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lores));
        loreList.add(0, rarityStr);
        ItemStack item = ItemUtil.createItem(type, name, id, false, loreList.toArray(new String[loreList.size()]));
        // Add tag for hidden rarity
        if (rarityStr.equals("§8§lHidden")) {
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "rarity");
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(key, PersistentDataType.STRING, "HIDDEN");
            item.setItemMeta(meta);
        }
        return item;
    }


    // Add identifier to this function arguments
    public static ItemStack createItem(Material type, String name, String id, boolean enchanted, String... lores) {
        // Function to make creating items easier

        // Example uses an IRON_SWORD as a placeholder item
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();

        // Set the item's name and lore to describe the perk
        if (meta != null) {
            // Add lore to the item
            meta.setDisplayName(name);
            ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lores));
            meta.setLore(loreList);
            // Add unique identifier to the item
            if (id != null) {
                NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(key, PersistentDataType.STRING, id);
            }
            // Make item look enchanted
            if (enchanted) {
                meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            // Apply meta data to item
            item.setItemMeta(meta);
        }
        return item;
    }

    /*

              _    _           _       _           _
             | |  | |         | |     | |         | |
             | |  | |_ __   __| | __ _| |_ ___  __| |
             | |  | | '_ \ / _` |/ _` | __/ _ \/ _` |
             | |__| | |_) | (_| | (_| | ||  __/ (_| |
              \____/| .__/ \__,_|\__,_|\__\___|\__,_|
                    | |
                    |_|

     Soon to replace old functions without custom model data

     */


    public static ItemStack createPerkItem(int cmd, String name, String id, int rarity, String... lores) {
        String rarityStr;
        switch (rarity) {
            case 0:
                rarityStr = "§f§lCommon";
                break;
            case 1:
                rarityStr = "§a§lUncommon";
                break;
            case 2:
                rarityStr = "§1§lRare";
                break;
            case 3:
                rarityStr = "§6§lLegendary";
                break;
            case 4:
                rarityStr = "§8§lHidden";
                break;
            default:
                rarityStr = "§7Unknown";
                break;
        }
        // Prepend the lores with rarity Str
        ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lores));
        loreList.replaceAll(s -> "§8• §f" + s);
        loreList.add(0, rarityStr);
        // Make lores same color
        ItemStack item = ItemUtil.createItem(Material.PAPER, "§e§o" + name, id, cmd, false, loreList.toArray(new String[loreList.size()]));
        // Add tag for hidden rarity
        if (rarityStr.equals("§8§lHidden")) {
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "rarity");
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(key, PersistentDataType.STRING, "HIDDEN");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static int getRarity(PerkType perkType) {
        return perkType.getRarity();
    }

    public static ItemStack createPerkSkull(String texture, String name, String id, int rarity, String... lores) {
        ItemStack head = createPerkItem(Material.PLAYER_HEAD, name, id, rarity, lores);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        // Set the custom texture
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID().toString());
        PlayerTextures playerTexture = profile.getTextures();
        try {
            URL url = new URL(texture);
            playerTexture.setSkin(url);
            profile.setTextures(playerTexture);
            skullMeta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().severe("Invalid skin URL: " + texture);
            e.printStackTrace();
        }
        head.setItemMeta(skullMeta);

        return head;
    }

    public static ItemStack createPerkSkull(UUID uuid, String name, String id, String... lores) {
        ItemStack head = createPerkItem(Material.PLAYER_HEAD, name, id, 0, lores);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        // Remove the rarity lore
        skullMeta.setLore(new ArrayList<>());

        // Set the custom player texture using UUID
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));

        head.setItemMeta(skullMeta);

        return head;
    }

    // Add identifier to this function arguments
    public static ItemStack createItem(Material type, String name, String id, int cmd, boolean enchanted, String... lores) {
        // Function to make creating items easier

        // Example uses an IRON_SWORD as a placeholder item
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();

        // Set the item's name and lore to describe the perk
        if (meta != null) {
            // Add lore to the item
            meta.setDisplayName(name);
            meta.setCustomModelData(cmd);
            ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lores));
            meta.setLore(loreList);
            // Add unique identifier to the item
            if (id != null) {
                NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(key, PersistentDataType.STRING, id);
            }
            // Make item look enchanted
            if (enchanted) {
                meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            // Apply meta data to item
            item.setItemMeta(meta);
        }
        return item;
    }

//    public static ItemStack getPerkCard(PerkType perkType) {
//        // Clone the original item to avoid modifying the base item
//        ItemStack item = perkType.getItem();
//        ItemStack perkCard = item.clone();
//        perkCard.setType(Material.PAPER);
//        ItemMeta meta = perkCard.getItemMeta();
//
//        if (meta != null) {
//            // Get the rarity color from previous item
//            ItemMeta itemMeta = item.getItemMeta();
//            String firstLine = itemMeta.getLore().get(0);
//            char colorCode = firstLine.charAt(1);
//            // Update the display name to represent the card
//            meta.setDisplayName("§" + colorCode + "§l" + itemMeta.getDisplayName());
//            meta.setCustomModelData(perkType.getCustomModelData());
//
//            // Add lore for clarity
//            List<String> lore = new ArrayList<>();
//            lore.add(ChatColor.RED + "You will obtain this perk when you extract!");
//            meta.setLore(lore);
//
//            perkCard.setItemMeta(meta);
//        }
//
//        return perkCard;
//    }

    public static ItemStack getPerkCard(PerkType perkType) {
        ItemStack perkCard = new ItemStack(Material.PAPER);
        ItemMeta meta = perkCard.getItemMeta();

        if (meta != null) {
            // Use the PerkType data directly instead of type.getItem()
            String rarityColor = getRarityColor(perkType.getRarity());
            meta.setDisplayName(rarityColor + "§l" + perkType.getDisplayName());
            meta.setCustomModelData(perkType.getCustomModelData());

            List<String> lore = new ArrayList<>();
            lore.add("§cYou will obtain this perk when you extract!");
            meta.setLore(lore);

            perkCard.setItemMeta(meta);
        }
        return perkCard;
    }

    // Helper to replace your 'char colorCode' logic
    private static String getRarityColor(int rarity) {
        switch (rarity) {
            case 1: return "§a"; // Uncommon
            case 2: return "§b"; // Rare
            case 3: return "§6"; // Legendary
            default: return "§f"; // Common
        }
    }

    public static List<PerkType> itemsToPerks(PlayerInventory inventory) {
        List<PerkType> foundPerks = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            String id = getPerkID(item);
            if (id != null) {
                try {
                    PerkType type = PerkType.valueOf(id.toUpperCase());
                    for (int i = 0; i < item.getAmount(); i++) {
                        foundPerks.add(type);
                    }
                    item.setAmount(0);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return foundPerks;
    }

//    public static String getPerkID(ItemStack item) {
//        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
//        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
//        String id = container.get(key, PersistentDataType.STRING);
//        return id;
//    }

    // Then in your getPerkID method:
    public static String getPerkID(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(PERK_ID_KEY, PersistentDataType.STRING);
    }

    // Function to check if two ItemStacks are equal based on their PersistentDataContainer
    public static boolean areItemsEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false; // One of the items is null, so they're not equal
        }

        if (item1.getType() == Material.BEDROCK || item2.getType() == Material.BEDROCK) {
            return false; // Prevent any comparison with unavailable perks
        }

        // Get the ItemMeta for both items
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 == null || meta2 == null) {
            return false; // One of the items has no metadata, so they're not equal
        }

        // Create a NamespacedKey for your custom identifier
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");

        // Get the PersistentDataContainers from both items
        PersistentDataContainer data1 = meta1.getPersistentDataContainer();
        PersistentDataContainer data2 = meta2.getPersistentDataContainer();

        // Check if both items have the custom key in their PersistentDataContainer
        if (data1.has(key, PersistentDataType.STRING) && data2.has(key, PersistentDataType.STRING)) {
            String id1 = data1.get(key, PersistentDataType.STRING);
            String id2 = data2.get(key, PersistentDataType.STRING);

            // Compare the unique IDs
            return id1 != null && id1.equals(id2);
        }

        return false; // The custom data isn't present or doesn't match
    }

    // Function to check if ItemStack contains hidden rarity key
    public static boolean hiddenItem(ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "rarity");
        String id = container.get(key, PersistentDataType.STRING);
        if (id == null)
            return false; // No custom data is present or the custom data isn't a hidden rarity key
        return id.equals("HIDDEN");
    }

    // ==================== UPDATED METHODS FOR NEW ARCHITECTURE ====================

    /**
     * Update the item's count based on owned perks.
     * NEW: Takes List<PerkType> and the specific type to count.
     */
    public static void updateCount(ItemStack item, List<PerkType> ownedPerks, PerkType type) {
        // Count how many of this specific type the player owns
        int count = (int) ownedPerks.stream().filter(t -> t == type).count();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // If the count is 0, make the item a bedrock block (unavailable)
        if (count == 0) {
            item.setType(Material.BEDROCK);
            // Update meta data identifier
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.remove(key);
        } else {
            // Update the item's count in lore
            List<String> loreList = meta.getLore();
            if (loreList == null) loreList = new ArrayList<>();

            if (meta.hasEnchants()) { // Checks if selected
                loreList.add(1, "§7Perks: " + count + "/3");
            } else {
                loreList.add(0, "§7Perks: " + count + "/3");
            }
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
    }

    /**
     * Update star count display on item.
     * NEW: Takes PerkType and star count directly.
     */
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

    /**
     * Update equip status display on item.
     * NEW: Takes Map<PerkType, Integer> for equipped perks.
     */
    public static void updateEquipStatus(ItemStack item, Map<PerkType, Integer> equippedPerks) {
        // Identify the perk using the PDC tag you already have
        String id = getPerkID(item);
        if (id == null) return;

        try {
            PerkType itemType = PerkType.valueOf(id.toUpperCase());
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            boolean equipped = equippedPerks.containsKey(itemType);

            if (equipped) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                // Avoid double-adding
                if (!lore.contains("§c§lEquipped")) {
                    lore.add(0, "§c§lEquipped");
                }
                meta.setLore(lore);
            } else {
                meta.removeEnchant(Enchantment.LUCK);
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.remove("§c§lEquipped");
                    meta.setLore(lore);
                }
            }
            item.setItemMeta(meta);
        } catch (IllegalArgumentException ignored) {}
    }

    /**
     * Update requirements display on item.
     * NEW: Takes Map<PerkType, Integer> for equipped perks.
     */
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
}