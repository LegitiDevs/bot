package net.legitimoose.bot;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import net.legitimoose.bot.util.MongoUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public record World(
        String creation_date,
        int creation_date_unix_seconds,
        boolean enforce_whitelist,
        boolean locked,
        String owner_uuid,
        int player_count,
        int max_players,
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
        boolean jam_world,
        Integer jam_id,
        String icon,
        long last_scraped
) {
    public void write(MongoDatabase db) {
        MongoCollection<World> coll = db.getCollection("worlds", World.class);

        coll.deleteMany(lt("last_scraped", System.currentTimeMillis() / 1000L - 86400));

        LOGGER.info("writing world");
        Bson updates =
                Updates.combine(
                        Updates.set("creation_date", this.creation_date),
                        Updates.set("creation_date_unix_seconds", this.creation_date_unix_seconds),
                        Updates.set("enforce_whitelist", this.enforce_whitelist),
                        Updates.set("locked", this.locked),
                        Updates.set("owner_uuid", this.owner_uuid),
                        Updates.set("player_count", this.player_count),
                        Updates.set("max_players", this.max_players),
                        Updates.set("resource_pack_url", this.resource_pack_url),
                        Updates.set("version", this.version),
                        Updates.set("visits", this.visits),
                        Updates.set("votes", this.votes),
                        Updates.set("whitelist_on_version_change", this.whitelist_on_version_change),
                        Updates.set("name", this.name),
                        Updates.set("description", this.description),
                        Updates.set("raw_name", Document.parse(this.raw_name)),
                        Updates.set("raw_description", MongoUtil.encode(new JSONArray(this.raw_description))),
                        Updates.set("jam_world", this.jam_world),
                        Updates.set("jam_id", this.jam_id),
                        Updates.set("icon", this.icon),
                        Updates.set("last_scraped", this.last_scraped));
        coll.updateOne(eq("world_uuid", this.world_uuid), updates, new UpdateOptions().upsert(true));
        LOGGER.info("Wrote world {}", this.world_uuid);
    }
}
