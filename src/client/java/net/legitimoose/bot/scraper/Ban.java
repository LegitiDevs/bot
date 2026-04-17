package net.legitimoose.bot.scraper;

import com.mongodb.client.MongoCollection;
import net.legitimoose.bot.util.McUtil;

public record Ban(
        long ban_time, // Epoch Time of the ban (seconds)
        String banned_player,
        String banned_uuid,
        String moderator,
        String mod_uuid,
        String reason,
        long duration, // Duration is in seconds
        long expires_at // Expiration date is in Epoch Time (seconds)
) {

    private static final int PERMANENT_DURATION = -1;

    public static void writeTempBan(long banTime, String bannedPlayer, String moderator, String reason, long duration) {
        Ban ban = new Ban(
                banTime,
                bannedPlayer,
                McUtil.getUuidOrThrow(bannedPlayer),
                moderator,
                McUtil.getUuidOrThrow(moderator),
                reason,
                duration,
                banTime + duration
        );
        ban.write();
    }

    public static void writePermBan(long banTime, String bannedPlayer, String moderator, String reason) {
        Ban ban = new Ban(
                banTime,
                bannedPlayer,
                McUtil.getUuidOrThrow(bannedPlayer),
                moderator,
                McUtil.getUuidOrThrow(moderator),
                reason,
                PERMANENT_DURATION,
                PERMANENT_DURATION
        );
        ban.write();
    }

    private void write() {
        MongoCollection<Ban> bans = Scraper.getInstance().db.getCollection("bans", Ban.class);

        bans.insertOne(this);
    }
}
