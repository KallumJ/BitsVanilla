package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.nibbles.event.base.*;
import team.bits.nibbles.player.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtendedPlayerEntity {

    /**
     * Last time (unix timestamp) at which the player used /rtp
     */
    private long lastRTPTime = 0;

    private PlayerEntity duelTarget;

    private boolean customClient = false;
    private boolean sendTPS = false;

    private long timePlayed = 0;
    private boolean afk = false;

    private boolean pvpEnabled = false;

    private final Set<String> completedChallenges = new HashSet<>();
    private final Set<WorldCorner> visitedWorldCorners = EnumSet.noneOf(WorldCorner.class);

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

        nbt.putBoolean("Afk", this.afk);

        nbt.putLong("Playtime", this.timePlayed);

        NbtList challenges = new NbtList();
        this.completedChallenges.forEach(challenge -> challenges.add(NbtString.of(challenge)));
        nbt.put("CompletedChallenges", challenges);

        NbtList corners = new NbtList();
        this.visitedWorldCorners.forEach(corner -> corners.add(NbtString.of(corner.name())));
        nbt.put("VisitedCorners", corners);

        nbt.putBoolean("Pvp", this.pvpEnabled);
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

        if (nbt.contains("Afk")) {
            this.afk = nbt.getBoolean("Afk");
        }

        if (nbt.contains("Playtime")) {
            this.timePlayed = nbt.getLong("Playtime");
        }

        if (nbt.contains("CompletedChallenges")) {
            NbtList challenges = nbt.getList("CompletedChallenges", NbtElement.STRING_TYPE);
            challenges.forEach(challenge -> this.completedChallenges.add(challenge.asString()));
        }

        if (nbt.contains("VisitedCorners")) {
            NbtList corners = nbt.getList("VisitedCorners", NbtElement.STRING_TYPE);
            corners.forEach(challenge -> this.visitedWorldCorners.add(WorldCorner.valueOf(challenge.asString())));
        }

        if (nbt.contains("Pvp")) {
            this.pvpEnabled = nbt.getBoolean("Pvp");
        }
    }

    public void bitsVanillaCopyFromOldPlayer(@NotNull PlayerEntityMixin oldPlayer) {
        this.lastRTPTime = oldPlayer.lastRTPTime;
        this.customClient = oldPlayer.customClient;
        this.sendTPS = oldPlayer.sendTPS;
        this.afk = false;
        this.timePlayed = oldPlayer.timePlayed;
        this.completedChallenges.addAll(oldPlayer.completedChallenges);
        this.visitedWorldCorners.addAll(oldPlayer.visitedWorldCorners);
        this.pvpEnabled = oldPlayer.pvpEnabled;

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

    @Override
    public boolean hasCompletedChallenge(@NotNull Challenge challenge) {
        return this.completedChallenges.contains(challenge.getInformation().tag());
    }

    @Override
    public void markChallengeCompleted(@NotNull Challenge challenge) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!this.hasCompletedChallenge(challenge)) {
            this.completedChallenges.add(challenge.getInformation().tag());
            challenge.complete((ServerPlayerEntity) self);
        }
    }

    @Override
    public void resetCompletedChallenge(@NotNull Challenge challenge) {
        this.completedChallenges.remove(challenge.getInformation().tag());
    }

    @Override
    public boolean hasVisitedCorner(@NotNull WorldCorner corner) {
        return this.visitedWorldCorners.contains(corner);
    }

    @Override
    public void clearVisitedCorners() {
        this.visitedWorldCorners.clear();
    }

    @Override
    public void markVisitedCorner(@NotNull WorldCorner corner) {
        this.visitedWorldCorners.add(corner);
    }

    @Override
    public boolean hasPvpEnabled() {
        return this.pvpEnabled;
    }

    @Override
    public void setPvpEnabled(boolean pvp) {
        this.pvpEnabled = pvp;
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

