package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.util.ToggleableFeatureLock;

public class EndLockCommand extends Command {

    private static final ToggleableFeatureLock END_LOCK = new ToggleableFeatureLock("end");

    public EndLockCommand() {
        super("lockend", new CommandInformation()
                .setDescription("Toggles the lock on the end")
                .setPublic(false)
        );
    }

    public static boolean isEndLocked() {
        return END_LOCK.isLocked();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        END_LOCK.toggleLock();

        Text text;
        if (!isEndLocked()) {
            text = Text.literal("The end is now unlocked").styled(style -> style.withColor(Colors.NEUTRAL));
        } else {
            text = Text.literal("The end is now locked").styled(style -> style.withColor(Colors.NEUTRAL));
        }

        context.getSource().sendFeedback(text, false);
        return 1;
    }
}
