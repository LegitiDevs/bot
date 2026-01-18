package net.legitimoose.bot;

import com.mongodb.MongoClientException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

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
        String resource_pack_url,
        String world_uuid,
        String version,
        int visits,
        int votes,
        boolean whitelist_on_version_change,
        String name,
        String description,
        boolean jam_world,
        Integer jam_id,
        String raw_name,
        String raw_description,
        String icon,
        long last_scraped
) {
    public void upload(MongoDatabase db) {
        MongoCollection<Document> coll = db.getCollection("worlds");
        Document doc;
        try {
            doc = coll.find(eq("world_uuid", this.world_uuid)).first();
        } catch (MongoClientException e) {
            doc = null;
        }
        coll.deleteMany(lt("last_scraped", System.currentTimeMillis() / 1000L - 86400));
        if (doc != null) {
            LOGGER.info("updating world");
            Bson updates =
                    Updates.combine(
                            Updates.set("enforce_whitelist", this.enforce_whitelist),
                            Updates.set("locked", this.locked),
                            Updates.set("owner_uuid", this.owner_uuid),
                            Updates.set("player_count", this.player_count),
                            Updates.set("resource_pack_url", this.resource_pack_url),
                            Updates.set("version", this.version),
                            Updates.set("visits", this.visits),
                            Updates.set("votes", this.votes),
                            Updates.set("whitelist_on_version_change", this.whitelist_on_version_change),
                            Updates.set("name", this.name),
                            Updates.set("description", this.description),
                            Updates.set("jam_world", this.jam_world),
                            Updates.set("jam_id", this.jam_id),
                            Updates.set("raw_name", this.raw_name),
                            Updates.set("raw_description", this.raw_description),
                            Updates.set("icon", this.icon),
                            Updates.set("last_scraped", this.last_scraped));
            coll.updateOne(eq("world_uuid", this.world_uuid), updates, new UpdateOptions());
            LOGGER.info("Updated world");
            return;
        }
        coll.insertOne(
                new Document()
                        .append("_id", new ObjectId())
                        .append("creation_date", this.creation_date)
                        .append("creation_date_unix_seconds", this.creation_date_unix_seconds)
                        .append("enforce_whitelist", this.enforce_whitelist)
                        .append("locked", this.locked)
                        .append("owner_uuid", this.owner_uuid)
                        .append("player_count", this.player_count)
                        .append("resource_pack_url", this.resource_pack_url)
                        .append("world_uuid", this.world_uuid)
                        .append("version", this.version)
                        .append("visits", this.visits)
                        .append("votes", this.votes)
                        .append("whitelist_on_version_change", this.whitelist_on_version_change)
                        .append("name", this.name)
                        .append("description", this.description)
                        .append("jam_world", this.jam_world)
                        .append("jam_id", this.jam_id)
                        .append("raw_name", this.raw_name)
                        .append("raw_description", this.raw_description)
                        .append("icon", this.icon)
                        .append("last_scraped", this.last_scraped));
        LOGGER.info("Created world");
    }
}