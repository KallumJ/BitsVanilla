package team.bits.vanilla.fabric.mixin;

import net.minecraft.util.MetricsData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MetricsData.class)
public class MetricsMixin {

//    @Inject(
//            method = "pushSample",
//            at = @At("HEAD")
//    )
//    public void onMetricsSamplePush(long time, CallbackInfo ci) {
//        for (ServerPlayerEntity player : ServerInstance.getOnlinePlayers()) {
//            if (((ExtendedPlayerEntity) player).shouldSendTPS()) {
//                CustomClientHandler.sendMetricsSample(player, time);
//            }
//        }
//    }
}
