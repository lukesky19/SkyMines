# SkyNodes
## Description
* This plugin is a mines plugin that tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.

## Features
* Restricts block breaking within a node to a configured list of blocks.
* When a block is mined, it is replaced with what was broken and the player is sent a client-side block update to show a configurable replacement Block.
  * A loot table can be configured for suspicious sand and suspicious gravel. (Optional)
  * It is required since the plugin cannot detect what the loot table was.
* Cooldowns are configurable per block.
* An infinite number of players can use the mine without worrying about the mine being empty since tracking is per-player and block updates are client-side.
* In the event of a config error, all block breaking, block drops, and block clicks will be cancelled to protect any mines from unintentional destruction.

## Dependencies
* WorldGuard

## FAQ
Q: What Minecraft versions does this plugin support?

A: 1.21-1.21.1

Q: What plugin versions are supported?

A: I will only support the latest plugin version(s), which are currently is 2.0.2 and 3.0.0-Pre-1.

Q: Are there any plans to support any other versions?

A: No.

Q: Does this work on Spigot and Paper?

A: This will work on Paper.

Q: Is Folia supported?

A: There is no Folia support at this time. I may look into it in the future though.

## Issues or Bugs
* Create a new GitHub Issue describing your issue.
* Please post any relevant logs containing errors related to SkyNodes and your configuration files.
* I will attempt to solve any issues to the best of my ability.

## Example Setup Instructions
* Create a new region (parent region) in a world that encompasses the entire playable area.
* Create a new region (child region) inside that region where you want players to be able to break blocks.
* Create a new group (/lp creategroup \<name>).
* Add that group to the 2nd region above (/rg addmember \<region name> g:\<group name>).
* Open nodes.yml and configure a node based on what you have done.
* Reload the plugin (/skynodes reload)
* Give a player the group you created to be able to mine the node. (/lp user \<username> parent add \<group name>)
  * Also works with temporary ranks as well. (/lp user \<username> parent addtemp \<group name> 1h accumulate)

## Building
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
