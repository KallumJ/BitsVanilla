package team.bits.vanilla.fabric.commands;

import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandManager;

import java.util.ArrayList;

public class Commands {

    public static final ArrayList<Command> COMMANDS_LIST = new ArrayList<>();

    public static void registerCommands() {
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

    private static void addCommand(Command command) {
        CommandManager.INSTANCE.register(command);
        COMMANDS_LIST.add(command);
    }
}
