package team.bits.vanilla.fabric.teleport;

import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.particle.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.interaction.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;

import java.util.*;

public final class Teleporter implements PlayerMoveEvent.Listener, PlayerDamageEvent.Listener {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Text TELEPORT_START = Text.literal("Teleporting...");
    private static final Text TELEPORT_CANCEL = Text.literal("Teleport cancelled");
    private static final Text TELEPORT_DONE = Text.literal("Teleported!");

    private static final int SHORT_WARMUP = 20;
    private static final int LONG_WARMUP = 60;

    public static final int TASK_INTERVAL = 5;

    private static final Collection<Teleport> TELEPORTS = new LinkedList<>();
    private static final Set<PlayerEntity> TELEPORTING = new HashSet<>();
    private static final Set<PlayerEntity> NO_MOVE = new HashSet<>();

    public static void queueTeleport(@NotNull ServerPlayerEntity player,
                                     @NotNull Location location, boolean cancelOnMove) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(location);

        if (player.isOnGround()) {

            Location destination = location.add(0, 0.5, 0);
            Location origin = Location.get(player);
            World destinationWorld = destination.world();

            player.sendMessage(TELEPORT_START, MessageTypes.NEUTRAL);
            destinationWorld.playSound(
                    null, location.x(), location.y(), location.z(),
                    SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS,
                    0.2F, 1.5F
            );

            teleportParticle(origin);
            teleportParticle(destination);

            int warmupTime = PlayerApiUtils.isVIP(player) ? SHORT_WARMUP : LONG_WARMUP;
            Teleport teleport = new Teleport(player, destination, warmupTime);

            TELEPORTS.add(teleport);
            TELEPORTING.add(player);
            if (cancelOnMove) {
                NO_MOVE.add(player);
            }

        } else {
            player.sendMessage(TELEPORT_CANCEL, MessageTypes.NEGATIVE);
        }
    }

    private static void teleport(@NotNull Teleport teleport) {
        final ServerPlayerEntity player = (ServerPlayerEntity) teleport.getPlayer();
        final Location destination = teleport.getDestination();
        final ServerWorld destinationWorld = destination.world();
        final Vec3d destinationPos = destination.position();

        player.addStatusEffect(
                new StatusEffectInstance(
                        StatusEffects.SLOW_FALLING, 10, 4,
                        false, false, false
                )
        );

        TeleportUtils.teleport(player, destination);

        player.sendMessage(TELEPORT_DONE, MessageTypes.POSITIVE);

        destinationWorld.playSound(
                null, destinationPos.x, destinationPos.y, destinationPos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,
                0.2F, 0.8F
        );
        teleportParticle(destination);

        LOGGER.info("Teleported player '{}' to {}", player.getDisplayName(), destination);

        TELEPORTING.remove(player);
        NO_MOVE.remove(player);
    }

    private static void cancelTeleport(@NotNull ServerPlayerEntity player) {
        player.sendMessage(TELEPORT_CANCEL, MessageTypes.NEGATIVE);

        TELEPORTING.remove(player);
        NO_MOVE.remove(player);

        Optional<Teleport> activeTeleport = TELEPORTS.stream()
                .filter(teleport -> teleport.getPlayer().equals(player))
                .findFirst();

        activeTeleport.ifPresent(TELEPORTS::remove);
    }

    public static void teleportParticle(@NotNull Location location) {
        final World world = location.world();
        final Vec3d pos = location.position();

        ParticleUtils.spawnParticle(world, ParticleTypes.PORTAL, pos,
                250, 0.5, 0.5, 0.5);
        ParticleUtils.spawnParticle(world, ParticleTypes.PORTAL, pos,
                50, 0.1, 0.1, 0.1);
    }

    public static void teleportTask() {
        for (Teleport teleport : new ArrayList<>(TELEPORTS)) {
            teleport.tick(TASK_INTERVAL);

            if (teleport.getPlayer().isSpectator()) {
                cancelTeleport((ServerPlayerEntity) teleport.getPlayer());
            }

            if (teleport.getRemainingWarmup() <= 0) {
                teleport(teleport);
                TELEPORTS.remove(teleport);
            }
        }
    }

    @Override
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        final PlayerEntity player = event.getPlayer();
        if (TELEPORTING.contains(player)) {
            cancelTeleport((ServerPlayerEntity) player);
        }
    }

    @Override
    public void onPlayerDamage(@NotNull PlayerDamageEvent event) {
        final PlayerEntity player = event.getPlayer();
        if (NO_MOVE.contains(player)) {
            cancelTeleport((ServerPlayerEntity) player);
        }
    }
}
