package team.bits.vanilla.fabric.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private static MinecraftServer server = null;

    public Server(MinecraftServer mcserver) {
        server = mcserver;
    }
    public ServerPlayerEntity getPlayerFromName(String name) {
        return server.getPlayerManager().getPlayer(name);
    }

    public List<ServerPlayerEntity> getOnlinePlayers() {
        return server.getPlayerManager().getPlayerList();
    }

    public List<String> getOnlinePlayersNames() {
        List<ServerPlayerEntity> onlinePlayers = getOnlinePlayers();

        ArrayList<String> onlinePlayerStrings = new ArrayList<>();

        for (ServerPlayerEntity onlinePlayer : onlinePlayers) {
            onlinePlayerStrings.add(onlinePlayer.getEntityName());
        }

        return onlinePlayerStrings;
    }
}
