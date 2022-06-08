package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.item.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;
import team.bits.vanilla.fabric.util.heads.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class PlayerHeadCommand extends AsyncCommand {

    private static final String NO_DIAMOND_ERR = "A diamond is required to get a player head";
    private static final String INVALID_USERNAME_ERR = "Username %s is invalid";

    public PlayerHeadCommand() {
        super("playerhead", new CommandInformation()
                        .setDescription("Gives the player the specified players head, at the cost of a diamond")
                        .setUsage("<player>")
                        .setPublic(true),
                "ph"
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                .executes(this)
                                .suggests(CommandSuggestionUtils.ALL_PLAYERS)
                        )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public void runAsync(@NotNull CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {

        final ServerPlayerEntity requestingPlayer = Objects.requireNonNull(context.getSource().getPlayer());
        final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) requestingPlayer;

        String playerHeadString = context.getArgument("player", String.class);
        final String userInput = playerHeadString;

        Optional<String> username = PlayerApiUtils.getUsernameForNickname(playerHeadString);
        if (username.isPresent()) {
            playerHeadString = username.get();

        } else {
            if (!MojangApiUtils.checkUsernameIsValid(playerHeadString)) {
                throw new SimpleCommandExceptionType(() -> String.format(INVALID_USERNAME_ERR, userInput)).create();
            }
        }

        if (ePlayer.hasItem(Items.DIAMOND, 1) || requestingPlayer.isCreative()) {
            ePlayer.giveItem(MobHeadUtils.getHeadForPlayer(playerHeadString));

            if (!requestingPlayer.isCreative()) {
                ePlayer.removeItem(Items.DIAMOND, 1);
            }
        } else {
            throw new SimpleCommandExceptionType(() -> NO_DIAMOND_ERR).create();
        }
    }
}
