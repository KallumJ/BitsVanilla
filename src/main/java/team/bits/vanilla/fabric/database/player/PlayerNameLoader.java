package team.bits.vanilla.fabric.database.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bits.vanilla.fabric.util.Utils;

public final class PlayerNameLoader {

    private PlayerNameLoader() {
    }

    public static void loadNameData(@NotNull ServerPlayerEntity player) {
        PlayerUtils.getNameDataAsync(player).thenAccept(nameData -> {
            if (nameData.isPresent()) {
                player.setCustomName(
                        new LiteralText(nameData.get().effectiveName())
                                .styled(style -> style.withColor(nameData.get().effectiveColor()))
                );

                Utils.updatePlayerDisplayName(player);
            }
        });
    }

    public static record PlayerNameData(@NotNull String username, @Nullable String nickname, @Nullable Integer color) {

        public @NotNull String effectiveName() {
            return this.nickname == null ? this.username : this.nickname;
        }

        public int effectiveColor() {
            return this.color == null ? 0xFFFFFF : this.color;
        }
    }
}
