# AGENTS.md

## Project snapshot
- `perks` is a Java 21 / Maven Spigot plugin for MC Battlegrounds.
- The plugin depends on `BGSCore` (`me.remag501.core:*`) and the Spigot 1.21.8 API.
- `PerksPlugin` is the bootstrap: it wires BGS services, config, registries, menus, listeners, and commands.
- Core integration comes through `BGSApi.commands()`, `events()`, `tasks()`, `namespaces()`, and `ability()`; message prefixes should use `BGSColor.PREFIX_PERKS`.

## Read first
- `pom.xml` for build output and dependency scope.
- `src/main/resources/plugin.yml`, `config.yml`, `perks.yml` for runtime contract and persisted data.
- `src/main/java/me/remag501/perk/PerksPlugin.java` for startup/shutdown order.
- `manager/PerkManager.java`, `model/PerkProfile.java`, `perk/PerkType.java`, `registry/PerkRegistry.java` for the state model.
- `service/ItemService.java` and `ui/*.java` for item tags and menu layout.

## Architecture to preserve
- Player state lives in `PerkProfile`; perk logic lives in shared singleton `Perk` implementations.
- `PerkType` is data-only (id, display name, rarity, model data, requirements); load/save uses those ids directly.
- New perks are registered explicitly in `PerkRegistry.init()` and should be created via `ItemService.createPerkItem(...)`.
- Perk listeners/tasks are namespaced by perk id; `onEnable()` should subscribe with `.owner(uuid).namespace(perkId)` and `onDisable()` must clean up with `unregisterListener(uuid, perkId)` / `stopTask(uuid, perkId)` plus any potion effects or ability state.

## Important conventions
- Inventory titles are contractually significant: `Choose Your Perk`, `Roll for Perks`, and `Confirm Scrap` are hard-coded in listeners.
- `PerkMenuListener`, `GambleListener`, and `ScrapListener` filter by those exact titles, so keep UI text and listener filters in sync.
- `GlobalPerkListener` enforces world gating from `config.yml`; `WorldRegistry.BUNKER_PREFIX` blocks any world starting with `bunker`.
- `perks.yml` stores each player as `<uuid>_owned` and `<uuid>_equipped`; the final owned entry is perk points.
- The local `src/main/java/me/remag501/perk/service/NamespaceService.java` is commented out; the real namespace API comes from BGSCore and backs `ItemService` PDC tags for perk IDs and hidden rarity.
- `PerkType.id` values are the serialized keys and stay upper-case in code; display names are user-facing and may be colorized in menus/cards.

## Command and permission notes
- `/perks` opens the UI for `perks.user` players.
- `/perks` subcommands (`add`, `remove`, `addpoints`, `addcard`, `reload`, `save`, `hiddenui`) are guarded by `perks.admin` / child permissions in `plugin.yml`.
- Tab completion lives in `PerksCompleter` and mirrors those subcommands.
- `CommandService.registerSubcommand("perk", ...)` is used for the BGS command bridge; keep that label aligned with the plugin command wiring.

## Core API reminders
- Use `NamespaceService.getPerkIdKey()` and `getRarityKey()` for item metadata; extraction, menu filtering, and item equality all depend on those tags.
- `TaskService` is used for delayed work and repeating perk logic; `AbilityService` is used for cooldowns, charges, and ultimates such as `Kangaroo`'s XP-bar charge.
- Keep perk messages consistent with `BGSColor.PREFIX_PERKS`, `POSITIVE`, and `NEGATIVE` so UI text matches the rest of the BGS suite.

## Verification workflow
- Build from the repo root with `mvn clean package`.
- The shaded artifact is produced under `target/` (see the `BGSPerks-<version>.jar` output from the shade plugin).
- There are no committed `src/test` tests, so the practical smoke check is loading the jar on a server with BGSCore and confirming `/perks`, menu clicks, and world gating still work.

## Perk docs
- `docs/perk-migration.md` explains how to translate legacy perks into the refactored model, one perk at a time.
- `docs/perk-authoring.md` explains how to craft new perks with the current API, registry, item, and UI conventions.

