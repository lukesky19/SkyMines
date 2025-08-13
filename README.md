# SkyMines
## Description
* SkyMines offers different types mines to get resources from.

## Features
* Packet-based mines where blocks broken are tracked server-side and clients are sent packets to show a configurable replacement Block when broken.
  * Restricts block breaking within a mine's regions to a configured list of blocks.
  * A loot table can be configured for suspicious sand and suspicious gravel when replaced.
    * This is required because the loot table is not detectable after the block as been brushed.
  * Each region has a separate list of blocks that can be broken with separate cooldowns per block.
  * Configurable boss bar timer while inside mines.
* World-style mines where blocks are unlocked by purchasing them.
  * Option to configure free blocks that do not need to be unlocked.
  * Option to allow player-placed blocks of any time.
  * Option to restrict player-placed blocks to unlocked or free blocks.
  * Option to allow all player-placed blocks to be broken regardless of unlocked blocks.
  * Option to configure restricted blocks for placement.
  * Option to allow player-triggered explosions to break blocks that are unlocked or free.
  * Configurable boss bar text.

## Dependencies
* SkyLib
* Vault
* WorldGuard

## Soft-Dependencies
* Multiverse-Core

## Commands
- /skymines help - View the plugin's help message
- /skymines reload - Command to reload the plugin
- /skymines time <mine id> - Command for a player to view how much time they have for a mine.
- /skymines time <mine id> - Command to view mine time remaining for the provided mine id.
- /skymines add <player name> <mine id> <time> - Command to add time for a player to access a specific mine.
  - The time is formatted like `1d30m`.
- /skymines remove <player name> <mine id> <time> - Command to remove time for a player to access a specific mine.
  - The time is formatted like `1d30m`.
- /skymines set <player name> <mine id> <time> - Command to set the time for a player to access a specific mine.
  - The time is formatted like `1d30m`.
- /skymines preview \[mine id] - Open the GUI to preview free blocks that can be accessed for the world-style mines.
- /skymines shop \[mine id]- Open the GUI to unlock blocks for the world-style mines.

## Permisisons
- `skymines.commands.skymines` - The permission to access the /skymines command.
- `skymines.commands.skymines.reload` - The permission to access /skymines reload.
- `skymines.commands.skymines.help` - The permission to access /skymines help.
- `skymines.commands.skymines.time` - The permission to access /skymines time.
- `skymines.commands.skymines.time.add` - The permission to access /skymines add.
- `skymines.commands.skymines.time.remove` - The permission to access /skymines remove.
- `skymines.commands.skymines.time.set` - The permission to access /skymines set.
- `skymines.commands.skymines.preview` - The permission to access /skymines preview.
- `skymines.commands.skymines.shop` - The permission to access /skymines shop.

## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/SkyMines/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyMines and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, and 1.21.8.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot? Paper? (Insert other server software here)?

A: I only support Paper, but this will likely also work on forks of Paper (untested). There are no plans to support any other server software (i.e., Spigot or Folia).

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
