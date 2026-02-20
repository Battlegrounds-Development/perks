package me.remag501.perk.registry;

import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;

public class WorldRegistry {

    public final List<String> DROP_WORLDS = new ArrayList<>();
    public final List<String> DISABLED_WORLDS = new ArrayList<>();
    public final String BUNKER_PREFIX = "bunker";

    /**
     * Centralized check used by PerkProfile and Managers
     */
    public boolean isPerkEnabled(World world) {
        if (world == null) return false;
        String name = world.getName();
        return !DISABLED_WORLDS.contains(name) && !name.startsWith(BUNKER_PREFIX);
    }

    public boolean isDropEnabled(World world) {
        return world != null && DROP_WORLDS.contains(world.getName());
    }
}