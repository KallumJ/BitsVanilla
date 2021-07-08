package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.util.Colors;

import java.io.File;
import java.io.IOException;

public class EndLockCommand extends Command {

    private static final File unlockEndFile = new File("unlock_end.lock");

    public EndLockCommand() {
        super("lockend", new CommandInformation()
            .setDescription("Toggles the lock on the end")
            .setPublic(false)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        // Toggles between there beign a
        TextComponent text;
        if (!unlockEndFile.exists()) {
            try {
                unlockEndFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            text = Component.text("The end is now locked", Colors.NEUTRAL);
        } else {
            unlockEndFile.delete();
            text = Component.text("The end is now unlocked", Colors.NEUTRAL);
        }
        BitsVanilla.audience(context.getSource().getPlayer()).sendMessage(text);
        return 1;
    }

    public static boolean isEndLocked() {
        return unlockEndFile.exists();
    }
}
