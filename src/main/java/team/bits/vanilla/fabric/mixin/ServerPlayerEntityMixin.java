package team.bits.vanilla.fabric.mixin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.nibbles.utils.Colors;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.commands.EndLockCommand;
import team.bits.vanilla.fabric.util.AFKManager;

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

            BitsVanilla.audience(player).sendMessage(Component.text("The end is currently locked", Colors.NEGATIVE));
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
                        style.withColor(NamedTextColor.GRAY.value()).withItalic(true)
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
