package team.bits.vanilla.fabric.listeners;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.teleport.TeleportUtils;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.warp.WarpUtils;
import team.bits.vanilla.fabric.event.misc.PlayerConnectEvent;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

public class NewPlayerListener implements PlayerConnectEvent {

    private static final Collection<ItemStack> STARTER_ITEMS = Arrays.asList(
            new ItemStack(Items.STONE_AXE, 1),
            new ItemStack(Items.RED_BED, 1),
            new ItemStack(Items.CAKE, 1)
    );

    @Override
    public void onPlayerConnect(@NotNull ServerPlayerEntity player, @NotNull ClientConnection connection) {
        final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        final Audience audience = BitsVanilla.audience(player);

        // check if this player is new (has never played before)
        if (!ePlayer.hasPlayedBefore()) {

            // give the player all the starter items
            STARTER_ITEMS.forEach(stack -> ePlayer.giveItem(stack.copy()));

            // show a welcome title
            Style style = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);
            audience.showTitle(Title.title(
                    Component.text("Welcome to Bits", style), // title
                    Component.text("Season 5", style),        // subtitle
                    Title.Times.of(
                            Duration.ofSeconds(1), // fade-in
                            Duration.ofSeconds(5), // stay
                            Duration.ofSeconds(1)  // fade-out
                    )
            ));

            // teleport the player to spawn
            WarpUtils.getWarp("spawn").ifPresent(spawn ->
                    // we use Utils.teleport instead of the Teleporter
                    // to bypass the teleport delay
                    TeleportUtils.teleport(player, spawn.location())
            );
        }
    }
}
