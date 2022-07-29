package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;

import java.util.*;
import java.util.concurrent.*;

import static net.minecraft.server.command.CommandManager.*;

public class DisableTPCommand extends Command {

    private static final String ASK_NO_TP = "Are you sure you want to disable teleporting? Type '/disable-tp confirm' to confirm";
    private static final String NO_TP_SUCCESS = "Successfully disabled teleporting!";
    private static final String ASK_UNDO = "Are you sure you want to take the L and re-enable teleporting? Type '/disable-tp undo confirm' to confirm";
    private static final String UNDO_SUCCESS = "Successfully re-enabled teleporting!";
    private static final String UNDO_BROADCAST = "%s gave up the challenge and re-enabled teleporting";

    public DisableTPCommand() {
        super("disable-tp", new CommandInformation()
                .setDescription("Disable (or re-enable) teleports for yourself")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .executes(this)
                        .then(literal("confirm").executes(AsyncCommand.wrap(this::confirmNoTP)))

                        .then(literal("undo")
                                .executes(this::askUndo)
                                .then(literal("confirm").executes(AsyncCommand.wrap(this::confirmUndo)))
                        ));


        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        player.sendMessage(Text.literal(ASK_NO_TP).styled(style -> style.withColor(Colors.NEUTRAL)));
        return 1;
    }

    public void confirmNoTP(CommandContext<ServerCommandSource> context) throws ExecutionException, InterruptedException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        boolean hasTpDisabled = PlayerApiUtils.getHasTPDisabled(player).get();
        if (!hasTpDisabled) {
            PlayerApiUtils.setNoTP(player, true);
            player.sendMessage(Text.literal(NO_TP_SUCCESS).styled(style -> style.withColor(Colors.POSITIVE)));
        }
    }

    public int askUndo(CommandContext<ServerCommandSource> context) {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        player.sendMessage(Text.literal(ASK_UNDO).styled(style -> style.withColor(Colors.NEUTRAL)));
        return 1;
    }

    public void confirmUndo(CommandContext<ServerCommandSource> context) throws ExecutionException, InterruptedException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        boolean hasTpDisabled = PlayerApiUtils.getHasTPDisabled(player).get();
        if (hasTpDisabled) {
            PlayerApiUtils.setNoTP(player, false);
            player.sendMessage(Text.literal(UNDO_SUCCESS).styled(style -> style.withColor(Colors.POSITIVE)));

            Text broadcast = Text.literal(String.format(UNDO_BROADCAST, PlayerApiUtils.getEffectiveName(player))).styled(style -> style.withColor(Colors.NEUTRAL));
            ServerInstance.broadcast(broadcast);
        }
    }
}
