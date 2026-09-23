package net.legitimoose.bot.scraper;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import java.time.Instant;
import java.util.List;
import org.bson.BsonDateTime;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;

public record Player(
        String uuid,
        String name,
        Rank rank,
        List<String> blocked,
        Streak streak,
        Instant last_joined,
        Integer legiticoins) {
    // "notify" is not an allowed record field name
    public record Streak(
            Integer days, @BsonProperty("notify") Boolean notifications) {}

    public void write() {
        Bson updates = Updates.combine(
                Updates.set("uuid", this.uuid),
                Updates.set("name", this.name),
                Updates.set("rank", this.rank),
                Updates.setOnInsert("blocked", this.blocked),
                Updates.set("streak", this.streak),
                Updates.set("last_joined", new BsonDateTime(this.last_joined.toEpochMilli())),
                Updates.set("legiticoins", this.legiticoins));
        Database.getPlayers().updateOne(eq("uuid", this.uuid), updates, new UpdateOptions().upsert(true));
    }
}
