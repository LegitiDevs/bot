package net.legitimoose.bot.scraper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

public record World(
        String creation_date,
        int creation_date_unix_seconds,
        boolean enforce_whitelist,
        boolean locked,
        String owner_uuid,
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
        String description,
        String raw_name,
        String raw_description,
        int featured_instant,

        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0") // API v4
        boolean jam_world,

        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0") // API v4
        int jam_id,

        JsonObject jam,

        String icon,

        @Deprecated
        long last_scraped,

        long last_scraped_ms
) {
}
