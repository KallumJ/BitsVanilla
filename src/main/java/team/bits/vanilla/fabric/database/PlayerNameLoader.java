package team.bits.vanilla.fabric.database;

import net.minecraft.server.network.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import team.bits.vanilla.fabric.util.*;

public final class PlayerNameLoader {

    private PlayerNameLoader() {
    }

    public static void loadNameData(@NotNull ServerPlayerEntity player) {
        PlayerApiUtils.getNameDataAsync(player).thenAcceptAsync(nameData -> {
            if (nameData.isPresent()) {
                player.setCustomName(
                        MutableText.of(new LiteralTextContent(nameData.get().effectiveName()))
                                .styled(style -> style.withColor(nameData.get().effectiveColor()))
                );

                Utils.updatePlayerDisplayName(player);
            }
        });
    }

    public record PlayerNameData(@NotNull String username, @Nullable String nickname, @Nullable Integer color) {

        public @NotNull String effectiveName() {
            return this.nickname == null ? this.username : this.nickname;
        }

        public int effectiveColor() {
            return this.color == null ? 0xFFFFFF : this.color;
        }
    }
}
