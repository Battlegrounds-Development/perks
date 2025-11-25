# SpigotPlugins_Perks Version 1.0
Spigot plugin that handles the perk features for MC Battlegrounds server.

Usage for perks plugin:
- /perks - opens UI for perks menu
- /perks add - Displays the perks that can be added
- /perks add {PERKTYPE} - Adds a perk from the player running the command
- /perks add {Player} {PERKTYPE} - Adds a perk to the specified player
- /perks remove {PERKTYPE} - Removes a perk from the player running the command
- /perks remove {Player} {PERKTYPE} - Removes a perk to the specified player
- /perks hiddenui - Allows player to the see hidden perks

Features:
- UI to interact with available perks
- Each player can hold up to three perks
- Players can equip up to five perks at once
- Perks are saved on plugin shutdown
- Perks can only work in specific worlds (Currently Sahara, Icycaverns, Kuroko, Musicland, Thundra)
- Perks have different rarities

Current Perks (Not up to date):
- Bloodied
- Flash
- Jumper
- Kangaroo
- Low Maintenance
- Resistant

## Known Issues
*   **Bug 1: Berserker**
    *   **Description:** Allows infinite damage stacking (3 second timer not working)
*   **Bug 2: Hiddenui**
    *   **Description:** Hidden ui does not work due to current display method
*   **Bug 3: onEnable double register**
    *   **Description:** Calling onEnable twice is problematic due to interference with tasks. 
    * Bug seems to exist on any perk with task (confirmed on WolfBounded, speculated on others)
    * Option 1: rewrite tasks such that there is a universal task per perk
    * Option 2: prevent onEnable from being called twice
*   **Bug 4: UI displays incorrect pre-req**
  *   **Description:** Lore no longer matches for some reason. This has been broken for a while after updating UI display logic.
  *   Pre req still work internally, UI is simply incorrect
*   **Bug 5: Perk star death drop**:
  *   **Description:** Perk cards drop twice the amount of death, when only one card is equipped.
  *   Star perks were fine-tuned in an earlier commit, I have to revisit commit history to resolve where bug occurred.
*   **Bug 6: Tai Chi (Not confirmed)**
  *   **Description:** If player with tai chi perk gets hit by someone else, they also gain effects of tai chi
  *   May be from fister set in customarmorsets
*   **Improvement needed: Active Perk List**
    *   **Description:** Every perk uses a active perk list but its not in interface

Refractor plans: Implement listener and add active perk list to perk interface.