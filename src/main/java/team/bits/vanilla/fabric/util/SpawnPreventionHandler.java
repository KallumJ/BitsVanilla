package team.bits.vanilla.fabric.util;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.teleport.Location;
import team.bits.vanilla.fabric.database.warp.WarpUtils;

public class SpawnPreventionHandler {

    public static final SpawnPreventionHandler INSTANCE = new SpawnPreventionHandler();

    private Location centerLocation;
    private int radius;

    private SpawnPreventionHandler() {
    }

    public void init(int radius) {
        Validate.inclusiveBetween(0, 512, radius, "Radius must be between 0 and 512");

        this.radius = radius;

        // we want spawn protection centered on the spawn warp
        WarpUtils.getWarpAsync("spawn").thenAccept(spawn ->
                spawn.ifPresent(warp -> this.centerLocation = warp.location())
        );
    }

    public boolean canSpawn(@NotNull Location location) {
        // if the spawn location isn't loaded yet, don't allow any mob spawning
        if (this.centerLocation == null) {
            return false;
        }

        // if the location is in the same world as the spawn, make sure it is outside the radius
        if (location.world().equals(this.centerLocation.world())) {
            return !location.position().isInRange(this.centerLocation.position(), this.radius);
        }

        // allow unrestricted spawns in all other worlds
        return true;
    }
}
