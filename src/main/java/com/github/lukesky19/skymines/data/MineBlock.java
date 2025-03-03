package com.github.lukesky19.skymines.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * This class stores the data of a block a player has mined.
 */
public class MineBlock {
    private final @NotNull Location location;
    private final @NotNull Material replacementMaterial;
    private int cooldownSeconds;

    /**
     * Constructor
     * @param location The location of the block inside a mine that was mined.
     * @param replacementMaterial The material that replaced it client-side.
     * @param cooldownSeconds The starting cooldown for this block.
     */
    public MineBlock(@NotNull Location location, @NotNull Material replacementMaterial, int cooldownSeconds) {
        this.location = location;
        this.replacementMaterial = replacementMaterial;
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Gets the location of the block that was mined.
     * @return A non-null Location.
     */
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Gets the Material that replaced the block client-side.
     * @return A non-null Material.
     */
    public @NotNull Material getReplacementMaterial() {
        return replacementMaterial;
    }

    /**
     * Sets the current cooldown seconds for this mined block.
     * @param cooldownSeconds The cooldown seconds to replace the current cooldown seconds with.
     */
    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Gets the current cooldown seconds for this mined block.
     * @return THe cooldown seconds of this mined block.
     */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
}