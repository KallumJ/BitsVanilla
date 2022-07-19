package team.bits.vanilla.fabric.mixin;

import com.mojang.datafixers.*;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.*;
import net.minecraft.nbt.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
import org.slf4j.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(LevelProperties.class)
public class LevelPropertiesMixin implements ExtendedLevelProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelPropertiesMixin.class);

    private static Dynamic<NbtElement> tmpNbt;

    @Inject(
            method = "readProperties",
            at = @At("RETURN")
    )
    private static void onLoad(Dynamic<NbtElement> dynamic, DataFixer dataFixer, int dataVersion,
                               NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo,
                               GeneratorOptions generatorOptions, Lifecycle lifecycle,
                               CallbackInfoReturnable<LevelProperties> cir) {
        tmpNbt = dynamic;
    }

    @Override
    public void load() {
        if (tmpNbt != null) {
            NbtList nbtList = (NbtList) tmpNbt.get("BirthdayCakes").result()
                    .map(Dynamic::getValue).orElse(new NbtList());
            Set<Location> birthdayCakes = new LinkedHashSet<>();
            for (NbtElement element : nbtList) {
                birthdayCakes.add(locationFromNbt((NbtCompound) element));
            }
            BirthdayCakeUtils.setBirthdayCakes(birthdayCakes);
            tmpNbt = null;
            LOGGER.info("Loaded {} cakes", birthdayCakes.size());
        }
    }

    @Inject(
            method = "updateProperties",
            at = @At("RETURN")
    )
    public void onSave(DynamicRegistryManager registryManager, NbtCompound levelNbt,
                       NbtCompound playerNbt, CallbackInfo ci) {
        if (tmpNbt != null) {
            return;
        }

        NbtList birthdayCakes = new NbtList();
        for (Location birthdayCake : BirthdayCakeUtils.getBirthdayCakes()) {
            birthdayCakes.add(locationToNbt(birthdayCake));
        }
        levelNbt.put("BirthdayCakes", birthdayCakes);
    }

    private static NbtCompound locationToNbt(Location location) {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("X", location.x());
        nbt.putDouble("Y", location.y());
        nbt.putDouble("Z", location.z());
        nbt.putString("World", WarpApiUtils.worldKeyToName(location.world().getRegistryKey()));
        return nbt;
    }

    private static Location locationFromNbt(NbtCompound nbt) {
        double x = nbt.getDouble("X");
        double y = nbt.getDouble("Y");
        double z = nbt.getDouble("Z");
        String worldName = nbt.getString("World");
        ServerWorld world = ServerInstance.get().getWorld(WarpApiUtils.nameToWorldKey(worldName));
        return new Location(new Vec3d(x, y, z), world);
    }
}
