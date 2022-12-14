package team.bits.vanilla.fabric.commands;

import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;

import java.util.*;

public class Commands {

    private static final Collection<Command> COMMANDS_LIST = new LinkedList<>();

    private Commands() {
    }

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
        addCommand(new TogglePVPCommand());
        addCommand(new ColorNameCommand());
        addCommand(new HatCommand());
        addCommand(new VIPCommand());
        addCommand(new EndLockCommand());
        addCommand(new AFKCommand());
        addCommand(new WhoisCommand());
        addCommand(new RulesCommand());
        addCommand(new VersionCommand());
        addCommand(new PlaytimeCommand());
        addCommand(new DisableTPCommand());
        addCommand(new RTPLockCommand());
        addCommand(new MapCommand());
        addCommand(new ChallengesCommand());
        addCommand(new WorldPregenCommand());
        addCommand(new FreecamCommand());
        addCommand(new ResetChallengeCommand());
    }

    public static @NotNull Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(COMMANDS_LIST);
    }

    private static void addCommand(Command command) {
        CommandManager.INSTANCE.register(command);
        COMMANDS_LIST.add(command);
    }
}
