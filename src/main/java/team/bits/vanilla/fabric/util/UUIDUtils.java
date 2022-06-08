package team.bits.vanilla.fabric.util;

import java.util.ArrayList;
import java.util.List;

/*
    Code sourced from here, distributed under the MIT license
    https://github.com/ricksouth/serilum-mc-mods
 */
public class UUIDUtils {
    public static List<Integer> uuidToIntArray(String oldid) {
        String oldidfull = oldid.replace("-", "");

        return getIntegerParts(oldidfull, 8);
    }

    private static List<Integer> getIntegerParts(String string, int partitionSize) {
        List<Integer> parts = new ArrayList<>();
        int len = string.length();
        for (int i=0; i<len; i+=partitionSize) {
            parts.add(partToDecimalValue(string.substring(i, Math.min(len, i + partitionSize))));
        }
        return parts;
    }

    private static int partToDecimalValue(String hex) {
        return Long.valueOf(hex, 16).intValue();
    }
}
