package team.bits.vanilla.fabric.chat;

import net.minecraft.text.*;
import org.jetbrains.annotations.*;

public final class FormatUtils {

    private FormatUtils() {
    }

    /**
     * Simple function for translating a {@link FormattedTextParser.FormatType} from the
     * {@link FormattedTextParser} into a Minecraft {@link Style} object.
     * Does not apply any colors to the style.
     */
    public static @NotNull Style formatTypeToStyle(@NotNull FormattedTextParser.FormatType formatType) {
        return switch (formatType) {
            case ITALIC -> Style.EMPTY.withItalic(true);
            case BOLD -> Style.EMPTY.withBold(true);
            case UNDERLINED -> Style.EMPTY.withUnderline(true);
            case STRIKETHROUGH -> Style.EMPTY.withStrikethrough(true);
            default -> Style.EMPTY;
        };
    }
}
