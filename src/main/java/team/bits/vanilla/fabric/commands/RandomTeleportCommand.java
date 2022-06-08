package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.server.dedicated.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.text.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.border.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

public class RandomTeleportCommand extends Command {

    private static final String RTP_COOLDOWN_ERR = "You need to wait %s until next random teleport";
    private static final String OVERWORLD_ONLY_ERR = "You can only use this command in the Overworld";
    private static final String TELEPORTING_MSG = "Teleporting...";

    private static final long RTP_COOLDOWN = 30L * 60L * 1000L; // in milliseconds

    public RandomTeleportCommand() {
        super("randomteleport", new CommandInformation()
                        .setDescription("Teleport to a random location in the world")
                        .setPublic(true),
                "rtp"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (RTPLockCommand.isRTPUnlocked()) {
            final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
            final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;

            // get the current time and the time at which the player last rtp'ed
            long currentTime = new Date().getTime();
            long lastTime = ePlayer.getLastRTPTime();

            // check if the rtp cooldown has elapsed already
            long elapsed = currentTime - lastTime;
            if (elapsed < RTP_COOLDOWN) {
                String cooldown = TextUtils.formatTimeRemaining(lastTime, currentTime, RTP_COOLDOWN);
                throw new SimpleCommandExceptionType(() -> String.format(RTP_COOLDOWN_ERR, cooldown)).create();
            }

            // make sure the player is in the overworld
            ServerWorld world = player.getWorld();
            if (!world.getRegistryKey().equals(World.OVERWORLD)) {
                throw new SimpleCommandExceptionType(() -> OVERWORLD_ONLY_ERR).create();
            }

            // tell the player we're starting the teleport
            player.sendMessage(Text.literal(TELEPORTING_MSG), MessageTypes.NEUTRAL);

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
        } else {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player != null) {
                player.sendMessage(Text.literal("The random teleport command is currently locked"), MessageTypes.NEGATIVE);
            }

        }


        return 1;
    }
}
