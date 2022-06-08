package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.*;
import team.bits.vanilla.fabric.pregen.*;

import java.util.*;
import java.util.concurrent.*;

import static net.minecraft.server.command.CommandManager.*;

public class WorldPregenCommand extends Command {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public WorldPregenCommand() {
        super("world-pregen", new CommandInformation()
                .setDescription("Pre-generate the world")
                .setUsage("<radius>")
                .setPublic(false)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("radius", IntegerArgumentType.integer())
                                .executes(this)
                        )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity requestingPlayer = Objects.requireNonNull(context.getSource().getPlayer());
        int radius = context.getArgument("radius", Integer.class);
        ServerWorld world = requestingPlayer.getWorld();

        ChunkIterator iterator = new ChunkIterator(
                requestingPlayer.getBlockX() / 16, requestingPlayer.getBlockZ() / 16, radius
        );
        WorldPregenerator generator = new WorldPregenerator(world, iterator);
        EXECUTOR_SERVICE.submit(generator);

        return 1;
    }
}
