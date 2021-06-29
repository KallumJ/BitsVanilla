package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
        LiteralCommandNode<ServerCommandSource> commandNode = dispatcher.register(literal(name).executes(this));

        // If aliases are provided, then use them
        if (aliases != null) {
            for (String alias : aliases) {
                dispatcher.register(literal(alias).redirect(commandNode));
            }
        }
    }

    public CommandHelpInformation getHelpInformation() {
        return helpInformation;
    }

    public String getName() {
        return name;
    }
}
