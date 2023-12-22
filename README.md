# SkyNodes
## Description
This plugin takes configured schematics from WorldEdit or FastAsyncWorldEdit and pastes them in configured locations after a set period of time.

## Features
* Supports WorldEdit and FastAsyncWorldEdit
* Restricts block breaking within a skyNode to a configured list of blocks.
* Teleports player to safety before a skyNode is pasted. (Doesn't apply when pasting using the /skynodes paste command)
* Multiple schematics can be configured for one location. The plugin will pick one at random.

## Dependencies
* WorldEdit or FastAsyncWorldEdit
* Multiverse-Core
* WorldGuard

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
