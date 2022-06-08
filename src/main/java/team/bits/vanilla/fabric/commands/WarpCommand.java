package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.teleport.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;
import java.util.concurrent.*;

import static net.minecraft.server.command.CommandManager.*;

public class WarpCommand extends AsyncCommand {

    private static final String WARP_NOT_FOUND_ERR = "Cannot find warp '%s'";
    private static final String WARP_LIST_TOOLTIP = "Click to warp to %s";
    private static final String WARP_COMMAND = "/warp %s";
    private static final String WARP_HEADER = "Warps:";
    private static final String WARP_SUBHEADER = "Click on a warp to go there!";
    private static final String WARP_EXISTS = "Warp already exists";
    private static final String WARP_SET = "Warp set!";
    private static final String WARP_SET_FAIL = "Warp set failed!";
    private static final String WARP_DELETED = "Warp deleted!";
    private static final String WARP_DELETE_FAIL = "Warp delete failed!";
    private static final String TELEPORTS_DISABLED = "You have teleporting disabled!";

    public WarpCommand() {
        super("warp", new CommandInformation()
                .setDescription("Teleport to a warp")
                .setUsage("<warp>")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        // if no arguments are given, list all the warps
                        .executes(AsyncCommand.wrap(this::listWarps))

                        // if the first argument is a warp, execute the default handler
                        .then(CommandManager.argument("warp", StringArgumentType.greedyString())
                                .suggests(CommandSuggestionUtils.WARPS)
                                .executes(this)
                        )

                        // if the first argument is 'set' and the second is a name, run the setWarp handler
                        .then(literal("set")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .executes(AsyncCommand.wrap(this::setWarp))
                                )
                        )

                        // if the first argument is 'del' and the second is a warp, run the delWarp handler
                        .then(literal("del")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("warp", StringArgumentType.greedyString())
                                        .suggests(CommandSuggestionUtils.WARPS)
                                        .executes(AsyncCommand.wrap(this::delWarp))
                                )
                        )
        );

        // forward /spawn to /warp spawn
        dispatcher.register(literal("spawn").executes(context ->
                dispatcher.execute("warp spawn", context.getSource())
        ));

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public void runAsync(@NotNull CommandContext<ServerCommandSource> context) throws InterruptedException, ExecutionException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        final String warpName = context.getArgument("warp", String.class);

        boolean hasTpDisabled = PlayerApiUtils.getHasTPDisabled(player).get();
        if (!hasTpDisabled) {

            Optional<Warp> warp = WarpApiUtils.getWarpAsync(warpName).get();
            if (warp.isPresent()) {
                Location location = warp.get().location();
                Teleporter.queueTeleport(player, location, true);

            } else {
                player.sendMessage(
                        Text.literal(String.format(WARP_NOT_FOUND_ERR, warpName)),
                        MessageTypes.NEGATIVE
                );
            }

        } else {
            player.sendMessage(Text.literal(TELEPORTS_DISABLED), MessageTypes.NEGATIVE);
        }
    }

    public void listWarps(@NotNull CommandContext<ServerCommandSource> context) throws ExecutionException, InterruptedException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        player.sendMessage(
                Text.literal(WARP_HEADER)
                        .append(Text.literal("\n"))
                        .append(Text.literal(WARP_SUBHEADER)
                                .styled(style -> style
                                        .withColor(Colors.NEUTRAL)
                                        .withItalic(true)
                                )),
                MessageTypes.HEADER
        );

        Collection<String> warps = WarpApiUtils.getWarpsListAsync().get();
        for (String warpName : warps) {
            player.sendMessage(
                    Text.literal(warpName)
                            .styled(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal(String.format(WARP_LIST_TOOLTIP, warpName))
                                    ))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            String.format(WARP_COMMAND, warpName)
                                    ))
                            ),
                    MessageTypes.PLAIN
            );
        }
    }

    public void setWarp(@NotNull CommandContext<ServerCommandSource> context) throws ExecutionException, InterruptedException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        final String warpName = context.getArgument("name", String.class);
        final Location location = Location.get(player);

        Optional<Warp> existingWarp = WarpApiUtils.getWarpAsync(warpName).get();
        if (existingWarp.isEmpty()) {

            Warp warp = new Warp(warpName, location);
            boolean success = WarpApiUtils.addWarpAsync(warp).get();
            if (success) {
                player.sendMessage(
                        Text.literal(WARP_SET),
                        MessageTypes.POSITIVE
                );
            } else {
                player.sendMessage(
                        Text.literal(WARP_SET_FAIL),
                        MessageTypes.NEGATIVE
                );
            }

        } else {
            player.sendMessage(
                    Text.literal(WARP_EXISTS),
                    MessageTypes.NEGATIVE
            );
        }
    }

    public void delWarp(@NotNull CommandContext<ServerCommandSource> context) throws ExecutionException, InterruptedException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        final String warpName = context.getArgument("warp", String.class);

        Optional<Warp> warp = WarpApiUtils.getWarpAsync(warpName).get();
        if (warp.isPresent()) {

            boolean success = WarpApiUtils.deleteWarpAsync(warp.get()).get();
            if (success) {
                player.sendMessage(Text.literal(WARP_DELETED), MessageTypes.POSITIVE);
            } else {
                player.sendMessage(Text.literal(WARP_DELETE_FAIL), MessageTypes.NEGATIVE);
            }

        } else {
            player.sendMessage(Text.literal(String.format(WARP_NOT_FOUND_ERR, warpName)), MessageTypes.NEGATIVE);
        }
    }
}
