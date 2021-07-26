package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.MetricsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.listeners.CustomClientHandler;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;
import team.bits.vanilla.fabric.util.ServerInstance;

@Mixin(MetricsData.class)
public class MetricsMixin {

    @Inject(
            method = "pushSample",
            at = @At("INVOKE")
    )
    public void onMetricsSamplePush(long time, CallbackInfo ci) {
        for (ServerPlayerEntity player : ServerInstance.getOnlinePlayers()) {
            if (((ExtendedPlayerEntity) player).shouldSendTPS()) {
                CustomClientHandler.sendMetricsSample(player, time);
            }
        }
    }
}
