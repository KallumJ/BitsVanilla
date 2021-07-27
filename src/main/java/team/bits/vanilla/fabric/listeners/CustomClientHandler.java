package team.bits.vanilla.fabric.listeners;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.misc.PlayerConnectEvent;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

public class CustomClientHandler implements PlayerConnectEvent {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Identifier CHECK_BITS_CLIENT = new Identifier("bits:is_bits_client");
    private static final Identifier CUSTOM_DRAW = new Identifier("bits:custom_draw");
    private static final Identifier TPS_DRAW = new Identifier("bits:tps_draw");

    public CustomClientHandler() {
        ServerNetworkingImpl.PLAY.registerGlobalReceiver(CHECK_BITS_CLIENT, this::onCheckBitsClient);
        ServerNetworkingImpl.PLAY.registerGlobalReceiver(TPS_DRAW, this::onTPSDraw);
    }

    // when a player connects, ask them if they are a custom Bits client
    @Override
    public void onPlayerConnect(@NotNull ServerPlayerEntity player, @NotNull ClientConnection connection) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CHECK_BITS_CLIENT, buf));
    }

    // when the client answers on the is_bits_client packet, mark them as a custom client
    private void onCheckBitsClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                   PacketByteBuf buf, PacketSender responseSender) {

        ((ExtendedPlayerEntity) player).setCustomClient(true);
        LOGGER.info(String.format("Player '%s' logged in with a custom client", PlayerUtils.getEffectiveName(player)));
    }

    // the client sends this packet to indicate if they want TPS data
    private void onTPSDraw(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                           PacketByteBuf buf, PacketSender responseSender) {

        ((ExtendedPlayerEntity) player).setSendTPS(buf.readBoolean());
    }

    // send one sample of TPS data to the player
    public static void sendMetricsSample(@NotNull ServerPlayerEntity player, long sample) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarLong(sample);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TPS_DRAW, buf));
    }

    // send a custom draw request to the player
    public static void sendCustomDraw(@NotNull ServerPlayerEntity player, int x, int y, @NotNull Text text) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeText(text);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CUSTOM_DRAW, buf));
    }
}
