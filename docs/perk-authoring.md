# Perk authoring guide

Use this guide when creating a **new** perk after the migration is complete.

## Perk template
A new perk should follow this flow:
1. Add the metadata to `PerkType`.
2. Register the singleton in `PerkRegistry.init()`.
3. Implement the perk as a subclass of `Perk` under `perk.impl.*`.
4. Use `PerkProfile` for player ownership/equipment state.
5. Use `ItemService` to build perk cards/items.

## Choose the right BGSCore service
- `BGSApi.events()` for event subscriptions.
- `BGSApi.tasks()` for delayed or repeating logic.
- `BGSApi.ability()` for cooldowns, charges, and ultimates.
- `BGSApi.namespaces()` for item metadata keys via `NamespaceService`.
- `BGSApi.commands()` only when the perk needs command integration; most perks do not.

## Implementation rules
- Perks are shared singletons; keep the class logic stateless unless you need short-lived per-player runtime data.
- If you do keep per-player runtime state, key it by `UUID` and clean it up in `onDisable(Player)`.
- Subscribe with `.owner(uuid).namespace(perkId)` so cleanup is scoped to the perk.
- Always reverse everything in `onDisable(Player)`: listeners, tasks, ability state, potion effects, and any other player-side changes.

## Item and UI rules
- Build perk items through `ItemService.createPerkItem(...)`.
- The item ID must match `PerkType.id` and stay upper-case in code.
- Use `NamespaceService.getPerkIdKey()` and `getRarityKey()` for metadata.
- Do not rename these inventory titles:
  - `Choose Your Perk`
  - `Roll for Perks`
  - `Confirm Scrap`

## Current implementation examples
- `Bloodied` shows how to maintain per-player state and task/listener cleanup.
- `Flash` shows a repeating task plus potion effect cleanup.
- `Kangaroo` shows how to use `AbilityService` for an XP-bar ultimate.

## Perk design checklist
Before writing code, define:
- perk name
- rarity
- star perk or not
- trigger type
- costs or charges
- dependencies on other perks
- whether it needs world gating
- whether it needs a UI button, perk card, or both

## Prompt template for new perks
> Create a new perk named `NAME`.
> - Rarity: `common|uncommon|rare|legendary`
> - Type: `passive|active|movement|combat|utility`
> - Stars: `yes|no`
> - Trigger: `...`
> - Dependencies: `...`
> - Cleanup needs: `...`
> Keep it aligned with `Perk`, `PerkProfile`, `PerkRegistry`, `ItemService`, and the BGSCore API.

