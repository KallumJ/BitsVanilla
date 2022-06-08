package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

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
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        final String nickname = context.getArgument("nickname", String.class);

        PlayerApiUtils.setNickname(player, nickname);
        PlayerNameLoader.loadNameData(player);

        player.sendMessage(
                Text.literal(String.format("Changed your nickname to '%s'", nickname)),
                MessageTypes.POSITIVE
        );

        return 1;
    }

    private int clearNickname(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        PlayerApiUtils.setNickname(player, null);
        PlayerNameLoader.loadNameData(player);

        player.sendMessage(Text.literal("Cleared your nickname"), MessageTypes.POSITIVE);

        return 1;
    }
}
