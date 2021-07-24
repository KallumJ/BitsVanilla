package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerDataHandle;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.color.NameColor;
import team.bits.vanilla.fabric.util.color.NameColors;

import java.util.Collection;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class ColorNameCommand extends Command {

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

    public int list(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();
        final Audience audience = BitsVanilla.adventure().audience(source);

        audience.sendMessage(Component.text(CLICK_CATEGORY_MSG, Colors.NEUTRAL));

        NameColors.INSTANCE.getColours().forEach((key, value) -> {
            TextColor textColor = TextColor.color(value.get(0).getRGB());

            audience.sendMessage(
                    Component.text(key, textColor)
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Click to see all ")
                                            .append(Component.text(key, textColor))
                                            .append(Component.text(" colors"))
                            ))
                            .clickEvent(ClickEvent.runCommand(
                                    String.format("/cn category %s", key)
                            ))
            );
        });

        return 1;
    }

    public int showCategory(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final Audience audience = BitsVanilla.adventure().audience(source);

        String category = context.getArgument("category", String.class);

        audience.sendMessage(Component.text(CLICK_COLOR_MSG, Colors.NEUTRAL));

        Collection<NameColor> colours;
        try {
            colours = NameColors.INSTANCE.getCategory(category);
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(() -> String.format(INVALID_CATEGORY_ERR, category)).create();
        }

        colours.forEach(shade -> {
            TextColor textColor = TextColor.color(shade.getRGB());
            audience.sendMessage(
                    Component.text(shade.name(), textColor)
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Click to change your color to ")
                                            .append(Component.text(shade.name(), textColor))
                                    )
                            )
                            .clickEvent(ClickEvent.runCommand(
                                    String.format("/cn set %s %s", category, shade.name())
                            ))
            );
        });

        return 1;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();
        final Audience audience = BitsVanilla.adventure().audience(player);

        if (PlayerUtils.isVIP(player)) {

            String category = context.getArgument("category", String.class);
            String colorName = context.getArgument("color", String.class);

            Optional<NameColor> color = NameColors.INSTANCE.getColour(category, colorName);
            if (color.isPresent()) {
                PlayerDataHandle playerData = PlayerDataHandle.get(player);
                playerData.setColour(color.get().color());
                playerData.save();

                audience.sendMessage(
                        Component.text(String.format(COLOR_CHANGED_MSG, colorName), Colors.POSITIVE)
                );
            } else {
                throw new SimpleCommandExceptionType(() -> String.format(INVALID_COLOR_ERR, colorName)).create();
            }

        } else {
            throw new SimpleCommandExceptionType(() -> NO_PERMISSION_ERR).create();
        }

        return 1;
    }
}
