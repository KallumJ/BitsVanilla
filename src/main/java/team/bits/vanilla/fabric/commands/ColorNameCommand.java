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
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.awt.*;
import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class ColorNameCommand extends AsyncCommand {

    private static final String CLICK_CATEGORY_MSG = "Click on a category to see the colors!";
    private static final String CLICK_COLOR_MSG = "Click on a color to use it!";
    private static final String COLOR_CHANGED_MSG = "You colored your name %s";
    private static final String NO_PERMISSION_ERR = "You need to donate in order to use this! Do /donate for more information";
    private static final String INVALID_CATEGORY_ERR = "Cannot find category %s";
    private static final String INVALID_COLOR_ERR = "Cannot find color %s";

    public ColorNameCommand() {
        super("colorname", new CommandInformation()
                        .setDescription("Change the color of your name")
                        .setUsage("<player>")
                        .setPublic(true),
                "cn"
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .executes(this::list)
                        .then(literal("list")
                                .executes(this::list)
                        )
                        .then(literal("category")
                                .then(CommandManager.argument("category", StringArgumentType.string())
                                        .executes(this::showCategory)
                                )
                        )
                        .then(literal("set")
                                .then(CommandManager.argument("category", StringArgumentType.string())
                                        .then(CommandManager.argument("color", StringArgumentType.string())
                                                .executes(this)
                                        )
                                )
                        )
        );

        dispatcher.register(
                literal("cn")
                        .executes(this::list)
                        .redirect(commandNode)
        );
    }

    public int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        player.sendMessage(Text.literal(CLICK_CATEGORY_MSG), MessageTypes.NEUTRAL);

        NameColors.INSTANCE.getColours().forEach((key, value) -> {
            Color color = value.get(0).color();
            TextColor textColor = TextColor.fromRgb(
                    color.getRed() << (16) |
                            color.getGreen() << (8) |
                            color.getBlue()
            );

            player.sendMessage(
                    Text.literal(key).styled(style -> style
                            .withColor(textColor)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("Click to see all ")
                                            .append(Text.literal(key).styled(s -> s.withColor(textColor)))
                                            .append(Text.literal(" colors"))
                            ))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/cn category %s", key)
                            ))),
                    MessageTypes.PLAIN
            );
        });

        return 1;
    }

    public int showCategory(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        final String category = context.getArgument("category", String.class);

        player.sendMessage(Text.literal(CLICK_COLOR_MSG), MessageTypes.NEUTRAL);

        Collection<NameColor> colours;
        try {
            colours = NameColors.INSTANCE.getCategory(category);
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(() -> String.format(INVALID_CATEGORY_ERR, category)).create();
        }

        colours.forEach(shade -> {
            Color color = shade.color();
            TextColor textColor = TextColor.fromRgb(
                    color.getRed() << (16) |
                            color.getGreen() << (8) |
                            color.getBlue()
            );
            player.sendMessage(
                    Text.literal(shade.name()).styled(style -> style
                            .withColor(textColor)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("Click to change your color to ")
                                                    .append(Text.literal(shade.name())
                                                            .styled(s -> s.withColor(textColor)))
                                    )
                            )
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/cn set %s %s", category, shade.name())
                            ))),
                    MessageTypes.PLAIN
            );
        });

        return 1;
    }

    @Override
    public void runAsync(@NotNull CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        if (PlayerApiUtils.isVIP(player)) {

            String category = context.getArgument("category", String.class);
            String colorName = context.getArgument("color", String.class);

            Optional<NameColor> color = NameColors.INSTANCE.getColour(category, colorName);
            if (color.isPresent()) {
                PlayerApiUtils.setColor(player, color.get().color());
                PlayerNameLoader.loadNameData(player);

                player.sendMessage(Text.literal(String.format(COLOR_CHANGED_MSG, colorName)), MessageTypes.POSITIVE);
            } else {
                throw new SimpleCommandExceptionType(() -> String.format(INVALID_COLOR_ERR, colorName)).create();
            }

        } else {
            throw new SimpleCommandExceptionType(() -> NO_PERMISSION_ERR).create();
        }
    }
}
