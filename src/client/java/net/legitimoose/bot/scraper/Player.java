package net.legitimoose.bot.scraper;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public record Player(
        String uuid,
        String name,
        Rank rank,
        List<String> blocked
) {
    public void write() {
        MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);

        Bson updates =
                Updates.combine(
                        Updates.set("uuid", this.uuid),
                        Updates.set("name", this.name),
                        Updates.set("rank", this.rank),
                        Updates.set("blocked", this.blocked));
                players.updateOne(eq("uuid", this.uuid), updates, new UpdateOptions().upsert(true));
    }
}
