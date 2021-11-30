package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.utils.MojangApiUtils;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerHeadCommand extends Command {

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
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) requestingPlayer;
        String playerHeadString = context.getArgument("player", String.class);
        String userInput = playerHeadString;

        try {
            Optional<String> username = PlayerUtils.getUsername(playerHeadString);
            if (username.isPresent()) {
                playerHeadString = username.get();

            } else {
                if (!MojangApiUtils.checkUsernameIsValid(playerHeadString)) {
                    throw new IllegalArgumentException();
                }
            }

            if (ePlayer.hasItem(Items.DIAMOND, 1) || requestingPlayer.isCreative()) {
                ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD, 1);
                NbtCompound tag = playerHead.getOrCreateNbt();

                tag.putString("SkullOwner", playerHeadString);

                playerHead.setNbt(tag);

                ePlayer.giveItem(playerHead);

                if (!requestingPlayer.isCreative()) {
                    ePlayer.removeItem(Items.DIAMOND, 1);
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (IllegalArgumentException ex) {
            throw new SimpleCommandExceptionType(() -> String.format(INVALID_USERNAME_ERR, userInput)).create();
        } catch (IllegalStateException ex) {
            throw new SimpleCommandExceptionType(() -> NO_DIAMOND_ERR).create();
        }

        return 1;
    }
}
