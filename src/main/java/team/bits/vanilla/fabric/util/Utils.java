package team.bits.vanilla.fabric.util;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.ServerInstance;

import java.util.Arrays;
import java.util.Objects;

public final class Utils {

    private Utils() {
    }

    public static void updatePlayerDisplayName(@NotNull ServerPlayerEntity player) {
        ServerInstance.get().getPlayerManager().sendToAll(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player)
        );
    }
}
