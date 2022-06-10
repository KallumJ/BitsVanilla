package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import team.bits.vanilla.fabric.util.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Overwrite
    public @Nullable Text getPlayerListName() {
        final ServerPlayerEntity self = ServerPlayerEntity.class.cast(this);

        // get the player's custom name (name/nickname + color)
        Text customName = self.getCustomName();

        if (!AFKManager.isVisuallyAfk(self)) {
            // if the player isn't afk, we can just return the custom name
            return customName;

        } else {
            if (customName != null) {

                // take the player's custom name but restyle it to be gray and italic
                return customName.copy().styled(style ->
                        style.withColor(Formatting.GRAY).withItalic(true)
                );

            } else {
                // customName should only be null if the name is still loading
                // from the database so we can safely return null here
                // (returning null means use the player's normal username)
                return null;
            }
        }
    }
}
