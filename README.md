# SkyNodes
## Description
This plugin takes configured schematics from WorldEdit or FastAsyncWorldEdit and pastes them in configured locations after a set period of time.

## Features
* Supports WorldEdit and FastAsyncWorldEdit
* Restricts block breaking within a node to a configured list of blocks.
* Teleports player to safety before a node is pasted. (Doesn't apply when pasting using the /skynodes paste command)
* Multiple schematics can be configured for one location. The plugin will pick one at random.

## Disclaimers
* Paper and forks of Paper are only supported at this time.
* I only use Paper or forks of Paper for servers I work on, but I may add Spigot support in the future.

## TO-DO
### v0.4.0
* Add the ability to undo pastes with the /skynodes paste command (/skynodes undo).
* ~~Add the option for multiple simultaneous tasks where nodes are pasted for different locations.~~
* Clean up and optimize code.
* Add separate time delays per task.
* Add skynodes.debug permission.
* Add more debug messages.
* Add more robust error checking.
* Add more robust error messages.

## Building
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time where a fork can never be continued (i.e., closed-sourced and abandoned).

I normally license such small projects under the MIT License, but I decided not to due to the reason above.
