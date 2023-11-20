# SkyNodes
## Description
This plugin takes configured schematics from WorldEdit or FastAsyncWorldEdit and pastes them in configured locations after a set period of time.

## Features
* Supports WorldEdit and FastAsyncWorldEdit
* Multiple schematics per node (chosen randomly).
* Multiple locations per node (chosen randomly).

## Disclaimer
* Paper and forks of Paper are only supported at this time.
* I only use Paper or forks of Paper for servers I work on, but I may add Spigot support in the future.

## TO-DO
### V0.3.0
* Add a check for the player within the region a schematic is placed and teleport them to safety.
* Make all messages sent to console or the player configurable.

## Building
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time where a fork can never be continued (i.e., closed-sourced and abandoned).

I normally license such small projects under the MIT License, but I decided not to due to the reason above.
