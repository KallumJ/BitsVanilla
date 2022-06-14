package team.bits.vanilla.fabric.freecam;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.*;
import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.base.*;
import team.bits.nibbles.event.server.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.commands.FreecamCommand;
import team.bits.vanilla.fabric.mixin.entity.ExtendedArmorStand;
import team.bits.vanilla.fabric.util.*;
import team.bits.vanilla.fabric.util.heads.*;

import java.util.*;

public class Freecam implements ServerTickEvent.Listener, ServerStoppingEvent.Listener, PlayerDisconnectEvent.Listener {

    private static final Text HOSTILES_MSG = Text.literal("You can't enter freecam now; there are monsters nearby");
    private static final Text NO_FREECAM_MSG = Text.literal("You can't enter freecam now");
    private static final int HOSTILES_RANGE = 10;
    private static final int MAX_BODY_DISTANCE = 50;
    private static final int TOO_FAR_NOTIF_THRESHOLD = 5 * 1000; // ms
    private static final Text TOO_FAR_MESSAGE =
            Text.literal(String.format("You can not stray more than %d blocks from your body", MAX_BODY_DISTANCE));

    private final ServerPlayerEntity player;
    private final Entity bodyEntity;
    private long messageLastSent = 0;

    protected Freecam(@NotNull ServerPlayerEntity player, @NotNull Entity bodyEntity) {
        this.player = player;
        this.bodyEntity = bodyEntity;
    }

    public static boolean isPlayerInFreecam(ServerPlayerEntity player) {
        return FreecamCommand.ACTIVE_FREECAMS.containsKey(player);
    }

    public void removeAndReturnPlayer() {
        this.player.teleport(this.bodyEntity.getX(), this.bodyEntity.getY(), this.bodyEntity.getZ());
        this.player.changeGameMode(GameMode.SURVIVAL);
        this.bodyEntity.remove(Entity.RemovalReason.DISCARDED);
        EventManager.INSTANCE.unregisterEvents(this);
        FreecamCommand.ACTIVE_FREECAMS.remove(player);
    }

    @Override
    public void onServerTick(@NotNull ServerTickEvent event) {
        if (this.hasNearbyHostileEntities()) {
            this.player.sendMessage(HOSTILES_MSG, true);
            this.removeAndReturnPlayer();
        }

        if (this.getDistanceToBody() > MAX_BODY_DISTANCE) {
            // Prevent player going too far from their body
            Vec3d playerPos = this.player.getPos();
            Vec3d bodyPos = this.bodyEntity.getPos();

            Vec3d direction = bodyPos
                    .subtract(playerPos)
                    .normalize()
                    .multiply(0.5);
            Vec3d newPos = playerPos.add(direction);

            this.player.teleport(newPos.x, newPos.y, newPos.z);

            // Notify the player they have strayed too far
            long currentTime = System.currentTimeMillis();
            if (currentTime - messageLastSent > TOO_FAR_NOTIF_THRESHOLD) {
                this.player.sendMessage(TOO_FAR_MESSAGE, MessageTypes.NEGATIVE);
                messageLastSent = currentTime;
            }

        }
    }

    @Override
    public void onServerStopping(@NotNull ServerStoppingEvent event) {
        this.removeAndReturnPlayer();
    }

    @Override
    public void onPlayerDisonnect(@NotNull PlayerDisconnectEvent event) {
        this.removeAndReturnPlayer();
    }

    private boolean hasNearbyHostileEntities() {
        return !Utils.getNearbyHostileEntities(this.bodyEntity.getWorld(), this.bodyEntity.getPos(), HOSTILES_RANGE).isEmpty();
    }

    private int getDistanceToBody() {
        return (int) this.player.distanceTo(this.bodyEntity);
    }

    public static class Factory {
        public static @NotNull Optional<Freecam> create(@NotNull ServerPlayerEntity player) {
            final World world = player.getWorld();
            final Vec3d playerPos = player.getPos();

            if (!Utils.getNearbyHostileEntities(world, playerPos, HOSTILES_RANGE).isEmpty()) {
                player.sendMessage(HOSTILES_MSG, true);
                return Optional.empty();
            }

            if (!isAllowedToEnterFreecam(player)) {
                player.sendMessage(NO_FREECAM_MSG, true);
                return Optional.empty();
            }

            player.changeGameMode(GameMode.SPECTATOR);

            ArmorStandEntity armorStand = new ArmorStandEntity(world, playerPos.x, playerPos.y, playerPos.z);
            ExtendedArmorStand eArmorStand = (ExtendedArmorStand) armorStand;
            eArmorStand.e_setDisabledSlots(0xFFFFFFFF);
            eArmorStand.e_setHideBasePlate(true);
            eArmorStand.e_setShowArms(true);
            armorStand.setInvulnerable(true);
            armorStand.setNoGravity(true);
            armorStand.setCustomName(player.getDisplayName());
            armorStand.setCustomNameVisible(true);

            armorStand.setPos(playerPos.x, playerPos.y, playerPos.z);
            armorStand.setPitch(player.getPitch());
            armorStand.setYaw(player.getYaw());

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                armorStand.equipStack(slot, player.getEquippedStack(slot));
            }

            ItemStack playerHead = MobHeadUtils.getHeadForPlayer(player.getGameProfile().getName());
            armorStand.equipStack(EquipmentSlot.HEAD, playerHead);

            world.spawnEntity(armorStand);

            Freecam body = new Freecam(player, armorStand);

            EventManager.INSTANCE.registerEvents(body);

            return Optional.of(body);
        }

        private static boolean isAllowedToEnterFreecam(@NotNull ServerPlayerEntity player) {
            final World world = player.getWorld();
            final BlockPos headPos = player.getBlockPos().up();

            return world.getBlockState(headPos).getBlock() != Blocks.WATER &&
                    player.isOnGround() &&
                    !player.isOnFire();
        }
    }
}
