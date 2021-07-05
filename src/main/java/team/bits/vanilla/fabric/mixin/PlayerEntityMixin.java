package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtendedPlayerEntity {

    private Vec3d previousPos = Vec3d.ZERO;

    /**
     * Last time (unix timestamp) at which the player used /rtp
     */
    private long lastRTPTime = 0;

    /**
     * True if the player has played on the server before, false if
     * this is the first time the player joined.
     */
    private boolean hasPlayedBefore;

    private PlayerEntity duelTarget;

    @Shadow
    public abstract PlayerInventory getInventory();

    /**
     * Caller for the {@link PlayerMoveCallback}
     */
    @Inject(
            method = "tickMovement",
            at = @At(value = "TAIL")
    )
    private void onTickMovement(CallbackInfo ci) {
        PlayerEntity player = PlayerEntity.class.cast(this);
        Vec3d currentPos = player.getPos();
        Vec3d moveVector = this.previousPos.subtract(currentPos);

        // only trigger if the player moved more than 1/10'th of a block
        if (moveVector.length() > 0.1) {
            PlayerMoveCallback.EVENT.invoker().onPlayerMove(player, moveVector);
        }

        this.previousPos = currentPos;
    }

    /**
     * Caller for the {@link PlayerDamageCallback}
     */
    @Inject(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"
            )
    )
    private void onDamage(DamageSource source, float amount, CallbackInfo ci) {
        PlayerEntity player = PlayerEntity.class.cast(this);
        PlayerDamageCallback.EVENT.invoker().onPlayerDamage(player, source, amount);
    }

    /**
     * Write custom data to the player's NBT
     */
    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.lastRTPTime > 0) {
            nbt.putLong("LastRTP", this.lastRTPTime);
        }
    }

    /**
     * Read custom data from the player's NBT
     */
    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    public void readCustomDataFromNBt(NbtCompound nbt, CallbackInfo ci) {
        this.hasPlayedBefore = true;
        if (nbt.contains("LastRTP")) {
            this.lastRTPTime = nbt.getLong("LastRTP");
        }
    }

    @Override
    public void giveItem(@NotNull ItemStack itemStack) {
        final PlayerInventory inventory = this.getInventory();

        int occupiedSlot = inventory.getOccupiedSlotWithRoomForStack(itemStack);

        if (occupiedSlot != -1) {
            inventory.insertStack(occupiedSlot, itemStack);
        } else {
            inventory.insertStack(inventory.getEmptySlot(), itemStack);
        }
    }

    @Override
    public boolean hasItem(@NotNull Item item, int amount) {
        final PlayerInventory inventory = this.getInventory();

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isOf(item) && stack.getCount() >= amount) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeItem(@NotNull Item item, int amount) {
        final PlayerInventory inventory = this.getInventory();

        for (int slot = 0; slot < inventory.size(); slot++) {
            if (inventory.getStack(slot).isOf(item)) {
                inventory.removeStack(slot, amount);
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<PlayerEntity> getDuelTarget() {
        return Optional.ofNullable(this.duelTarget);
    }

    @Override
    public void setDuelTarget(@Nullable PlayerEntity player) {
        this.duelTarget = player;
    }

    @Override
    public long getLastRTPTime() {
        return this.lastRTPTime;
    }

    @Override
    public void setLastRTPTime(long time) {
        this.lastRTPTime = time;
    }

    @Override
    public boolean hasPlayedBefore() {
        return this.hasPlayedBefore;
    }
}

