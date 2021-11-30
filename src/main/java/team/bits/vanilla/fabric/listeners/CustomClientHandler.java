package team.bits.vanilla.fabric.listeners;

public class CustomClientHandler /*implements PlayerConnectEvent.Listener*/ {

//    private static final Logger LOGGER = LogManager.getLogger();
//
//    private static final Identifier CHECK_BITS_CLIENT = new Identifier("bits:is_bits_client");
//    private static final Identifier CUSTOM_DRAW = new Identifier("bits:custom_draw");
//    private static final Identifier TPS_DRAW = new Identifier("bits:tps_draw");
//
//    public CustomClientHandler() {
//        ServerNetworkingImpl.PLAY.registerGlobalReceiver(CHECK_BITS_CLIENT, this::onCheckBitsClient);
//        ServerNetworkingImpl.PLAY.registerGlobalReceiver(TPS_DRAW, this::onTPSDraw);
//    }
//
//    // when a player connects, ask them if they are a custom Bits client
//    @Override
//    public void onPlayerConnect(@NotNull PlayerConnectEvent event) {
//        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//        event.getPlayer().networkHandler.sendPacket(new CustomPayloadS2CPacket(CHECK_BITS_CLIENT, buf));
//    }
//
//    // when the client answers on the is_bits_client packet, mark them as a custom client
//    private void onCheckBitsClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
//                                   PacketByteBuf buf, PacketSender responseSender) {
//
//        ((ExtendedPlayerEntity) player).setCustomClient(true);
//        LOGGER.info(String.format("Player '%s' logged in with a custom client", PlayerUtils.getEffectiveName(player)));
//    }
//
//    // the client sends this packet to indicate if they want TPS data
//    private void onTPSDraw(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
//                           PacketByteBuf buf, PacketSender responseSender) {
//
//        ((ExtendedPlayerEntity) player).setSendTPS(buf.readBoolean());
//    }
//
//    // send one sample of TPS data to the player
//    public static void sendMetricsSample(@NotNull ServerPlayerEntity player, long sample) {
//        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//        buf.writeVarLong(sample);
//        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TPS_DRAW, buf));
//    }
//
//    // send a custom draw request to the player
//    public static void sendCustomDraw(@NotNull ServerPlayerEntity player, int x, int y, @NotNull Text text) {
//        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//        buf.writeVarInt(x);
//        buf.writeVarInt(y);
//        buf.writeText(text);
//        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CUSTOM_DRAW, buf));
//    }
}
