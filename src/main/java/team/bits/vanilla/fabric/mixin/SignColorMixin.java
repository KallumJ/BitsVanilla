package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.filter.*;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.*;

@Mixin(ServerPlayNetworkHandler.class)
public class SignColorMixin {

    // color code prefix used by the Minecraft client
    private static final char COLOR_CHAR = '\u00A7';
    // color code prefix we want to be able to use
    private static final char INPUT_CHAR = '&';
    // all possible color codes concatenated in a string
    private static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    /**
     * We redirect the {@link List#get(int)} call inside the onSignUpdate method,
     * which is used to get the updated line from the list and apply it to the sign.
     * We run the resulting line through our {@link SignColorMixin#translateColorCodes(String)}
     * method to translate the color codes before applying.
     *
     * @param list  updated sign lines (received from client)
     * @param index index in the list we want to read
     * @return translated line from the list at the given index
     */
    @Redirect(
            method = "onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;get(I)Ljava/lang/Object;"
            )
    )
    public Object getSignLine(List<FilteredMessage> list, int index) {
        // Message.permitted simply sets the raw and filtered
        // text to the same value. We don't use text filtering
        // so we can simply use this method to ignore it

        // OBF - method_45060 -> permitted
        return FilteredMessage.method_45060(translateColorCodes(list.get(index).raw()));
    }

    /**
     * Translate any color codes using the INPUT_CHAR into
     * color codes using the COLOR_CHAR.
     *
     * @param inputText the text to translate
     * @return the translated text
     */
    private static String translateColorCodes(String inputText) {
        // loop over all the characters in the text.
        // we skip the last character because
        // color codes need 2 characters.
        char[] inputTextCharacters = inputText.toCharArray();
        for (int i = 0; i < inputTextCharacters.length - 1; i++) {
            // check if the character is the INPUT_CHAR and
            // the character after that is inside of ALL_CODES.
            // if so, we know this is a valid color code.
            if (inputTextCharacters[i] == INPUT_CHAR && ALL_CODES.indexOf(inputTextCharacters[i + 1]) > -1) {
                // replace the original character with the COLOR_CHAR
                // and make the color code lowercase
                inputTextCharacters[i] = COLOR_CHAR;
                inputTextCharacters[i + 1] = Character.toLowerCase(inputTextCharacters[i + 1]);
            }
        }
        return new String(inputTextCharacters);
    }
}
