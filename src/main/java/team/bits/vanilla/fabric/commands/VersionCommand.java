package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.ServerInstance;

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
        super("version", new String[]{"ver"}, new CommandInformation()
                .setDescription("See the version of Minecraft and BitsVanilla")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BitsVanilla.audience(context.getSource()).sendMessage(
                Component.text()
                        .append(Component.text(String.format("Server version %s", serverVersion), Colors.POSITIVE))
                        .append(Component.newline())
                        .append(Component.text(String.format("Bits version %s", bitsVersion), Colors.POSITIVE))
                        .build()
        );
        return 1;
    }
}
