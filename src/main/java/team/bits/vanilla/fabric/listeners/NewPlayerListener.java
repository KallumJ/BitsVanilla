package team.bits.vanilla.fabric.listeners;

import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.server.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

public class NewPlayerListener implements PlayerConnectEvent.Listener {

    private static final Collection<ItemStack> STARTER_ITEMS = Arrays.asList(
            new ItemStack(Items.STONE_AXE, 1),
            new ItemStack(Items.RED_BED, 1),
            new ItemStack(Items.CAKE, 1)
    );

    @Override
    public void onPlayerConnect(@NotNull PlayerConnectEvent event) {
        final ServerPlayerEntity player = event.getPlayer();
        final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;

        // check if this player is new (has never played before)
        if (!ePlayer.hasPlayedBefore()) {

            // give the player all the starter items
            STARTER_ITEMS.forEach(stack -> ePlayer.giveItem(stack.copy()));

            // show a welcome title
            Style style = Style.EMPTY.withColor(Formatting.AQUA).withBold(true);
            TitleUtils.showTitle(player,
                    Text.literal("Welcome to Bits").setStyle(style), // title
                    Text.literal("Season 7").setStyle(style),        // subtitle
                    20,  // fade-in
                    200, // stay
                    20   // fade-out
            );

            // teleport the player to spawn
            WarpApiUtils.getWarpAsync("spawn").thenAcceptAsync(warp -> warp.ifPresent(spawn ->
                    // we use Utils.teleport instead of the Teleporter
                    // to bypass the teleport delay
                    TeleportUtils.teleport(player, spawn.location())
            ));
        }
    }
}
