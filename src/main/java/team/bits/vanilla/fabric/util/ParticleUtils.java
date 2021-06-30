package team.bits.vanilla.fabric.util;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class ParticleUtils {

    private ParticleUtils() {
    }

    public static void spawnParticle(@NotNull World world, @NotNull ParticleEffect parameters, @NotNull Vec3d pos, int count, double deltaX, double deltaY, double deltaZ) {
        ((ServerWorld) world).spawnParticles(parameters, pos.x, pos.y, pos.z, count, deltaX, deltaY, deltaZ, 0);
    }
}
