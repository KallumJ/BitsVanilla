package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public abstract class Command implements com.mojang.brigadier.Command<ServerCommandSource> {
    private final String name;
    private final String[] aliases;

    public Command(String name) {
        this.name = name;
        this.aliases = null;
    }

    public Command(String name, String[] aliases) {
        this.name = name;
        this.aliases = aliases;
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

}
