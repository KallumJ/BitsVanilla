package team.bits.vanilla.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.command.CommandOutput;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.commands.Commands;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.listeners.SleepListener;

public class BitsVanilla implements ModInitializer {

    private static FabricServerAudiences adventure;

    public static FabricServerAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return adventure;
    }

    public static @NotNull Audience audience(@NotNull CommandOutput source) {
        return adventure().audience(source);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> adventure = FabricServerAudiences.of(server));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> adventure = null);

        Commands.registerCommands();

        SleepListener sleepListener = new SleepListener();
        PlayerSleepCallback.EVENT.register(sleepListener);
        PlayerWakeUpCallback.EVENT.register(sleepListener);

        Teleporter teleporter = new Teleporter();
        PlayerMoveCallback.EVENT.register(teleporter);
        PlayerDamageCallback.EVENT.register(teleporter);
    }
}
