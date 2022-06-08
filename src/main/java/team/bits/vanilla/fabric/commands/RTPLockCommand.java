package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.util.ToggleableFeatureLock;

public class RTPLockCommand extends Command {
    private static final ToggleableFeatureLock RTP_LOCK = new ToggleableFeatureLock("rtp");

    public RTPLockCommand() {
        super("lockrtp", new CommandInformation()
                .setDescription("Toggles RTP Prevention")
                .setPublic(false)
        );
    }

    public static boolean isRTPUnlocked() {
        return !RTP_LOCK.isLocked();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RTP_LOCK.toggleLock();

        Text text;
        if (isRTPUnlocked()) {
            text = Text.literal("The rtp command is now unlocked").styled(style -> style.withColor(Colors.NEUTRAL));
        } else {
            text = Text.literal("The rtp command is now locked").styled(style -> style.withColor(Colors.NEUTRAL));
        }

        context.getSource().sendFeedback(text, false);
        return 1;
    }
}
