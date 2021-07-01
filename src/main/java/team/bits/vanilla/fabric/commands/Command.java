package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public abstract class Command implements com.mojang.brigadier.Command<ServerCommandSource> {
    private final String name;
    private final String[] aliases;
    private final CommandHelpInformation helpInformation;

    public Command(String name, CommandHelpInformation helpInfo) {
        this.name = name;
        this.aliases = null;
        this.helpInformation = helpInfo;
    }

    public Command(String name, String[] aliases, CommandHelpInformation helpInfo) {
        this.name = name;
        this.aliases = aliases;
        this.helpInformation = helpInfo;
    }

    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(literal(name).executes(this));

        registerAliases(dispatcher, commandNode);
    }

    public CommandHelpInformation getHelpInformation() {
        return helpInformation;
    }

    public String getName() {
        return name;
    }

    protected void registerAliases(CommandDispatcher<ServerCommandSource> dispatcher, CommandNode<ServerCommandSource> commandNode) {
        // If aliases are provided, then use them
        if (aliases != null) {
            for (String alias : aliases) {

                // if the node has no children (arguments) we can just execute the command
                // otherwise we have to redirect the children
                if (commandNode.getChildren().isEmpty()) {
                    dispatcher.register(literal(alias).executes(commandNode.getCommand()));
                } else {
                    dispatcher.register(literal(alias).redirect(commandNode));
                }
            }
        }
    }
}
