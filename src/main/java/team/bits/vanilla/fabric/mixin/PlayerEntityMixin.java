package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.nibbles.event.base.EventManager;
import team.bits.nibbles.player.CopyPlayerDataEvent;
import team.bits.nibbles.player.INibblesPlayer;
import team.bits.nibbles.utils.Scheduler;
import team.bits.vanilla.fabric.database.player.PlayerNameLoader;
import team.bits.vanilla.fabric.statistics.lib.StatRecord;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtendedPlayerEntity {

    /**
     * Last time (unix timestamp) at which the player used /rtp
     */
    private long lastRTPTime = 0;

    private PlayerEntity duelTarget;

    private final Map<String, StatRecord> statLevels = new HashMap<>();
    private boolean migratedStats;

    private boolean customClient = false;
    private boolean sendTPS = false;

    private long timePlayed = 0;
    private boolean afk = false;

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

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void incrementPlayTime(CallbackInfo ci) {
        if (!this.afk) {
            this.timePlayed++;
        }
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
        this.statLevels.forEach((key, statRecord) -> stats.putInt(key, statRecord.level()));
        nbt.put("StatLevels", stats);

        nbt.putBoolean("MigratedStats", this.migratedStats);

        nbt.putBoolean("Afk", this.afk);
    }

    /**
     * Read custom data from the player's NBT
     */
    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    public void readCustomDataFromNBt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("LastRTP")) {
            this.lastRTPTime = nbt.getLong("LastRTP");
        }

        if (nbt.contains("StatLevels")) {
            NbtCompound stats = nbt.getCompound("StatLevels");
            stats.getKeys().forEach(id ->
                    this.statLevels.put(id, new StatRecord(stats.getInt(id), 0))
            );
        }

        if (nbt.contains("MigratedStats")) {
            this.migratedStats = nbt.getBoolean("MigratedStats");
        }

        if (nbt.contains("Afk")) {
            this.afk = nbt.getBoolean("Afk");
        }
    }

    public void bitsVanillaCopyFromOldPlayer(@NotNull PlayerEntityMixin oldPlayer) {
        this.lastRTPTime = oldPlayer.lastRTPTime;
        this.statLevels.putAll(oldPlayer.statLevels);
        this.migratedStats = oldPlayer.migratedStats;
        this.customClient = oldPlayer.customClient;
        this.sendTPS = oldPlayer.sendTPS;
        this.afk = false;

        Scheduler.runOffThread(() -> PlayerNameLoader.loadNameData((ServerPlayerEntity) (Object) this));
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
    public @NotNull StatRecord getStatRecord(@NotNull Identifier statId) {
        return this.statLevels.getOrDefault(statId.toString(), new StatRecord(0, 0));
    }

    @Override
    public void setStatRecord(@NotNull Identifier statId, @NotNull StatRecord statRecord) {
        this.statLevels.put(statId.toString(), statRecord);
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

    @Override
    public void setAFK(boolean afk) {
        this.afk = afk;
    }

    @Override
    public long getTimePlayed() {
        return this.timePlayed;
    }

    /*
     * This is a little static event handler for passing the CopyPlayerData
     * event through to the right PlayerEntityMixin instance
     */
    static {
        EventManager.INSTANCE.registerEvents((CopyPlayerDataEvent.Listener) event ->
                _bitsVanillaCopyPlayerData(event.getOldPlayer(), event.getNewPlayer())
        );
    }

    private static void _bitsVanillaCopyPlayerData(@NotNull INibblesPlayer oldPlayer, @NotNull INibblesPlayer newPlayer) {
        ((PlayerEntityMixin) newPlayer).bitsVanillaCopyFromOldPlayer((PlayerEntityMixin) oldPlayer);
    }
}

