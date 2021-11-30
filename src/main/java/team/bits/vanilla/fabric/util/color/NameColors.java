package team.bits.vanilla.fabric.util.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.TextUtils;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;

public final class NameColors {

    private NameColors() {
    }

    public static final NameColors INSTANCE = new NameColors();

    private final Map<String, List<NameColor>> colors = new HashMap<>();

    public void load() {
        InputStream colorsFile = this.getClass().getClassLoader().getResourceAsStream("colours.json");

        // we use the deprecated version of the JsonParser because of
        // a gson version mismatch on fabric
        @SuppressWarnings("deprecation")
        JsonObject root = new JsonParser().parse(new InputStreamReader(Objects.requireNonNull(colorsFile))).getAsJsonObject();

        root.entrySet().forEach(entry -> {
            String color = TextUtils.fancyFormat(entry.getKey());
            List<NameColor> shades = new LinkedList<>();
            entry.getValue().getAsJsonArray().forEach(shadeEntry -> {
                JsonObject shade = shadeEntry.getAsJsonObject();
                Map.Entry<String, JsonElement> value = shade.entrySet().iterator().next();
                shades.add(new NameColor(value.getKey(), parseColor(value.getValue().getAsString())));
            });
            this.colors.put(color, shades);
        });
    }

    public @NotNull Map<String, List<NameColor>> getColours() {
        return Collections.unmodifiableMap(this.colors);
    }

    public @NotNull Optional<NameColor> getColour(@NotNull String category, @NotNull String name) {
        return this.colors.get(category).stream()
                .filter(colour -> colour.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public @NotNull Collection<NameColor> getCategory(@NotNull String category) {
        return Collections.unmodifiableCollection(this.colors.get(category));
    }

    private static @NotNull Color parseColor(@NotNull String hex) {
        int rgb;
        try {
            rgb = Integer.parseInt(hex.substring(1), 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(String.format("Illegal hex string %s", hex));
        }
        return new Color(rgb);
    }
}
