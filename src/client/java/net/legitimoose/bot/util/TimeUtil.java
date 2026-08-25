package net.legitimoose.bot.util;

public class TimeUtil {

    public static final long MILLIS_PER_MINUTE = 60 * 1000;
    public static final long MILLIS_PER_HOUR = 60 * 60 * 1000;
    public static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    public static final long MILLIS_PER_WEEK = 7 * 24 * 60 * 60 * 1000;

    /**
     * Formats the time in millis into a string with the first two largest units
     * <p>
     * This does truncate values but that's not too much of a problem
     */
    public static String format(long time) {
        if (time < MILLIS_PER_MINUTE)
            return time / 1000 + "s";
        if (time < MILLIS_PER_HOUR)
            return time / MILLIS_PER_MINUTE + "m " + (time % MILLIS_PER_MINUTE) / 1000 + "s";
        if (time < MILLIS_PER_DAY)
            return time / MILLIS_PER_HOUR + "h " + (time % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE + "m";
        if (time < MILLIS_PER_WEEK)
            return time / MILLIS_PER_DAY + "d " + (time % MILLIS_PER_DAY) / MILLIS_PER_HOUR + "h";
        return time / MILLIS_PER_WEEK + "w " + (time % MILLIS_PER_WEEK) / MILLIS_PER_DAY + "d";
    }

    /**
     * Converts the time string into milliseconds
     * Accepts any number of space separated specifiers: s, h, d, w
     * @return -1 if the input is invalid
     */
    public static long parse(String time) {
        long total = -1;
        String[] segments = time.split(" +");
        if (segments.length == 0)
            return -1;

        for (String part : segments) {
            int l = part.length();
            if (l < 2)
                continue;

            try {
                int prefix = Integer.parseInt(part.substring(0, l - 1));

                total += switch (part.charAt(l - 1)) {
                    case 's' -> prefix * 1000L;
                    case 'm' -> prefix * MILLIS_PER_MINUTE;
                    case 'h' -> prefix * MILLIS_PER_HOUR;
                    case 'd' -> prefix * MILLIS_PER_DAY;
                    case 'w' -> prefix * MILLIS_PER_WEEK;
                    default -> throw new NumberFormatException();
                };

            } catch (NumberFormatException e) {
                return -1;
            }
        }

        // Returns -1 if all parts are too short
        return total == -1 ? -1 : total + 1;
    }

}
