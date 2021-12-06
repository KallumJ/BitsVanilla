package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.teleport.Location;
import team.bits.nibbles.utils.Colors;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.database.warp.Warp;
import team.bits.vanilla.fabric.database.warp.WarpUtils;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;

import static net.minecraft.server.command.CommandManager.literal;

public class WarpCommand extends Command {

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
                        .executes(this::listWarps)

                        // if the first argument is a warp, execute the default handler
                        .then(CommandManager.argument("warp", StringArgumentType.greedyString())
                                .suggests(CommandSuggestionUtils.WARPS)
                                .executes(this)
                        )

                        // if the first argument is 'set' and the second is a name, run the setWarp handler
                        .then(literal("set")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .executes(this::setWarp)
                                )
                        )

                        // if the first argument is 'del' and the second is a warp, run the delWarp handler
                        .then(literal("del")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("warp", StringArgumentType.greedyString())
                                        .suggests(CommandSuggestionUtils.WARPS)
                                        .executes(this::delWarp)
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
    public int run(@NotNull CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();
        final String warpName = context.getArgument("warp", String.class);

        if (PlayerUtils.hasTPDisabled(player)) {
            BitsVanilla.audience(player).sendMessage(Component.text(TELEPORTS_DISABLED, Colors.NEGATIVE));
            return 1;
        }

        WarpUtils.getWarpAsync(warpName).thenAccept(warp -> {
            if (warp.isPresent()) {
                Location location = warp.get().location();
                Teleporter.queueTeleport(player, location, true);

            } else {
                BitsVanilla.audience(player).sendMessage(
                        Component.text(String.format(WARP_NOT_FOUND_ERR, warpName), Colors.NEGATIVE)
                );
            }
        });

        return 1;
    }

    public int listWarps(@NotNull CommandContext<ServerCommandSource> context) {
        final Audience audience = BitsVanilla.adventure().audience(context.getSource());

        audience.sendMessage(
                Component.text(WARP_HEADER, Colors.HEADER)
                        .append(Component.newline())
                        .append(Component.text(WARP_SUBHEADER, Style.style(Colors.NEUTRAL, TextDecoration.ITALIC)))
        );

        WarpUtils.getWarpsListAsync().thenAccept(warps -> {
            for (String warpName : warps) {
                audience.sendMessage(
                        Component.text(warpName, NamedTextColor.WHITE)
                                .hoverEvent(HoverEvent.showText(
                                        Component.text(String.format(WARP_LIST_TOOLTIP, warpName))
                                ))
                                .clickEvent(ClickEvent.runCommand(
                                        String.format(WARP_COMMAND, warpName)
                                ))
                );
            }
        });

        return 1;
    }

    public int setWarp(@NotNull CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();
        final String warpName = context.getArgument("name", String.class);
        final Location location = Location.get(player);

        WarpUtils.getWarpAsync(warpName).thenAccept(existingWarp -> {
            if (existingWarp.isEmpty()) {

                Warp warp = new Warp(warpName, location);
                WarpUtils.addWarpAsync(warp).thenAccept(success -> {
                    if (success) {
                        BitsVanilla.adventure().audience(context.getSource())
                                .sendMessage(Component.text(WARP_SET, Colors.POSITIVE));
                    } else {
                        BitsVanilla.adventure().audience(context.getSource())
                                .sendMessage(Component.text(WARP_SET_FAIL, Colors.NEGATIVE));
                    }
                });

            } else {
                BitsVanilla.audience(player).sendMessage(Component.text(WARP_EXISTS, Colors.NEGATIVE));
            }
        });

        return 1;
    }

    public int delWarp(@NotNull CommandContext<ServerCommandSource> context) {
        final String warpName = context.getArgument("warp", String.class);

        WarpUtils.getWarpAsync(warpName).thenAccept(warp -> {
            if (warp.isPresent()) {

                WarpUtils.deleteWarpAsync(warp.get()).thenAccept(success -> {
                    if (success) {
                        BitsVanilla.adventure().audience(context.getSource())
                                .sendMessage(Component.text(WARP_DELETED, Colors.POSITIVE));
                    } else {
                        BitsVanilla.adventure().audience(context.getSource())
                                .sendMessage(Component.text(WARP_DELETE_FAIL, Colors.NEGATIVE));
                    }
                });

            } else {
                BitsVanilla.audience(context.getSource()).sendMessage(
                        Component.text(String.format(WARP_NOT_FOUND_ERR, warpName), Colors.NEGATIVE)
                );
            }
        });

        return 1;
    }
}
