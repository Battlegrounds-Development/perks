package me.remag501.perks.util;

import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;

public class WorldUtil {

    public static final List<String> DROP_WORLDS = new ArrayList<>();
    public static final List<String> DISABLED_WORLDS = new ArrayList<>();
    public static final String BUNKER_PREFIX = "bunker";

    /**
     * Centralized check used by PerkProfile and Managers
     */
    public static boolean isPerkEnabled(World world) {
        if (world == null) return false;
        String name = world.getName();
        return !DISABLED_WORLDS.contains(name) && !name.startsWith(BUNKER_PREFIX);
    }

    public static boolean isDropEnabled(World world) {
        return world != null && DROP_WORLDS.contains(world.getName());
    }
}