package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.lwjgl.system.CallbackI;
import team.bits.vanilla.fabric.BitsVanilla;

import java.util.ArrayList;

public class HelpCommand extends Command {

    private final static String CMD_HELP_STRING = "%s - %s";
    private final static String USAGE_STRING = " (Usage: %s)";

    public HelpCommand() {
        super("help", new CommandHelpInformation()
            .setPublic(false)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ArrayList<TextComponent> cmdStrings = new ArrayList<>();

        for (Command command : Commands.COMMANDS_LIST) {
            // If command is to be used by the public
            if (command.getHelpInformation().isPublic()) {
                StringBuilder cmdString = new StringBuilder();

                String commandName = generateCommandString(command);

                CommandHelpInformation helpInformation = command.getHelpInformation();
                String helpDesc = helpInformation.getDescription();

                // Generate string with name and description
                cmdString.append(String.format(CMD_HELP_STRING, commandName, helpDesc));

                // If a usage is specified, add that too!
                if (helpInformation.getUsage() != null) {
                    cmdString.append(String.format(USAGE_STRING, helpInformation.getUsage()));
                }

                // Add the string as a TextComponent to the array list
                cmdStrings.add(Component.text(cmdString + "\n").color(NamedTextColor.WHITE));
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
