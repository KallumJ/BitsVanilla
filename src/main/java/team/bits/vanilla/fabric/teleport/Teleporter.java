package team.bits.vanilla.fabric.teleport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.util.Location;
import team.bits.vanilla.fabric.util.ParticleUtils;
import team.bits.vanilla.fabric.util.Scheduler;
import team.bits.vanilla.fabric.util.Utils;

import java.util.*;

public final class Teleporter implements PlayerMoveCallback, PlayerDamageCallback {

    private static final TextComponent TELEPORT_START = Component.text("Teleporting...", Colors.NEUTRAL);
    private static final TextComponent TELEPORT_CANCEL = Component.text("Teleport cancelled", Colors.NEGATIVE);
    private static final TextComponent TELEPORT_DONE = Component.text("Teleported!", Colors.POSITIVE);

    private static final int SHORT_WARMUP = 20;
    private static final int LONG_WARMUP = 60;

    private static final int TASK_INTERVAL = 5;

    private static final Collection<Teleport> TELEPORTS = new LinkedList<>();
    private static final Set<PlayerEntity> TELEPORTING = new HashSet<>();
    private static final Set<PlayerEntity> NO_MOVE = new HashSet<>();

    static {
        Scheduler.scheduleAtFixedRate(Teleporter::teleportTask, 0, TASK_INTERVAL);
    }

    public static void queueTeleport(@NotNull ServerPlayerEntity player, @NotNull Location location, boolean cancelOnMove) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(location);

        if (player.isOnGround()) {

            Location targetLocation = location.add(0, 0.5, 0);
            Location currentLocation = Location.get(player);
            World world = targetLocation.world();

            BitsVanilla.audience(player).sendMessage(TELEPORT_START);
            world.playSound(
                    null, location.x(), location.y(), location.z(),
                    SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS,
                    0.2F, 1.5F
            );

            teleportParticle(currentLocation);
            teleportParticle(targetLocation);

            int time = PlayerUtils.isVIP(player) ? SHORT_WARMUP : LONG_WARMUP;
            Teleport teleport = new Teleport(player, targetLocation, time);

            TELEPORTS.add(teleport);
            TELEPORTING.add(player);
            if (cancelOnMove) {
                NO_MOVE.add(player);
            }

        } else {
            BitsVanilla.audience(player).sendMessage(TELEPORT_CANCEL);
        }
    }

    private static void teleport(@NotNull Teleport teleport) {
        final ServerPlayerEntity player = (ServerPlayerEntity) teleport.getPlayer();
        final Location destination = teleport.getDestination();
        final ServerWorld world = destination.world();
        final Vec3d position = destination.position();

        // give the player slow falling
        player.addStatusEffect(
                new StatusEffectInstance(
                        StatusEffects.SLOW_FALLING, 10, 4,
                        false, false, false
                )
        );

        // teleport the player to the destination
        Utils.teleport(player, destination);

        // send a confirmation message
        BitsVanilla.audience(player).sendMessage(TELEPORT_DONE);

        // play sound and particle
        world.playSound(
                null, position.x, position.y, position.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,
                0.2F, 0.8F
        );
        teleportParticle(destination);

        TELEPORTING.remove(player);
        NO_MOVE.remove(player);
    }

    private static void cancelTeleport(@NotNull PlayerEntity player) {
        BitsVanilla.audience(player).sendMessage(TELEPORT_CANCEL);

        TELEPORTING.remove(player);
        NO_MOVE.remove(player);

        Optional<Teleport> optionalTeleport = TELEPORTS.stream()
                .filter(teleport -> teleport.getPlayer().equals(player))
                .findFirst();

        optionalTeleport.ifPresent(TELEPORTS::remove);
    }

    public static void teleportParticle(@NotNull Location location) {
        final World world = location.world();
        final Vec3d pos = location.position();

        ParticleUtils.spawnParticle(world, ParticleTypes.PORTAL, pos, 250, 0.5, 0.5, 0.5);
        ParticleUtils.spawnParticle(world, ParticleTypes.PORTAL, pos, 50, 0.1, 0.1, 0.1);
    }

    public static void teleportTask() {
        for (Teleport teleport : new ArrayList<>(TELEPORTS)) {
            teleport.cooldown -= TASK_INTERVAL;

            if (teleport.cooldown <= 0) {
                TELEPORTS.remove(teleport);
                teleport(teleport);
            }
        }
    }

    @Override
    public void onPlayerMove(@NotNull PlayerEntity player, @NotNull Vec3d moveVector) {
        if (TELEPORTING.contains(player)) {
            cancelTeleport(player);
        }
    }

    @Override
    public void onPlayerDamage(@NotNull PlayerEntity player, @NotNull DamageSource source, float amount) {
        if (NO_MOVE.contains(player)) {
            cancelTeleport(player);
        }
    }
}
