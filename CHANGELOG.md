# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

## 1.1.0 (2026-02-20)


### Features

* added /bgs perk command ([dd1af01](https://github.com/Battlegrounds-Development/perks/commit/dd1af01537beb1f88649eafc76071198650b4866))
* Added CMD support to perk cards and totem. ([6b8ea21](https://github.com/Battlegrounds-Development/perks/commit/6b8ea216336bf4076999b4785625feb7021599d3))
* added support for perk usage in dungeons, and drop safety in safe worlds. ([b5e61f2](https://github.com/Battlegrounds-Development/perks/commit/b5e61f21a089019566e7d42f4c038add583b5ebd))
* Added way to test player kills by clicking on sign with text "fakekill <player>" ([6bb8caa](https://github.com/Battlegrounds-Development/perks/commit/6bb8caa801a56ebdd20a227add3203d7fecd8b17))
* implement naked naming and automated versioning ([13d868c](https://github.com/Battlegrounds-Development/perks/commit/13d868c9c80b94e12e72e0016ab3a44e5804e8f5))
* removed testing feature EventSimulator.java ([5639cbb](https://github.com/Battlegrounds-Development/perks/commit/5639cbb92ef065c86e9cf364ec88c22ce711c535))


### Bug Fixes

* Active and equipped perks are two separate things. PerkProfile allows classes to check if active. ([77a06bc](https://github.com/Battlegrounds-Development/perks/commit/77a06bc37add6f479819366319fc0c1bcc3b6c7f))
* added BGSCore as dependency ([eec927a](https://github.com/Battlegrounds-Development/perks/commit/eec927a2df1047a4cb64a72f13935d5e0114a733))
* corrected conditional statement to see if player is in correct world (might be working) ([c7f5ed4](https://github.com/Battlegrounds-Development/perks/commit/c7f5ed4b84665197cb0d34af7f8e91a7fd4987ea))
* gambling menu auto scraps perk when giving to you ([97fd9fc](https://github.com/Battlegrounds-Development/perks/commit/97fd9fcdb6cc3cad35b1b8bd0ac42d943ff036a6))
* removed cleanup function from abstract perk and child classes ([b2294ef](https://github.com/Battlegrounds-Development/perks/commit/b2294ef256c0fdf46013a24e2524fc0ee7932ccd))
* star perks check if max limit is hit before equipping more ([e792f09](https://github.com/Battlegrounds-Development/perks/commit/e792f0984b2dc9ca92a2e8e71a134c98fad024c2))
* when perks are removed they are dequippes and abilities are removed. This fixes star perks and dequips perks on death appropriately. ([2406171](https://github.com/Battlegrounds-Development/perks/commit/2406171dcd97710801960eaf99dc5305da27c5fe))
* wrapped every on enable and disable with world check in perk profile ([72247ca](https://github.com/Battlegrounds-Development/perks/commit/72247caa14e1cf6f0daf4aa29545a05c9d581539))
