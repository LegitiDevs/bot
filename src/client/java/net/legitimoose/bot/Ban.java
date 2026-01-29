package net.legitimoose.bot;

public record Ban(
        long ban_time,
        String banned_player,
        String banned_uuid,
        String moderator,
        String mod_uuid,
        String reason,
        long duration,
        long expires_at
) {}