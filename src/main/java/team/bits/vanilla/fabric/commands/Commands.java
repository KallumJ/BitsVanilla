package team.bits.vanilla.fabric.commands;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.util.ArrayList;

public class Commands {

    public static final ArrayList<Command> COMMANDS_LIST = new ArrayList<>();

    static {
        addCommand(new DiscordCommand());
        addCommand(new DonateCommand());
        addCommand(new HelpCommand());
        addCommand(new BeamCommand());
        addCommand(new PlayerHeadCommand());
        addCommand(new BedCommand());
        addCommand(new ChunkInspectCommand());
        addCommand(new NicknameCommand());
        addCommand(new WarpCommand());
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
