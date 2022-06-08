package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.commands.*;
import team.bits.vanilla.fabric.util.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(
            method = "moveToWorld",
            at = @At("HEAD"),
            cancellable = true
    )
    private void endLockCheck(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (EndLockCommand.isEndLocked() && destination.getRegistryKey().equals(World.END)) {
            ServerPlayerEntity player = ServerPlayerEntity.class.cast(this);

            player.sendMessage(Text.literal("The end is currently locked"), MessageTypes.NEGATIVE);
            player.teleport(player.getX() + 1, player.getY() + 1, player.getZ() + 1);
            cir.cancel();
        }
    }

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
