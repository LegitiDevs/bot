package net.legitimoose.bot.scraper;

import com.google.gson.JsonObject;

import java.time.Instant;

public record World(
        String creation_date,
        int creation_date_unix_seconds,
        boolean enforce_whitelist,
        boolean locked,
        String owner_uuid,
        String owner_name,
        Rank owner_rank,
        int player_count,
        int max_players,
        int max_datapack_size,
        String resource_pack_url,
        String world_uuid,
        String version,
        int visits,
        int votes,
        boolean whitelist_on_version_change,
        String name,
        String normalized_name,
        String description,
        String raw_name,
        String raw_description,
        int featured_instant,

        String jam,

        String icon,

        @Deprecated
        long last_scraped,

        Instant last_scraped_ms,

        boolean deleted
) {
}
