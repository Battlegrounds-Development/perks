package me.remag501.perks.utils;

import me.remag501.perks.core.Perk;
import me.remag501.perks.core.PerkType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Items {

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
//        List<String> loreList = new ArrayList<>(List.of(rarityStr));
//        loreList.addAll(Arrays.asList(lores));
        ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lores));
        loreList.add(0, rarityStr);
                ItemStack item = Items.createItem(type, name, id, false, loreList.toArray(new String[loreList.size()]));
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
            // Apply meta deta to item
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
        ItemStack item = Items.createItem(Material.PAPER, "§e§o" + name, id, cmd,false, loreList.toArray(new String[loreList.size()]));
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
        switch(perkType.getItem().getItemMeta().getLore().get(0).charAt(1)) {
            case 'f':
                return 0;
            case 'a':
                return 1;
            case '1':
                return 2;
            case '6':
                return 3;
            default:
                break;
        }
        return -1;
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
//        PlayerProfile profile = Bukkit.createPlayerProfile(uuid);
//        skullMeta.setOwnerProfile(profile);
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
            // Apply meta deta to item
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getPerkCard(PerkType perkType) {
        // Clone the original item to avoid modifying the base item
        ItemStack item = perkType.getItem();
        ItemStack perkCard = item.clone();
        perkCard.setType(Material.PAPER);
        ItemMeta meta = perkCard.getItemMeta();

        if (meta != null) {
            // Get the rarity color from previous item
            ItemMeta itemMeta = item.getItemMeta();
            String firstLine = itemMeta.getLore().get(0);
            char colorCode = firstLine.charAt(1);
            // Update the display name to represent the card
            meta.setDisplayName("§" + colorCode + "§l" + itemMeta.getDisplayName());
//            meta.setCustomModelData(perkType.get);
            meta.setCustomModelData(perkType.getItem().getItemMeta().getCustomModelData()); // Super ineffiecnt since nbt data should be stored in perktype
            // Add lore for clarity
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "You will obtain this perk when you extract!");
            meta.setLore(lore);

            // Store the perk type in the item's PersistentDataContainer (might already exist from being a clone)
//            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
//            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, item.getType().toString());

            perkCard.setItemMeta(meta);
        }

        return perkCard;
    }

    public static List<PerkType> itemsToPerks(PlayerInventory inventory) {
        List<PerkType> perkTypes = new ArrayList<>();
        List<ItemStack> perkCards = new ArrayList<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            for (PerkType type : PerkType.values()) {
                ItemStack perkItem = type.getPerk().getItem();
                if (areItemsEqual(item, perkItem)) {
                    for (int i = 0; i < item.getAmount(); i++) {
                        perkTypes.add(type);
                        inventory.remove(item);
//                        perkCards.add(item); // Save to remove later
                    }
                    break;
                }
            }
        }

//        for (ItemStack item: perkCards) // Remove perk cards from inventory
//            inventory.remove(item);

        return perkTypes;
    }

    public static String getPerkID(ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
        String id = container.get(key, PersistentDataType.STRING);
        return id;
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
//        Bukkit.getPluginManager().getPlugin("Perks").getLogger().info(data1.get(key, PersistentDataType.STRING) + " " + data2.get(key, PersistentDataType.STRING));
        if (data1.has(key, PersistentDataType.STRING) && data2.has(key, PersistentDataType.STRING)) {
            String id1 = data1.get(key, PersistentDataType.STRING);
            String id2 = data2.get(key, PersistentDataType.STRING);
//

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

    public static void updateCount(ItemStack item, List<Perk> perks) {
        // Update the item's count based on the number of perks the player has
        int count = 0;
        boolean starPerk = false;
        for (Perk perk : perks) {
            if (areItemsEqual(item, perk.getItem())) {
                count++;
                starPerk = perk.isStarPerk();
            }
        }
        // Check if meta is null
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        // If the count is 0, then make the item a bedrock block
        if (count == 0) {
            item.setType(Material.BEDROCK);
            // Update meta data identifier
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Perks"), "unique_id");
            PersistentDataContainer data = meta.getPersistentDataContainer();;
            data.remove(key);
//            Bukkit.getPluginManager().getPlugin("Perks").getLogger().info(data.get(key, PersistentDataType.STRING));

        }
//        else if (starPerk) {
//            // Update the item's stars
//            List<String> loreList = meta.getLore();
//            StringBuilder starStr = new StringBuilder();
//            for (int i = 0; i < 3; i++) {
//                if (i <= count-1)
//                    starStr.append("★");
//                else
//                    starStr.append("☆");
//            }
//            if (meta.hasEnchants()) // Checks if selected
//                loreList.add(1, String.valueOf(starStr));
//            else
//                loreList.add(0, String.valueOf(starStr));
//            meta.setLore(loreList);
//        }
        else {
            // Update the item's count based on the number of perks the player has
            List<String> loreList = meta.getLore();
            if (meta.hasEnchants()) // Checks if selected
                loreList.add(1, "§7Perks: " + count + "/3");
            else
                loreList.add(0, "§7Perks: " + count + "/3");
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
    }

    public static void updateStarCount(ItemStack item, List<Perk> equippedPerks) {
        int stars = 0;
        for (Perk perk: equippedPerks) {
            if (perk.getItem().equals(item) && perk.isStarPerk()) {
                stars = perk.getStars();
                break;
            }
        }
        if (stars == 0)
            return; // Perk is not equipped or a star perk
        // Get item meta
        ItemMeta meta = item.getItemMeta();
        // Build a star string
        List<String> loreList = meta.getLore();
        StringBuilder starStr = new StringBuilder("§6");
        for (int i = 0; i < 3; i++) {
            if (i <= stars-1)
                starStr.append("★");
            else
                starStr.append("☆");
        }
        if (meta.hasEnchants()) // Checks if selected
            loreList.add(1, String.valueOf(starStr));
        else
            loreList.add(0, String.valueOf(starStr));
        meta.setLore(loreList);
        item.setItemMeta(meta);
    }

    public static void updateEquipStatus(ItemStack item, List<Perk> equippedPerks) {
        // Check if perk is equipped
        boolean equipped = false;
        for (Perk perk: equippedPerks){
            if (areItemsEqual(item, perk.getItem())) {
                equipped = true;
                break;
            }
        }
        // Get the meta of the item
        ItemMeta meta = item.getItemMeta();
        // Enchant the item then add lore to show its equipped
        if (equipped) {
            // Enchant
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            // Get previous lore
            List<String> lore = meta.getLore();
            if (lore == null)
                lore = new ArrayList<String>();
            lore.add(0, "§c§lEquipped");
            meta.setLore(lore);
        } else {
            meta.removeEnchant(Enchantment.LUCK);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            // Get previous lore
            List<String> lore = meta.getLore();
            if (lore != null)
                lore.remove("§4Equipped");
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public static void updateRequirements(ItemStack item, List<Perk> equippedPerks, PerkType perkType) {
        List<List<PerkType>> requirements = perkType.getPerk().getRequirements();
        if (requirements == null)
            return; // Perk has no requirements
        ItemMeta meta = item.getItemMeta();
        List<String> loreList = meta.getLore();

        // Build lore for requirements
        loreList.add(""); // Add line break
//        loreList.add("§fRequirements: ");
        for (List<PerkType> requirement: requirements) {
            StringBuilder requirementString = new StringBuilder();

            // Check if the player has the required perk and build string
            boolean meetsRequirements = false;
            for (PerkType perkRequired: requirement) {
                requirementString.append(perkRequired.getItem().getItemMeta().getDisplayName().replace("§e§o", "")).append(", ");
                if (equippedPerks.remove(perkRequired.getPerk())) // Prevent double dipping requirements
                    meetsRequirements = true;
            }

            // Insert prefix based on whether player meets requirement
            if (meetsRequirements) {
                loreList.add("§f§aRequirements: ");
                requirementString.insert(0, "§a + ");
            }
            else {
                loreList.add("§f§cRequirements: ");
                requirementString.insert(0, "§c - ");
            }
            requirementString.deleteCharAt(requirementString.length()-2); // Remove second last character, the "," at the end of string
            loreList.add(requirementString.toString());
        }

        // Update lore
        meta.setLore(loreList);
        item.setItemMeta(meta);
    }

}
