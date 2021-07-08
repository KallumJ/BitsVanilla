package team.bits.vanilla.fabric.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The minecraft client crashes if you send it statistics it doesn't
 * know. This mixin removes any statistics in the 'bits' namespace
 * from statistics packets before they're sent to the client.
 */
@Mixin(StatisticsS2CPacket.class)
public class StatisticsPacketMixin {

    @Shadow
    @Final
    private Object2IntMap<Stat<?>> stats;

    @Inject(
            method = "<init>(Lit/unimi/dsi/fastutil/objects/Object2IntMap;)V",
            at = @At("TAIL")
    )
    public void createStatsPacket(Object2IntMap<Stat<?>> stats, CallbackInfo ci) {
        // loop over a copy of the stats map to prevent concurrent modification
        for (Stat<?> stat : new Object2IntArrayMap<>(this.stats).keySet()) {

            if (stat.getType().equals(Stats.CUSTOM) && // check if the stat is a custom statistic
                    stat.getValue() instanceof Identifier statId && // if the value is an identifier
                    statId.getNamespace().equals("bits")) { // and if the namespace is bits

                // remove the statistic from the map so it doesn't get sent to the client
                this.stats.removeInt(stat);
            }
        }
    }
}
