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
package com.github.lukesky19.skynodes.managers;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.records.Messages;
import com.github.lukesky19.skynodes.utils.ConfigLoaderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public final class MessagesManager {
    final SkyNodes plugin;
    final ConfigLoaderUtil configLoaderUtil;
    final MiniMessage mm = MiniMessage.miniMessage();
    Messages messages;
    public MessagesManager(SkyNodes plugin, ConfigLoaderUtil configLoaderUtil)  {
        this.plugin = plugin;
        this.configLoaderUtil = configLoaderUtil;
    }
    public Messages getMessages() {
        return messages;
    }

    @SuppressWarnings("DataFlowIssue")
    private void loadPluginMessages(CommentedConfigurationNode configurationNode) {
        Component prefix;
        Component reload;
        Component startTasksSuccess;
        List<Component> help = new ArrayList<>();
        String noNodesFound;
        Component operationFailure;
        String clipBoardLoadFailure;
        Component noPermission;
        Component unknownArgument;
        Component missingArgumentTaskId;
        Component missingArgumentNodeId;
        String nodePasteSuccess;
        String nodePasteFailure;
        String worldNotFound;
        String schematicsListError;
        String schematicNotFound;
        String invalidLocation;
        String invalidSafeLocation;
        String invalidRegion;
        String blocksAllowedListError;
        String invalidBlockMaterial;
        Component undo;
        Component redo;
        Component noUndo;
        Component noRedo;
        String invalidTaskId;
        String invalidNodeId;
        Component inGameOnly;
        Component bypassedSafeTeleport;
        Component bypassedBlockBreakCheck;
        Component canMine;
        Component canNotMine;

        if(!configurationNode.node("Prefix").virtual()) {
            prefix = mm.deserialize(configurationNode.node("Prefix").getString());
        } else {
            prefix = mm.deserialize("<gray>[<yellow><bold>SkyNodes<reset><gray>]");
        }

        if(!configurationNode.node("Reload").virtual()) {
            reload = mm.deserialize(configurationNode.node("Reload").getString());
        } else {
            reload = mm.deserialize("<aqua>The plugin has reloaded successfully.");
        }

        if(!configurationNode.node("StartTasksSuccess").virtual()) {
            startTasksSuccess = mm.deserialize(configurationNode.node("StartTasksSuccess").getString());
        } else {
            startTasksSuccess = mm.deserialize("<aqua>The plugin has started all tasks successfully.");
        }

        if(!configurationNode.node("Help").virtual()) {
            try {
                for(String message : configurationNode.node("Help").getList(String.class)) {
                    help.add(mm.deserialize(message));
                }
            } catch (SerializationException ignored) {}
        } else {
            help.add(mm.deserialize("<aqua>Skynodes is developed by <white><bold>lukeskywlker19<reset><aqua>."));
            help.add(mm.deserialize("<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</click><reset><aqua>."));
            help.add(mm.deserialize(" "));
            help.add(mm.deserialize("<aqua><bold>List of Commands:"));
            help.add(mm.deserialize("<white>/<aqua>skynodes <yellow>help"));
            help.add(mm.deserialize("<white>/<aqua>skynodes <yellow>reload"));
            help.add(mm.deserialize("<white>/<aqua>skynodes <yellow>paste <white><<red>schematic name<white>> <white><<red>world<white>> <white><<red>X<white>> <white><<red>Y<white>> <white><<red>Z<white>>"));
        }

        if(!configurationNode.node("NoNodesFound").virtual()) {
            noNodesFound = configurationNode.node("NoNodesFound").getString();
        } else {
            noNodesFound = "<red>No nodes for task <white><taskid> <red>are configured. Did you setup any nodes in nodes.yml?";
        }

        if(!configurationNode.node("OperationFailure").virtual()) {
            operationFailure = mm.deserialize(configurationNode.node("OperationFailure").getString());
        } else {
            operationFailure = mm.deserialize("<red>The operation failed to complete. See console for more information.");
        }

        if(!configurationNode.node("ClipboardLoadFailure").virtual()) {
            clipBoardLoadFailure = configurationNode.node("ClipboardLoadFailure").getString();
        } else {
            clipBoardLoadFailure = "<red>Unable to load to the clipboard for <white><taskid> <red>and node <white><nodeid>. See console for more information.";
        }

        if(!configurationNode.node("NoPermission").virtual()) {
            noPermission = mm.deserialize(configurationNode.node("NoPermission").getString());
        } else {
            noPermission = mm.deserialize("<red>You do not have permission for this command or sub-command.");
        }

        if(!configurationNode.node("UnknownArgument").virtual()) {
            unknownArgument = mm.deserialize(configurationNode.node("UnknownArgument").getString());
        } else {
            unknownArgument = mm.deserialize("<red>Unknown argument. Double-check your command.");
        }

        if(!configurationNode.node("MissingArgumentTaskId").virtual()) {
            missingArgumentTaskId = mm.deserialize(configurationNode.node("MissingArgumentTaskId").getString());
        } else {
            missingArgumentTaskId = mm.deserialize("<red>Missing argument for the task id. Double-check your command.");
        }

        if(!configurationNode.node("MissingArgumentNodeId").virtual()) {
            missingArgumentNodeId = mm.deserialize(configurationNode.node("MissingArgumentNodeId").getString());
        } else {
            missingArgumentNodeId = mm.deserialize("<red>Missing argument for the node id. Double-check your command.");
        }

        if(!configurationNode.node("NodePasteSuccess").virtual()) {
            nodePasteSuccess = configurationNode.node("NodePasteSuccess").getString();
        } else {
            nodePasteSuccess = "<aqua>Node <white><nodeid> <aqua>for task <white><taskid> <aqua>has pasted successfully.";
        }

        if(!configurationNode.node("NodePasteFailure").virtual()) {
            nodePasteFailure = configurationNode.node("NodePasteFailure").getString();
        } else {
            nodePasteFailure = "<red>Node <white><nodeid> <red>for task <white><taskid> <red>has failed to paste. See console for more information.";
        }

        if(!configurationNode.node("WorldNotFound").virtual()) {
            worldNotFound = configurationNode.node("WorldNotFound").getString();
        } else {
            worldNotFound = "<red>The world for task <white><taskid> <red>and node <white><nodeid> <red>is invalid.";
        }

        if(!configurationNode.node("SchematicsListError").virtual()) {
            schematicsListError = configurationNode.node("SchematicsListError").getString();
        } else {
            schematicsListError = "<red>The plugin was unable to obtain the schematic names for task <white><taskId> <red>and node <white><nodeid><red>. Is the node formatted correctly?";
        }

        if(!configurationNode.node("SchematicNotFound").virtual()) {
            schematicNotFound = configurationNode.node("SchematicNotFound").getString();
        } else {
            schematicNotFound = "<red>A schematic configured for task <white><taskid> <red>and node <white><nodeid> <red>is invalid or does not exist.";
        }

        if(!configurationNode.node("InvalidLocation").virtual()) {
            invalidLocation = configurationNode.node("InvalidLocation").getString();
        } else {
            invalidLocation = "<red>The X Y Z coordinates of <white>location <red>for task <white><taskid> <red>and node <white><nodeid> <red>are invalid.";
        }

        if(!configurationNode.node("InvalidSafeLocation").virtual()) {
            invalidSafeLocation = configurationNode.node("InvalidSafeLocation").getString();
        } else {
            invalidSafeLocation = "<red>The X Y Z coordinates of <white>safe-location <red>for task <white><taskid> <red>and node <white><nodeid> <red>are invalid.";
        }

        if(!configurationNode.node("InvalidRegion").virtual()) {
            invalidRegion = configurationNode.node("InvalidRegion").getString();
        } else {
            invalidRegion = "<red>The region for task <white><taskid> <red>and node <white><nodeid> <red>does not exist.";
        }

        if(!configurationNode.node("BlocksAllowedListError").virtual()) {
            blocksAllowedListError = configurationNode.node("BlocksAllowedListError").getString();
        } else {
            blocksAllowedListError = "<red>The plugin was unable to obtain the blocks-allowed list for task <white><taskid> <red>and node <white><nodeid><red>. Is the node formatted correctly?";
        }

        if(!configurationNode.node("InvalidBlockMaterial").virtual()) {
            invalidBlockMaterial = configurationNode.node("InvalidBlockMaterial").getString();
        } else {
            invalidBlockMaterial = "<red>A block for <white><taskid> <red>and node <white><nodeid> <red>is invalid.";
        }

        if(!configurationNode.node("Undo").virtual()) {
            undo = mm.deserialize(configurationNode.node("Undo").getString());
        } else {
            undo = mm.deserialize("<aqua>Undid the last edit.");
        }

        if(!configurationNode.node("Redo").virtual()) {
            redo = mm.deserialize(configurationNode.node("Redo").getString());
        } else {
            redo = mm.deserialize("<aqua>Redid the last edit.");
        }

        if(!configurationNode.node("NoUndo").virtual()) {
            noUndo = mm.deserialize(configurationNode.node("NoUndo").getString());
        } else {
            noUndo = mm.deserialize("<aqua>Nothing left to undo.");
        }

        if(!configurationNode.node("NoRedo").virtual()) {
            noRedo = mm.deserialize(configurationNode.node("NoRedo").getString());
        } else {
            noRedo = mm.deserialize("<aqua>Nothing left to redo.");
        }

        if(!configurationNode.node("InvalidTaskId").virtual()) {
            invalidTaskId = configurationNode.node("InvalidTaskId").getString();
        } else {
            invalidTaskId = "<red>The task id <white><taskid> <red>is not a valid configured task.";
        }

        if(!configurationNode.node("InvalidNodeId").virtual()) {
            invalidNodeId = configurationNode.node("InvalidNodeId").getString();
        } else {
            invalidNodeId = "<red>The node id <white><nodeid> <red>is not a valid configured node.";
        }

        if(!configurationNode.node("InGameOnly").virtual()) {
            inGameOnly = mm.deserialize(configurationNode.node("InGameOnly").getString());
        } else {
            inGameOnly = mm.deserialize("<red>This command is only available in-game.");
        }

        if(!configurationNode.node("BypassedSafeTeleport").virtual()) {
            bypassedSafeTeleport = mm.deserialize(configurationNode.node("BypassedSafeTeleport").getString());
        } else {
            bypassedSafeTeleport = mm.deserialize("<red>You bypassed the safe teleport because you have the permission <white>skynodes.bypass.safeteleport<red>.");
        }

        if(!configurationNode.node("BypassedBlockBreakCheck").virtual()) {
            bypassedBlockBreakCheck = mm.deserialize(configurationNode.node("BypassedBlockBreakCheck").getString());
        } else {
            bypassedBlockBreakCheck = mm.deserialize("<red>You bypassed the block break checks because you have the permission <white>skynodes.bypass.blockbreakcheck<red>.");
        }

        if(!configurationNode.node("CanMine").virtual()) {
            canMine = mm.deserialize(configurationNode.node("CanMine").getString());
        } else {
            canMine = mm.deserialize("You can mine this.");
        }

        if(!configurationNode.node("CanNotMine").virtual()) {
            canNotMine = mm.deserialize(configurationNode.node("CanNotMine").getString());
        } else {
            canNotMine = mm.deserialize("You can not mine this.");
        }

        messages = new Messages(
                prefix,
                reload,
                help,
                startTasksSuccess,
                noNodesFound,
                operationFailure,
                clipBoardLoadFailure,
                noPermission,
                unknownArgument,
                missingArgumentTaskId,
                missingArgumentNodeId,
                nodePasteSuccess,
                nodePasteFailure,
                worldNotFound,
                schematicsListError,
                schematicNotFound,
                invalidLocation,
                invalidSafeLocation,
                invalidRegion,
                blocksAllowedListError,
                invalidBlockMaterial,
                undo,
                redo,
                noUndo,
                noRedo,
                invalidTaskId,
                invalidNodeId,
                inGameOnly,
                bypassedSafeTeleport,
                bypassedBlockBreakCheck,
                canMine,
                canNotMine
        );
    }

    public void reloadMessages() {
        loadPluginMessages(configLoaderUtil.getMessagesConfig());
    }
}
