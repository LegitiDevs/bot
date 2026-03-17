package net.legitimoose.bot.util;

public class DiscordUtil {
    public static String sanitizeString(String orig) {
        return orig
                // replace @ and # with lookalikes
                .replace("@", "＠")
                .replace("#", "＃");
    }
}
