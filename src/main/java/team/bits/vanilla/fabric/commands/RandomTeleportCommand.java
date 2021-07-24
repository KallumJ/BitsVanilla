package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;
import team.bits.vanilla.fabric.util.ServerInstance;
import team.bits.vanilla.fabric.util.Utils;

import java.util.Date;

public class RandomTeleportCommand extends Command {

    private static final String RTP_COOLDOWN_ERR = "You need to wait %s until next random teleport";
    private static final String OVERWORLD_ONLY_ERR = "You can only use this command in the Overworld";
    private static final String TELEPORTING_MSG = "Teleporting...";

    private static final long RTP_COOLDOWN = 30 * 60 * 1000; // in milliseconds

    public RandomTeleportCommand() {
        super("randomteleport", new CommandInformation()
                        .setDescription("Teleport to a random location in the world")
                        .setPublic(true),
                "rtp"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();
        final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        final Audience audience = BitsVanilla.audience(player);

        // get the current time and the time at which the player last rtp'ed
        long currentTime = new Date().getTime();
        long lastTime = ePlayer.getLastRTPTime();

        // check if the rtp cooldown has elapsed already
        long elapsed = currentTime - lastTime;
        if (elapsed < RTP_COOLDOWN) {
            String cooldown = Utils.formatTimeRemaining(lastTime, currentTime, RTP_COOLDOWN);
            throw new SimpleCommandExceptionType(() -> String.format(RTP_COOLDOWN_ERR, cooldown)).create();
        }

        // make sure the player is in the overworld
        ServerWorld world = player.getServerWorld();
        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            throw new SimpleCommandExceptionType(() -> OVERWORLD_ONLY_ERR).create();
        }

        // tell the player we're starting the teleport
        audience.sendMessage(Component.text(TELEPORTING_MSG, Colors.NEUTRAL));

        // get the world size and spawn point
        WorldBorder border = world.getWorldBorder();
        int size = (int) (Math.round(Math.min(border.getSize(), 100000) / 2) - 1000);
        BlockPos spawn = world.getSpawnPos();

        // teleport the player to a random location inside the border
        MinecraftDedicatedServer server = ServerInstance.get();
        server.enqueueCommand(
                String.format("spreadplayers %s %s 1 %s false %s",
                        spawn.getX(), spawn.getZ(), size, player.getName().getString()
                ),
                server.getCommandSource()
        );

        // store the current time as the last rtp time
        ePlayer.setLastRTPTime(currentTime);

        return 1;
    }
}
