package me.remag501.perks.service;

import me.remag501.perks.perk.PerkType;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Bukkit;
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

import java.util.*;

public class ItemService {

    private final NamespaceService namespaceService;

    public ItemService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }


    public ItemStack createPerkItem(int cmd, String name, String id, int rarity, String... lores) {

        String rarityStr = switch (rarity) {
            case 0 -> "§f§lCommon";
            case 1 -> "§a§lUncommon";
            case 2 -> "§1§lRare";
            case 3 -> "§6§lLegendary";
            case 4 -> "§8§lHidden";
            default -> "§7Unknown";
        };

        // Prepend the lores with rarity Str
        ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lores));
        loreList.replaceAll(s -> "§8• §f" + s);
        loreList.add(0, rarityStr);
        // Make lores same color
        ItemStack item = createItem(Material.PAPER, "§e§o" + name, id, cmd, false, loreList.toArray(new String[loreList.size()]));
        // Add tag for hidden rarity
        if (rarityStr.equals("§8§lHidden")) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(namespaceService.RARITY_KEY, PersistentDataType.STRING, "HIDDEN");
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createItem(Material type, String name, String id, int cmd, boolean enchanted, String... lores) {
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
                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(namespaceService.PERK_ID_KEY, PersistentDataType.STRING, id);
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

    public ItemStack createItem(Material type, String name, String id, boolean enchanted, String... lores) {
        return createItem(type, name, id, 0, enchanted, lores);
    }


    public ItemStack createSkull(UUID uuid, String name, String id, String... lores) {
        ItemStack head = createItem(Material.PLAYER_HEAD, name, id,false, lores);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        // Remove the rarity lore
        skullMeta.setLore(new ArrayList<>());

        // Set the custom player texture using UUID
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));

        head.setItemMeta(skullMeta);

        return head;
    }

    public ItemStack getPerkCard(PerkType perkType) {
        ItemStack perkCard = new ItemStack(Material.PAPER);
        ItemMeta meta = perkCard.getItemMeta();

        if (meta != null) {
            // Use the PerkType data directly instead of type.getItem()
            String rarityColor = ItemUtil.getRarityColor(perkType.getRarity());
            meta.setDisplayName(rarityColor + "§l" + perkType.getDisplayName());
            meta.setCustomModelData(perkType.getCustomModelData());

            List<String> lore = new ArrayList<>();
            lore.add("§cYou will obtain this perk when you extract!");
            meta.setLore(lore);

            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(namespaceService.PERK_ID_KEY, PersistentDataType.STRING, perkType.getId());

            perkCard.setItemMeta(meta);
        }
        return perkCard;
    }

    public List<PerkType> itemsToPerks(PlayerInventory inventory) {
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

    // Then in your getPerkID method:
    public String getPerkID(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(namespaceService.PERK_ID_KEY, PersistentDataType.STRING);
    }

    // Function to check if two ItemStacks are equal based on their PersistentDataContainer
    public boolean areItemsEqual(ItemStack item1, ItemStack item2) {
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

        // Get the PersistentDataContainers from both items
        PersistentDataContainer data1 = meta1.getPersistentDataContainer();
        PersistentDataContainer data2 = meta2.getPersistentDataContainer();

        // Check if both items have the custom key in their PersistentDataContainer
        if (data1.has(namespaceService.PERK_ID_KEY, PersistentDataType.STRING) && data2.has(namespaceService.PERK_ID_KEY, PersistentDataType.STRING)) {
            String id1 = data1.get(namespaceService.PERK_ID_KEY, PersistentDataType.STRING);
            String id2 = data2.get(namespaceService.PERK_ID_KEY, PersistentDataType.STRING);

            // Compare the unique IDs
            return id1 != null && id1.equals(id2);
        }

        return false; // The custom data isn't present or doesn't match
    }

    // Function to check if ItemStack contains hidden rarity key
    public boolean hiddenItem(ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String id = container.get(namespaceService.RARITY_KEY, PersistentDataType.STRING);
        if (id == null)
            return false; // No custom data is present or the custom data isn't a hidden rarity key
        return id.equals("HIDDEN");
    }

    // ==================== UPDATED METHODS FOR NEW ARCHITECTURE ====================

    /**
     * Update the item's count based on owned perks.
     * NEW: Takes List<PerkType> and the specific type to count.
     */
    public void updateCount(ItemStack item, List<PerkType> ownedPerks, PerkType type) {
        // Count how many of this specific type the player owns
        int count = (int) ownedPerks.stream().filter(t -> t == type).count();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // If the count is 0, make the item a bedrock block (unavailable)
        if (count == 0) {
            item.setType(Material.BEDROCK);
            // Update meta data identifier
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.remove(namespaceService.PERK_ID_KEY);
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
     * Update equip status display on item.
     * NEW: Takes Map<PerkType, Integer> for equipped perks.
     */
    public void updateEquipStatus(ItemStack item, Map<PerkType, Integer> equippedPerks) {
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
}