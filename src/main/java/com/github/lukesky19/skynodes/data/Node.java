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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public record Node(String nodeId, World nodeWorld, int nodeX, int nodeY, int nodeZ, List<File> nodeSchems, ProtectedRegion region, Location safeLocation, List<Material> materials) {
    @NotNull
    public static Node createNode(String nodeId, World nodeWorld, int nodeX, int nodeY, int nodeZ, List<File> nodeSchems, ProtectedRegion region, Location safeLocation, List<Material> materials) {
        return new Node(nodeId, nodeWorld, nodeX, nodeY, nodeZ, nodeSchems, region, safeLocation, materials);
    }
}
