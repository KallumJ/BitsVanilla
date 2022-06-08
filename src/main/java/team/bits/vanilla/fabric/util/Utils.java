package team.bits.vanilla.fabric.util;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.utils.*;

import java.util.*;

public final class Utils {

    private Utils() {
    }

    public static void updatePlayerDisplayName(@NotNull ServerPlayerEntity player) {
        ServerInstance.get().getPlayerManager().sendToAll(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player)
        );
    }

    public static @NotNull Collection<HostileEntity> getNearbyHostileEntities(@NotNull World world,
                                                                              @NotNull Vec3d center, int range) {
        Box boundingBox = Box.of(center, range, range, range);
        return Collections.unmodifiableCollection(world.getEntitiesByClass(
                HostileEntity.class, boundingBox, LivingEntity::isAlive
        ));
    }
}
