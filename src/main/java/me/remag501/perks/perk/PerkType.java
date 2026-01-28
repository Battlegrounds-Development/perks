package me.remag501.perks.perk;

import me.remag501.perks.perk.impl.*;
import me.remag501.perks.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

//new Perk("Sword Perk", Items.createItem(Material.DIAMOND_SWORD, "Sword Perk", false, "stuff")
public enum PerkType {
    SWORD_PERK(new LongSwordPerk(ItemUtil.createPerkItem(Material.DIAMOND_SWORD, "Sword Perk", "SWORD_PERK", 4, "stuff"))),
    CREEPER_BRAWLER(new CreeperBrawler(ItemUtil.createPerkItem(Material.CREEPER_HEAD, "Creeper Brawler Perk", "CREEPER_BRAWLER", 4, "Creeper Brawler Perk"))),
    HOT_HANDS(new HotHandsPerk(ItemUtil.createPerkItem(4011, "Hot Hands", "HOT_HANDS", 1, "Hot potato got a little too hot"))),
    GOLDEN_EXPERIENCE(new GoldenExperiencePerk(ItemUtil.createPerkSkull("http://textures.minecraft.net/texture/a5e048e5e94b945d161f0f3df83cc6f61985d5974c7ad9595197a50dc00edc0b",
            "Golden Experience", "GOLDEN_EXPERIENCE", 4, "Muda Muda Muda"))),
    DAMAGE_MULT(new DamageMultiplierPerk(ItemUtil.createPerkItem(Material.WOODEN_SWORD, "Damage Multiplier 1.5x", "DAMAGE_MULT", 4, "Deals an extra 1.5x damage"))),
    DAMAGE_TWO_MULT(new DamageMultiplierTwoPerk(ItemUtil.createPerkItem(Material.STONE_SWORD, "Damage Multiplier 2x", "DAMAGE_TWO_MULT", 4, "Deals an extra 2x damage"))),
    FLAT_DAMAGE(new FlatDamagePerk(ItemUtil.createPerkItem(Material.GLASS_PANE, "Flat Damage", "FLAT_DAMAGE", 4, "Deals an extra 2 flat damage"))),
    BLOODIED(new Bloodied(ItemUtil.createPerkItem(4017, "Bloodied", "BLOODIED", 2, "When hp drops below 20/30/40% gain strength 1"), true)),
    FLASH(new Flash(ItemUtil.createPerkItem(4012, "Flash", "FLASH", 1, "Speed 1 but gain weakness every 3 minutes"))),
    JUMPER(new Jumper(ItemUtil.createPerkItem(4041, "Pogo", "JUMPER", 1, "Jump Boost 1 but gain slowness every 1.5 minutes"))),
    UNYIELDING(new Resistant(ItemUtil.createPerkItem(4025, "Unyielding", "UNYIELDING", 2, "Resistance 1 when under 20/25/30% HP"), true)),
    LOW_MAINTENANCE(new LowMaintenance(ItemUtil.createPerkItem(0, "Low Maintenance", "LOW_MAINTENANCE", 1, "Saturation 1 for 15 seconds every 2 minutes"))),
    KANGAROO(new Kangaroo(ItemUtil.createPerkItem(4014, "Kangaroo", "KANGAROO", 3, "Double jump once every thirty seconds"),
            List.of(List.of(PerkType.FLASH, PerkType.JUMPER)))),
    THE_WORLD(new TheWorldPerk(ItemUtil.createPerkSkull("http://textures.minecraft.net/texture/ff1fc6ebc549c6da4807bd30fc6e47bf4bdb516f256864891a31e6f6aa2527b0",
            "The World", "THE_WORLD", 4, "The ultimate stando."))),
    SERENDIPITY(new Serendipity(ItemUtil.createPerkItem(4023, "Serendipity", "SERENDIPITY", 2, "20% chance to take no damage from mobs."))),
    OVERDRIVE(new Overdrive(ItemUtil.createPerkItem(4021, "Overdrive", "OVERDRIVE", 1, "Hit mobs with instant healing 1."))),
    BERSERKER(new Berserker(ItemUtil.createPerkItem(4016, "Berserker", "BERSERKER", 3, "Axe hits are multiplied by fist damage over last 3 seconds."))),
    COOKIE_CLICKER(new CookieClicker(ItemUtil.createPerkItem(4015, "Cookie Clicker", "COOKIE_CLICKER", 0, "Everytime you kill a player two cookies are dropped."))),
    BOUNTY_HUNTER(new BountyHunter(ItemUtil.createPerkItem(4018, "Bounty Hunter", "BOUNTY_HUNTER", 0, "Everytime you kill a player you gain money."))),
    XP_FARM(new XPFarm(ItemUtil.createPerkItem(4026, "XP Farm", "XP_FARM", 0, "Everytime you kill a player you gain xp."))),
    TAI_CHI(new TaiChi(ItemUtil.createPerkItem(4024, "Tai Chi", "TAI_CHI", 2, "Holding out your fist for three seconds and hitting an enemy inflicts wither with blindness."))),
    CONCUSSION(new Concussion(ItemUtil.createPerkItem(4019, "Concussion", "CONCUSSION", 1, "Hitting a player with your fist gives them nausea."))),
    GHOST_FIST(new GhostFist(ItemUtil.createPerkItem(4022, "Kumite", "GHOST_FIST", 3, "Hitting a player with your fist creates a delayed second hit."),
            List.of(List.of(PerkType.CONCUSSION), List.of(PerkType.TAI_CHI, PerkType.HOT_HANDS)))),
    UNDEAD(new Undead(ItemUtil.createPerkItem(16, "Undead", "UNDEAD", 0, "Gain four absorption hearts if you kill a player with a zombie"))),
    FLOWER_POWER(new FlowerPower(ItemUtil.createPerkItem(0, "Flower Power", "FLOWER_POWER", 0, "If you are near trees or flowers, you deal more damage"))),
    GUERILLA_TACTICS(new GuerrillaTactics(ItemUtil.createPerkItem(0, "Guerilla Tactics", "GUERILLA_TACTICS", 0, "If you sneak in flowers for three seconds, you turn invisible"))),
    PACK_MASTER(new PackMaster(ItemUtil.createPerkItem(0, "Pack Master", "PACK_MASTER", 0, "A wolf is summoned upon killing someone. (uncommon)"))),
    SNEAK_ATTACK(new SneakAttack(ItemUtil.createPerkItem(0, "Sneak Attack", "SNEAK_ATTACK", 0, "Hitting a player behind them on the first hit deals 150% damage"))),
    WOLF_BOUNDED(new WolfBounded(ItemUtil.createPerkItem(0, "Wolf Bounded", "WOLF_BOUNDED", 0, "All wolves have a shared health pool"))),
    FERAL(new Feral(ItemUtil.createPerkItem(0, "Feral", "FERAL", 0, "Deals 5% extra damage per wolf owned"))),
    JUMPED(new Jumped(ItemUtil.createPerkItem(0, "Jumped", "JUMPED", 0, "Wolves teleport to enemy when hitting them on first hit")));

    private final Perk perk;

    PerkType(Perk perk) {
        this.perk = perk;
    }

    public Perk getPerk() {
        return perk;
    }

    public ItemStack getItem() {
        return perk.getItem();
    }

    public static PerkType getPerkType(Perk perk) {
        for (PerkType type : PerkType.values()) {
            if (type.getPerk().getItem().equals(perk.getItem())) {
                return type;
            }
        }
        return null; // Return null if no match is found
    }

    public static List<PerkType> getPerksByRarity(int rarity) {
        List<PerkType> perks = new ArrayList<>();
        for (PerkType type: PerkType.values()) {
            if (ItemUtil.getRarity(type) == rarity)
                perks.add(type);
        }
        return perks;
    }

    public static List<PerkType> perkTypesByPage(int page) {
        List<PerkType> perks = new ArrayList<>();
        int count = 0, passed = 0;
        for (PerkType type: PerkType.values()) {
            if (ItemUtil.getRarity(type) != -1) { // Item is not hidden

                if (passed / 14 == page) {
                    perks.add(type);
                    count++;
                } else passed++;

                if (count == 14)
                    break;
            }
        }
        return perks;
    }

}
