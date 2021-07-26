package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.database.player.PlayerDataHandle;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;
import team.bits.vanilla.fabric.util.Scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtendedPlayerEntity {

    private static final int HEAD_SLOT = 39;

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

    private final Map<String, Integer> statLevels = new HashMap<>();
    private boolean migratedStats;

    private boolean customClient = false;
    private boolean sendTPS = false;

    @Shadow
    public abstract PlayerInventory getInventory();

    /**
     * Caller for the {@link PlayerMoveCallback}
     */
    @Inject(
            method = "tickMovement",
            at = @At("TAIL")
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

    @Redirect(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getName()Lnet/minecraft/text/Text;"
            )
    )
    public Text getCustomName(PlayerEntity playerEntity) {
        Text customName = playerEntity.getCustomName();
        return customName != null ? customName.copy().setStyle(Style.EMPTY) : playerEntity.getName();
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

        NbtCompound stats = new NbtCompound();
        this.statLevels.forEach(stats::putInt);
        nbt.put("StatLevels", stats);

        nbt.putBoolean("MigratedStats", this.migratedStats);
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

        if (nbt.contains("StatLevels")) {
            NbtCompound stats = nbt.getCompound("StatLevels");
            stats.getKeys().forEach(id ->
                    this.statLevels.put(id, stats.getInt(id))
            );
        }

        if (nbt.contains("MigratedStats")) {
            this.migratedStats = nbt.getBoolean("MigratedStats");
        }
    }

    @Override
    public void copyFromOldPlayer(@NotNull ExtendedPlayerEntity oldPlayer) {
        PlayerEntityMixin cOldPlayer = (PlayerEntityMixin) oldPlayer;
        this.hasPlayedBefore = cOldPlayer.hasPlayedBefore;
        this.lastRTPTime = cOldPlayer.lastRTPTime;
        this.statLevels.putAll(cOldPlayer.statLevels);
        this.migratedStats = cOldPlayer.migratedStats;
        this.customClient = cOldPlayer.customClient;
        this.sendTPS = cOldPlayer.sendTPS;

        Scheduler.runOffThread(() -> {
            ServerPlayerEntity self = ServerPlayerEntity.class.cast(this);
            PlayerDataHandle.get(self).loadCustomName(self);
        });
    }

    @Override
    public void giveItem(@NotNull ItemStack itemStack) {
        final ServerPlayerEntity player = ServerPlayerEntity.class.cast(this);
        final PlayerInventory inventory = this.getInventory();

        // try and insert the stack into their inventory
        if (!inventory.insertStack(itemStack)) {

            // if inserting fails, drop the stack on the ground
            ItemEntity itemEntity = player.dropItem(itemStack, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }
    }

    @Override
    public void giveItems(@NotNull Collection<ItemStack> items) {
        items.forEach(this::giveItem);
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

    @Override
    public void insertItemAtHead(ItemStack item) {
        Inventory inventory = getInventory();
        ItemStack itemAtHead = inventory.getStack(HEAD_SLOT);

        inventory.setStack(HEAD_SLOT, item);
        giveItem(itemAtHead);
    }

    @Override
    public int getSlotOfStack(ItemStack item) {
        final PlayerInventory inventory = this.getInventory();

        for (int slot = 0; slot < inventory.size(); slot++) {
            if (inventory.getStack(slot).equals(item)) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public int getStatLevel(@NotNull Identifier statId) {
        return this.statLevels.getOrDefault(statId.toString(), 0);
    }

    @Override
    public void setStatLevel(@NotNull Identifier statId, int level) {
        this.statLevels.put(statId.toString(), level);
    }

    @Override
    public boolean hasMigratedStats() {
        return this.migratedStats;
    }

    @Override
    public void markMigratedStats() {
        this.migratedStats = true;
    }

    @Override
    public void setCustomClient(boolean customClient) {
        this.customClient = customClient;
    }

    @Override
    public boolean isCustomClient() {
        return this.customClient;
    }

    @Override
    public void setSendTPS(boolean sendTPS) {
        this.sendTPS = sendTPS;
    }

    @Override
    public boolean shouldSendTPS() {
        return this.sendTPS;
    }
}

