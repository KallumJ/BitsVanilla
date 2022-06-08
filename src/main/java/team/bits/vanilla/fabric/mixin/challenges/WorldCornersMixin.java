package team.bits.vanilla.fabric.mixin.challenges;

import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

@Mixin(PlayerEntity.class)
public class WorldCornersMixin {

    private static final int BORDER_MARGIN = 20;

    @Inject(
            method = "tickMovement",
            at = @At("RETURN")
    )
    public void handleWorldCornersChallenge(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) this;

        World world = self.getWorld();
        BlockPos playerPosition = self.getBlockPos();

        if (!ePlayer.hasCompletedChallenge(Challenges.WORLD_CORNERS)) {
            if (isInWorldCorner(playerPosition, world)) {
                WorldCorner corner = getCurrentCorner(playerPosition);

                if (!ePlayer.hasVisitedCorner(corner)) {
                    ePlayer.markVisitedCorner(corner);

                    if (hasVisitedAllCorners(ePlayer)) {
                        ePlayer.markChallengeCompleted(Challenges.WORLD_CORNERS);
                    }
                }
            }
        }
    }

    private static boolean isInWorldCorner(@NotNull BlockPos position, @NotNull World world) {
        int borderSize = ((int) world.getWorldBorder().getSize()) >> 1;
        return Math.abs(position.getX()) > (borderSize - BORDER_MARGIN) &&
                Math.abs(position.getZ()) > (borderSize - BORDER_MARGIN);
    }

    private static @NotNull WorldCorner getCurrentCorner(@NotNull BlockPos position) {
        int x = position.getX();
        int z = position.getZ();
        if (x < 0) {
            return (z < 0) ? WorldCorner.NORTH_WEST : WorldCorner.SOUTH_WEST;
        } else {
            return (z < 0) ? WorldCorner.NORTH_EAST : WorldCorner.SOUTH_EAST;
        }
    }

    private static boolean hasVisitedAllCorners(@NotNull ExtendedPlayerEntity ePlayer) {
        for (WorldCorner corner : WorldCorner.values()) {
            if (!ePlayer.hasVisitedCorner(corner)) {
                return false;
            }
        }
        return true;
    }
}
