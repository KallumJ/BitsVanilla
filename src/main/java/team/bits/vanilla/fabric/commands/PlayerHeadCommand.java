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
import team.bits.vanilla.fabric.util.MojangApiUtils;
import team.bits.vanilla.fabric.util.PlayerWrapper;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerHeadCommand extends Command {

    private static final String NO_DIAMOND_ERR = "A diamond is required to get a player head";
    private static final String INVALID_USERNAME_ERR = "Username %s is invalid";

    public PlayerHeadCommand() {
        super("playerhead", new String[]{"ph"}, new CommandInformation()
                .setDescription("Gives the player the specified players head, at the cost of a diamond")
                .setUsage("<player>")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(literal(super.getName()).then(CommandManager.argument("player", StringArgumentType.string()).executes(this)));

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();
        PlayerWrapper playerWrapper = new PlayerWrapper(requestingPlayer);
        String playerHeadString = context.getArgument("player", String.class);

        try {
            if (!MojangApiUtils.checkUsernameIsValid(playerHeadString)) {
                throw new IllegalArgumentException();
            }

            if (playerWrapper.checkPlayerHasItem(Items.DIAMOND)) {
                ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD, 1);
                NbtCompound tag = playerHead.getOrCreateTag();

                tag.putString("SkullOwner", playerHeadString);

                playerHead.setTag(tag);

                playerWrapper.giveItem(playerHead);
                playerWrapper.removeItem(playerWrapper.getSlotOfItem(Items.DIAMOND), 1);

            } else {
                throw new IllegalStateException();
            }
        } catch (IllegalArgumentException ex) {
            throw new SimpleCommandExceptionType(() -> String.format(INVALID_USERNAME_ERR, playerHeadString)).create();
        } catch (IllegalStateException ex) {
            throw new SimpleCommandExceptionType(() -> NO_DIAMOND_ERR).create();
        }

        return 1;
    }
}
