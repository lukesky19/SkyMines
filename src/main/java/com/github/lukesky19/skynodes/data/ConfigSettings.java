/*
    SkyNodes places a random configured schematic after a set period of time.
    Copyright (C) 2023  lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skynodes.data;

import org.spongepowered.configurate.CommentedConfigurationNode;

public record ConfigSettings(Boolean debug, Integer timeDelay) {
    public static ConfigSettings loadConfigSettings(CommentedConfigurationNode commentedConfigurationNode) {
        try {
            return new ConfigSettings(commentedConfigurationNode.node("debug").getBoolean(), commentedConfigurationNode.node("time-delay").getInt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
