package team.bits.vanilla.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import team.bits.vanilla.fabric.commands.Commands;
import team.bits.vanilla.fabric.commands.DiscordCommand;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.listeners.SleepListener;

public class BitsVanilla implements ModInitializer {

    private static FabricServerAudiences adventure;

    public static FabricServerAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return adventure;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> adventure = FabricServerAudiences.of(server));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> adventure = null);

        Commands.registerCommands();

        SleepListener sleepListener = new SleepListener();
        PlayerSleepCallback.EVENT.register(sleepListener);
        PlayerWakeUpCallback.EVENT.register(sleepListener);

        PlayerMoveCallback.EVENT.register((player, moveVector) -> System.out.println(moveVector));
    }
}
