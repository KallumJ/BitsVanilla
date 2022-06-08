package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.fabricmc.loader.api.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;

public class VersionCommand extends Command {

    private static String serverVersion;
    private static String bitsVersion;

    // we have to wait until a server instance is
    // created before we can request the version.
    // this method is called once the server
    // instance is ready
    public static void init() {
        serverVersion = ServerInstance.get().getVersion();

        // ask the fabric loader for our version
        bitsVersion = FabricLoader.getInstance()
                .getModContainer("bits-vanilla-fabric").orElseThrow()
                .getMetadata().getVersion().getFriendlyString();
    }

    public VersionCommand() {
        super("version", new CommandInformation()
                        .setDescription("See the version of Minecraft and BitsVanilla")
                        .setPublic(true),
                "ver"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(
                Text.empty()
                        .append(Text.literal(String.format("Server version %s", serverVersion)).styled(style -> style.withColor(Colors.POSITIVE)))
                        .append(Text.literal("\n"))
                        .append(Text.literal(String.format("Bits version %s", bitsVersion)).styled(style -> style.withColor(Colors.POSITIVE))),
                false
        );
        return 1;
    }
}
