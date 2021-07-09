package team.bits.vanilla.fabric.util;

import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.BitsVanilla;

import java.util.List;
import java.util.Objects;

public final class ServerInstance {

    private static MinecraftDedicatedServer INSTANCE;

    private ServerInstance() {
    }

    public static void set(@NotNull MinecraftDedicatedServer server) {
        INSTANCE = Objects.requireNonNull(server);
    }

    public static @NotNull MinecraftDedicatedServer get() {
        return Objects.requireNonNull(INSTANCE);
    }

    public static void broadcast(TextComponent textComponent) {
        PlayerManager serverPlayerManager = ServerInstance.get().getPlayerManager();
        List<ServerPlayerEntity> onlinePlayers = serverPlayerManager.getPlayerList();
        BitsVanilla.adventure().audience(onlinePlayers).sendMessage(textComponent);
    }
}
