package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import team.bits.nibbles.command.*;

import java.util.*;

public class HelpCommand extends Command {

    private static final String CMD_HELP_STRING = "%s - %s %s";
    private static final String USAGE_STRING = "Usage: %s %s";

    public HelpCommand() {
        super("help", new CommandInformation()
                .setPublic(false)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ArrayList<Text> cmdStrings = new ArrayList<>();

        for (Command command : Commands.getCommands()) {
            // If command is to be used by the public
            if (command.getCommandInfo().isPublic()) {
                StringBuilder cmdString = new StringBuilder();

                String commandName = generateCommandString(command);

                CommandInformation helpInformation = command.getCommandInfo();
                String helpDesc = helpInformation.getDescription();
                String aliases = getAliasesString(command.getAliases());

                // Generate string with name, description and aliases
                cmdString.append(String.format(CMD_HELP_STRING, commandName, helpDesc, aliases));

                MutableText textComponent = Text.literal(cmdString + "\n")
                        .styled(style -> style.withColor(Formatting.WHITE));

                // If there is usage, add it as a hover event
                if (helpInformation.getUsage() != null) {
                    textComponent.styled(style ->
                            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(String.format(USAGE_STRING, commandName, helpInformation.getUsage()))
                            ))
                    );
                }

                // Add the string as a TextComponent to the array list
                cmdStrings.add(textComponent);
            }
        }

        // Create the final text component
        MutableText message = Text.literal("---List of commands--- \n")
                .styled(style -> style.withColor(Formatting.GREEN));
        cmdStrings.forEach(message::append);

        // Send the message
        context.getSource().sendFeedback(message, false);

        return 1;
    }

    private String generateCommandString(Command command) {
        return "/" + command.getName();
    }

    private String getAliasesString(String[] aliases) {
        if (aliases.length == 0) {
             return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");

        for (int i = 0; i < aliases.length; i++) {
            stringBuilder.append("/").append(aliases[i]);

            if (i != aliases.length - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
