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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.data.config.Locale;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to create the help command argument.
 */
public class HelpCommand {
    private final @NotNull LocaleManager localeManager;

    /**
     * Default Constructor.
     * You should use {@link #HelpCommand(LocaleManager)} instead.
     * @deprecated You should use {@link #HelpCommand(LocaleManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public HelpCommand() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     */
    public HelpCommand(@NotNull LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("help")
                .requires(ctx -> ctx.getSender().hasPermission("skymines.commands.skymines.help"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Locale locale = localeManager.getLocale();

                    for (String msg : locale.help()) {
                        sender.sendMessage(AdventureUtil.serialize(msg));
                    }

                    return 1;
                });

        return builder.build();
    }
}
