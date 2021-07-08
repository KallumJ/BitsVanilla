package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.util.Colors;

public class EndLockCommand extends Command {
    public static boolean locked = true;

    public EndLockCommand() {
        super("lockend", new CommandInformation()
            .setDescription("Toggles the lock on the end")
            .setPublic(false)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        locked = !locked;

        TextComponent text;
        if (locked) {
            text = Component.text("The end is now locked", Colors.NEUTRAL);
        } else {
            text = Component.text("The end is now unlocked", Colors.NEUTRAL);
        }
        BitsVanilla.audience(context.getSource().getPlayer()).sendMessage(text);
        return 1;
    }

    public static boolean isEndLocked() {
        return locked;
    }
}
