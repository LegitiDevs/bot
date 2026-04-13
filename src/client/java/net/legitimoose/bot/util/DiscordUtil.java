package net.legitimoose.bot.util;

public class DiscordUtil {

    /**
     * Replaces @ and # symbols with lookalike Unicode symbols
     */
    public static String sanitizeString(String string) {
        return string.replace('#', '＃')
                .replace('@', '＠');
    }

}
