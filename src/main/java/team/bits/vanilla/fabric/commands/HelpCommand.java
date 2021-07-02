package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.BitsVanilla;

import java.util.ArrayList;

public class HelpCommand extends Command {

    private final static String CMD_HELP_STRING = "%s - %s";
    private final static String USAGE_STRING = "Usage: %s %s";

    public HelpCommand() {
        super("help", new CommandInformation()
                .setPublic(false)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ArrayList<TextComponent> cmdStrings = new ArrayList<>();

        for (Command command : Commands.COMMANDS_LIST) {
            // If command is to be used by the public
            if (command.getCommandInformation().isPublic()) {
                StringBuilder cmdString = new StringBuilder();

                String commandName = generateCommandString(command);

                CommandInformation helpInformation = command.getCommandInformation();
                String helpDesc = helpInformation.getDescription();

                // Generate string with name and description
                cmdString.append(String.format(CMD_HELP_STRING, commandName, helpDesc));

                TextComponent textComponent = Component.text(cmdString + "\n").color(NamedTextColor.WHITE);

                // If there is usage, add it as a hover event
                if (helpInformation.getUsage() != null) {
                    textComponent = textComponent.hoverEvent(HoverEvent.showText(Component.text(String.format(USAGE_STRING, commandName, helpInformation.getUsage()))));
                }

                // Add the string as a TextComponent to the array list
                cmdStrings.add(textComponent);
            }
        }

        // Create the final text component
        TextComponent message = Component.text("---List of commands--- \n").color(NamedTextColor.GREEN)
                .append(Component.text().append(cmdStrings));

        // Send the message
        BitsVanilla.adventure().audience(context.getSource())
                .sendMessage(message);

        return 1;
    }

    private String generateCommandString(Command command) {
        return "/" + command.getName();
    }
}
