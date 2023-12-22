# SkyNodes
## Description
* This plugin takes a configured task with a delay and a list of nodes, picks a random node, and pastes it in a set location for that node.

## Features
* Supports WorldEdit and FastAsyncWorldEdit
* Restricts block breaking within a SkyNode to a configured list of blocks.
* Teleports player to safety before a SkyNode is pasted. (Doesn't apply when pasting using the /skynodes paste command)
* Multiple schematics can be configured for one location. The plugin will pick one at random.
* Multiple tasks are supported to paste multiple SkyNodes at once.

## Dependencies
* WorldEdit or FastAsyncWorldEdit
* Multiverse-Core
* WorldGuard

## Example Setup Instructions
* Create a new region in a world that encompases the entire playable area.
* Create a new region inside that region where you want players to be able to break blocks.
* Create a new group (/lp creategroup \<name>).
* Add that group to the 2nd region above (/rg addmember \<region name> g:\<group name>).
* Create a schematic to replace the blocks players will break. Make sure to remember the coordinates you ran //copy at.
* Open nodes.yml and configure a node based on what you have done.
* Reload the plugin (/skynodes reload)
* Give a player the group you created to be able to mine the node. (/lp user \<username> parent add \<group name>)
* Also works with temporary ranks as well. (/lp user \<username> parent add \<group name> 1h accumulate)

## Disclaimers
* Paper and forks of Paper are only supported at this time.
* I only use Paper or forks of Paper for servers I work on, but I may add Spigot support in the future.

## TO-DO
### v0.5.0
* Change /skynodes paste to /skynodes paste <taskid> <nodeid>
* Add /skynodes undo (for last 10 pastes)
* Add /skynodes redo (for last 10 undos)

## Building
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
