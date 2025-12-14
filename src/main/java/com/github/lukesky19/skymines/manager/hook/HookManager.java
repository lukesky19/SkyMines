/*
    SkyMines offers different types mines to get resources from.
    Copyright (C) 2023 lukeskywlker19

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
package com.github.lukesky19.skymines.manager.hook;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.integration.Hook;
import com.github.lukesky19.skymines.integration.hooks.EconomyHook;
import com.github.lukesky19.skymines.integration.hooks.PlayerPointsHook;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages hooks into different plugins.
 */
public class HookManager {
    private final @NotNull Map<Class<?>, Hook> hooks = new HashMap<>();

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     */
    public HookManager(@NotNull SkyMines skyMines) {
        EconomyHook economyHook = new EconomyHook(skyMines);
        registerHook(EconomyHook.class, economyHook);

        PlayerPointsHook playerPointsHook = new PlayerPointsHook(skyMines);
        registerHook(PlayerPointsHook.class, playerPointsHook);
    }

    /**
     * Register a hook.
     * @param hookClass The class.
     * @param hook The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public <T extends Hook> void registerHook(@NotNull Class<T> hookClass, @NotNull Hook hook) {
        hooks.put(hookClass, hook);
        hook.initialize();
    }

    /**
     * Get a hook.
     * @param hookClass The class.
     * @param <T> Parameter for any class that extends {@link Hook}.
     * @return The class instance.
     */
    public @NotNull <T extends Hook> T getHook(@NotNull Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }
}
