package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.utils.Colors;
import team.bits.vanilla.fabric.BitsVanilla;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class EndLockCommand extends Command {

    private static final File unlockEndFile = new File("unlock_end.lock");

    public EndLockCommand() {
        super("lockend", new CommandInformation()
                .setDescription("Toggles the lock on the end")
                .setPublic(false)
        );
    }

    public static boolean isEndLocked() {
        return unlockEndFile.exists();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        // Toggles between there being an unlock_end.lock file and not
        TextComponent text;
        if (!unlockEndFile.exists()) {
            try {
                Files.createFile(unlockEndFile.toPath());
                text = Component.text("The end is now locked", Colors.NEUTRAL);
            } catch (IOException e) {
                text = Component.text("Could not lock the end", Colors.NEGATIVE);
            }
        } else {
            try {
                Files.delete(unlockEndFile.toPath());
                text = Component.text("The end is now unlocked", Colors.NEUTRAL);
            } catch (IOException e) {
                text = Component.text("Could not unlock the end", Colors.NEGATIVE);
            }
        }
        BitsVanilla.audience(context.getSource().getPlayer()).sendMessage(text);
        return 1;
    }
}
