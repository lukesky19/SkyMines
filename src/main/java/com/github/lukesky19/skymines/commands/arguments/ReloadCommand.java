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
package com.github.lukesky19.skymines.commands.arguments;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.locale.Locale;
import com.github.lukesky19.skymines.locale.LocaleManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

/**
 * This class is used to create the reload command argument.
 */
public class ReloadCommand {
    private final @NonNull SkyMines skyMines;
    private final @NonNull LocaleManager localeManager;

    /**
     * Default Constructor.
     * You should use {@link #ReloadCommand(SkyMines, LocaleManager)} instead.
     * @deprecated You should use {@link #ReloadCommand(SkyMines, LocaleManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public ReloadCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public ReloadCommand(@NonNull SkyMines skyMines, @NonNull LocaleManager localeManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the reload command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the reload command argument.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("reload")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.reload"))
                .executes(ctx -> {
                    Locale locale = localeManager.getConfiguration();

                    skyMines.reload(false);

                    ctx.getSource().getSender().sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.reload()));

                    return 1;
                });

        return builder.build();
    }
}