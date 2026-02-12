package net.legitimoose.bot;

import com.mongodb.client.MongoCollection;

public record Ban(
        long ban_time,
        String banned_player,
        String banned_uuid,
        String moderator,
        String mod_uuid,
        String reason,
        long duration,
        long expires_at
) {
    public void write() {
        MongoCollection<Ban> bans = Scraper.getInstance().db.getCollection("bans", Ban.class);

        bans.insertOne(
                new Ban(
                        ban_time,
                        banned_player,
                        banned_uuid,
                        moderator,
                        mod_uuid,
                        reason,
                        duration,
                        expires_at
                )
        );
    }
}
