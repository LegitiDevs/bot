package net.legitimoose.bot

import com.mongodb.MongoClientException
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import net.legitimoose.bot.LegitimooseBot.logger
import org.bson.Document
import org.bson.types.ObjectId

data class World(
    val creation_date: String,
    val creation_date_unix_seconds: Int,
    val enforce_whitelist: Boolean,
    val locked: Boolean,
    val owner_uuid: String,
    val player_count: Int,
    val resource_pack_url: String,
    val world_uuid: String,
    val version: String,
    val visits: Int,
    val votes: Int,
    val whitelist_on_version_change: Boolean,
    val name: String,
    val description: String,
    val raw_name: String,
    val raw_description: String,
    val icon: String,
    val last_scraped: Long
) {
  fun upload(db: MongoDatabase) {
    val coll: MongoCollection<Document> = db.getCollection("worlds")
    var doc: Document?
    try {
      doc = coll.find(eq("world_uuid", this.world_uuid)).first()
    } catch (e: MongoClientException) {
      doc = null
    }
    coll.deleteMany(lt("last_scraped", System.currentTimeMillis() / 1000L - 86400))
    if (doc != null) {
      logger.info("updating world")
      val updates =
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
              Updates.set("raw_name", this.raw_name),
              Updates.set("raw_description", this.raw_description),
              Updates.set("icon", this.icon),
              Updates.set("last_scraped", this.last_scraped))
      coll.updateOne(eq("world_uuid", this.world_uuid), updates, UpdateOptions())
      logger.info("Updated world")
      return
    }
    coll.insertOne(
        Document()
            .append("_id", ObjectId())
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
            .append("raw_name", this.raw_name)
            .append("raw_description", this.raw_description)
            .append("icon", this.icon)
            .append("last_scraped", this.last_scraped))
    logger.info("Created world")
  }
}
