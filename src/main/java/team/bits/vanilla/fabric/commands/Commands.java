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
        addCommand(new RandomTeleportCommand());
        addCommand(new DuelCommand());
        addCommand(new ColorNameCommand());
        addCommand(new HatCommand());
        addCommand(new VIPCommand());
        addCommand(new EndLockCommand());
        addCommand(new AFKCommand());
        addCommand(new WhoisCommand());
        addCommand(new RulesCommand());
        addCommand(new StatsCommand());
        addCommand(new VersionCommand());
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
