# Perk migration guide

Use this guide when converting a legacy perk in `src/main/java/me/remag501/perk/perk/impl/` into the refactored model.

## Core rule
- Do **not** copy the old architecture forward.
- Old classes may be commented snapshots that used `@EventHandler`, `BukkitRunnable`, `Bukkit.getScheduler()`, or `me.remag501.perks.*`.
- The new target is a shared singleton `Perk` with player-specific state living in `PerkProfile`.

## Before migrating a perk
1. Find the legacy behavior and write down:
   - trigger type: passive, combat, movement, utility, or active ability
   - any repeating timers or cooldowns
   - any world restrictions
   - any item/menu tags it depends on
2. Decide whether the behavior belongs in:
   - `EventService` for event subscriptions
   - `TaskService` for delayed/repeating logic
   - `AbilityService` for charges, cooldowns, or ultimates
   - `PerkProfile` for owned/equipped state and scrap logic

## Legacy-to-new mapping examples
- `Berserker`, `Jumper`, `LowMaintenance`, `TheWorldPerk`: old direct event handlers and scheduler code should become BGS subscriptions/tasks.
- `Bloodied`: use per-player state only for live runtime data; cleanup must remove listeners, tasks, and effects.
- `Flash`: use a task for repeated weakness application and reset the speed effect on disable.
- `Kangaroo`: use `AbilityService` for the XP-bar charge and namespaced event subscriptions for movement/fall logic.

## Migration checklist for each perk
- Add or confirm the `PerkType` entry.
- Add the singleton implementation in `perk.impl.*`.
- Register it in `PerkRegistry.init()`.
- Wire event listeners with `.owner(uuid).namespace(perkId)`.
- Stop all tasks in `onDisable(Player)`.
- Reset any ability state in `onDisable(Player)`.
- Remove potion effects or other player-side state in `onDisable(Player)`.
- Keep messages aligned with `BGSColor.PREFIX_PERKS`, `POSITIVE`, and `NEGATIVE`.
- Verify any item or menu tags still use `NamespaceService.getPerkIdKey()` / `getRarityKey()` through `ItemService`.

## What not to migrate
- Do not reintroduce class-level `@EventHandler` methods inside perks.
- Do not use raw `Bukkit.getScheduler()` when `TaskService` fits.
- Do not store persistent player state in the perk singleton unless it is transient and cleaned up on disable.
- Do not change menu titles; the listeners rely on exact text.

## One-perk-at-a-time workflow
When you prompt for a specific perk, I will:
1. inspect the legacy implementation,
2. translate it to the refactored pattern,
3. register it in `PerkRegistry`,
4. check cleanup and world gating,
5. verify the UI text and metadata still match the listeners.

## Best prompt for migration work
> Migrate `PERK_NAME` using the new perk architecture. Keep the current gameplay behavior, register it in `PerkRegistry`, and preserve item/UI contracts.

