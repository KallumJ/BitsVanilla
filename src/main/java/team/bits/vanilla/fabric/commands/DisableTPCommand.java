package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.utils.Colors;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;

import static net.minecraft.server.command.CommandManager.literal;

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
                        .then(literal("confirm").executes(this::confirmNoTP))

                        .then(literal("undo")
                                .executes(this::askUndo)
                                .then(literal("confirm").executes(this::confirmUndo))
                        ));


        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        BitsVanilla.audience(context.getSource()).sendMessage(Component.text(ASK_NO_TP, Colors.NEUTRAL));
        return 1;
    }

    public int confirmNoTP(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();

        if (PlayerUtils.hasTPDisabled(requestingPlayer)) {
            return 0;
        }

        PlayerUtils.setNoTP(requestingPlayer, true);
        BitsVanilla.audience(requestingPlayer).sendMessage(Component.text(NO_TP_SUCCESS, Colors.POSITIVE));

        return 1;
    }

    public int askUndo(CommandContext<ServerCommandSource> context) {
        BitsVanilla.audience(context.getSource()).sendMessage(Component.text(ASK_UNDO, Colors.NEUTRAL));
        return 1;
    }

    public int confirmUndo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();

        if (!PlayerUtils.hasTPDisabled(requestingPlayer)) {
            return 0;
        }

        PlayerUtils.setNoTP(requestingPlayer, false);
        BitsVanilla.audience(requestingPlayer).sendMessage(Component.text(UNDO_SUCCESS, Colors.POSITIVE));

        Component broadcast = Component.text(
                String.format(UNDO_BROADCAST, PlayerUtils.getEffectiveName(requestingPlayer)), Colors.NEUTRAL
        );
        for (ServerPlayerEntity player : ServerInstance.get().getPlayerManager().getPlayerList()) {
            BitsVanilla.audience(player).sendMessage(broadcast);
        }

        return 1;
    }
}
