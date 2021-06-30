package team.bits.vanilla.fabric.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public record Location(Vec3d position, World world) {

    public @NotNull Location add(double x, double y, double z) {
        return new Location(this.position.add(x, y, z), this.world);
    }

    public static @NotNull Location get(@NotNull Entity entity) {
        return new Location(entity.getPos(), entity.getEntityWorld());
    }

    public double x() {
        return this.position.x;
    }

    public double y() {
        return this.position.y;
    }

    public double z() {
        return this.position.z;
    }
}
