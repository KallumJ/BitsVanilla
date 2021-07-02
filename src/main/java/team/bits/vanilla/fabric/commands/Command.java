package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public abstract class Command implements com.mojang.brigadier.Command<ServerCommandSource> {
    private final String name;
    private final String[] aliases;
    private final CommandInformation commandInformation;
    private int permissionLevel;

    public Command(String name, CommandInformation helpInfo) {
        this.name = name;
        this.aliases = null;
        this.commandInformation = helpInfo;
    }

    public Command(String name, String[] aliases, CommandInformation cmdInfo) {
        this.name = name;
        this.aliases = aliases;
        this.commandInformation = cmdInfo;
    }

    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        permissionLevel = getCommandInformation().isPublic() ? 0 : 4;

        CommandNode<ServerCommandSource> commandNode= dispatcher.register(literal(name).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(permissionLevel)).executes(this));

        registerAliases(dispatcher, commandNode);
    }

    public CommandInformation getCommandInformation() {
        return commandInformation;
    }

    public String getName() {
        return name;
    }

    protected void registerAliases(CommandDispatcher<ServerCommandSource> dispatcher, CommandNode<ServerCommandSource> commandNode) {
        // If aliases are provided, then use them
        if (aliases != null) {
            permissionLevel = getCommandInformation().isPublic() ? 0 : 4;
            for (String alias : aliases) {
                // if the node has no children (arguments) we can just execute the command
                // otherwise we have to redirect the children
                if (commandNode.getChildren().isEmpty()) {
                    dispatcher.register(literal(alias).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(permissionLevel)).executes(commandNode.getCommand()));
                } else {
                    dispatcher.register(literal(alias).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(permissionLevel)).redirect(commandNode));
                }
            }
        }
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }
}
