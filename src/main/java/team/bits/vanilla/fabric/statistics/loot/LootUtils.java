package team.bits.vanilla.fabric.statistics.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.util.ServerInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class LootUtils {

    private LootUtils() {
    }

    private static final Identifier STATS_LOOT_IDENTIFIER = new Identifier("bits:statistics");

    public static @NotNull Collection<ItemStack> getLoot(@NotNull ServerPlayerEntity player) {
        Objects.requireNonNull(player);

        // get the loot table
        final LootManager lootManager = ServerInstance.get().getLootManager();
        final LootTable lootTable = lootManager.getTable(STATS_LOOT_IDENTIFIER);

        // get the player's world and position
        final ServerWorld world = player.getServerWorld();
        final Vec3d position = player.getPos();

        // create a loot context
        LootContext context = new LootContext.Builder(world)
                .random(player.getRandom()) // use the random object from the player
                .luck(player.getLuck()) // use the player's luck
                .parameter(LootContextParameters.ORIGIN, position) // origin is the player's position
                .parameter(LootContextParameters.THIS_ENTITY, player) // the player is the current entity
                .build(LootContextTypes.ADVANCEMENT_REWARD); // the type is an advancement reward

        // generate the loot
        Collection<ItemStack> loot = lootTable.generateLoot(context);

        // return the generated loot
        return Collections.unmodifiableCollection(loot);
    }
}
