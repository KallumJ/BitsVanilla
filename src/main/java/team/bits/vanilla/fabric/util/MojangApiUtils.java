package team.bits.vanilla.fabric.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangApiUtils {

    public static boolean checkUsernameIsValid(String username) {

        try {
            URL url = new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s", username));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();

            // If response is not empty, mojang found a player, return true, else, return false;
            return !response.toString().equals("");
        } catch (IOException ex) {
            return false;
        }
    }
}
