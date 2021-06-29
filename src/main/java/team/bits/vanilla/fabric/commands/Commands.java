package team.bits.vanilla.fabric.commands;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.util.ArrayList;

public class Commands {

    public static final ArrayList<Command> COMMANDS_LIST = new ArrayList<>();

    static {
        addCommand(new DiscordCommand());
        addCommand(new DonateCommand());
        addCommand(new HelpCommand());
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            for (Command command : COMMANDS_LIST) {
                command.registerCommand(dispatcher);
            }
        });
    }

    private static void addCommand(Command command) {
        COMMANDS_LIST.add(command);
    }
}
