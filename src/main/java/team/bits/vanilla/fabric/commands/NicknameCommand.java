package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerDataHandle;
import team.bits.vanilla.fabric.util.Colors;

import static net.minecraft.server.command.CommandManager.literal;

public class NicknameCommand extends Command {

    public NicknameCommand() {
        super("nickname", new CommandInformation()
                        .setDescription("Change your nickname")
                        .setUsage("<nickname>")
                        .setPublic(true),
                "nick"
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("nickname", StringArgumentType.greedyString())
                                .executes(this)
                        )
        );

        dispatcher.register(literal(super.getName())
                .then(literal("clear")
                        .executes(this::clearNickname)
                )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();
        final String nickname = context.getArgument("nickname", String.class);

        PlayerDataHandle dataHandle = PlayerDataHandle.get(player);
        dataHandle.setNickname(nickname);
        dataHandle.save();

        BitsVanilla.adventure().audience(context.getSource())
                .sendMessage(Component.text(String.format("Changed your nickname to '%s'", nickname), Colors.POSITIVE));

        return 1;
    }

    private int clearNickname(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();

        PlayerDataHandle dataHandle = PlayerDataHandle.get(player);
        dataHandle.setNickname(null);
        dataHandle.save();

        BitsVanilla.adventure().audience(context.getSource())
                .sendMessage(Component.text("Cleared your nickname", Colors.POSITIVE));

        return 1;
    }
}
