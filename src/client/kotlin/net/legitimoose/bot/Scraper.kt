package net.legitimoose.bot

import com.mojang.serialization.JsonOps
import com.mongodb.kotlin.client.MongoClient
import net.legitimoose.bot.LegitimooseBot.config
import net.legitimoose.bot.LegitimooseBot.logger
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.inventory.ClickType
import net.minecraft.network.chat.ComponentSerialization
import java.util.concurrent.TimeUnit

class Scraper {
    companion object {
        private val mongoClient = MongoClient.create(config.mongoUri)
        private val db = mongoClient.getDatabase("legitimooseapi")

        private fun waitSeconds(time: Long) {
            try {
                TimeUnit.SECONDS.sleep(time)
            } catch (e: InterruptedException) {
                logger.warn("Failed to wait {} seconds:", time)
                logger.warn(e.message)
            }
        }

        fun scrape() {
            val client = Minecraft.getInstance()
            client.player!!.connection.sendCommand("worlds")

            waitSeconds(1)
            val max_pages: Int
            try {
                max_pages = client.screen!!.title.siblings[0].string.substring(3).toInt()
            } catch (e: NumberFormatException) {
                logger.error("Cannot start scraping: failed to parse integer amount of worlds!")
                logger.error(e.message)
                return
            }
            logger.info("Last page is: {}", max_pages)
            for (i in 1..max_pages) {
                val inv = client.player!!.containerMenu.getSlot(0).container
                for (j in 0..26) {
                    if (client.player!!.containerMenu.containerId == 0) return // should check if player closed the inventory not sure though
                    val itemStack = inv.getItem(j)
                    // last page & air: break, last world was already hit.
                    if (i == max_pages && itemStack.toString().substring(2) == "minecraft:air") break
                    var customData: CompoundTag
                    try {
                        customData = itemStack.get(DataComponents.CUSTOM_DATA)!!.copyTag()
                    } catch (e: NullPointerException) {
                        logger.warn(e.message)
                        continue
                    }
                    val publicBukkitValues = customData.get("PublicBukkitValues") as CompoundTag

                    val world = World(
                        creation_date = publicBukkitValues.get("datapackserverpaper:creation_date")!!.asString,
                        creation_date_unix_seconds = publicBukkitValues.get("datapackserverpaper:creation_date_unix_seconds")!!
                            .asString.toInt(),
                        enforce_whitelist = publicBukkitValues.get("datapackserverpaper:enforce_whitelist")!!
                            .asString.toBoolean(),
                        locked = publicBukkitValues.get("datapackserverpaper:locked")!!.asString.toBoolean(),
                        owner_uuid = publicBukkitValues.get("datapackserverpaper:owner")!!.asString,
                        player_count = publicBukkitValues.get("datapackserverpaper:player_count")!!.asString.toInt(),
                        resource_pack_url = publicBukkitValues.get("datapackserverpaper:resource_pack_url")!!
                            .asString,
                        world_uuid = publicBukkitValues.get("datapackserverpaper:uuid")!!.asString,
                        version = publicBukkitValues.get("datapackserverpaper:version")!!.asString,
                        visits = publicBukkitValues.get("datapackserverpaper:visits")!!.asString.toInt(),
                        votes = publicBukkitValues.get("datapackserverpaper:votes")!!.asString.toInt(),
                        whitelist_on_version_change = publicBukkitValues.get("datapackserverpaper:whitelist_on_version_change")!!
                            .asString.toBoolean(),
                        name = itemStack.get(DataComponents.CUSTOM_NAME)!!.string,
                        description = itemStack.get(DataComponents.LORE)!!.lines[0].string,
                        raw_name = ComponentSerialization.CODEC.encodeStart(
                            JsonOps.INSTANCE,
                            itemStack.get(DataComponents.CUSTOM_NAME)
                        ).result().get().toString(),
                        raw_description = ComponentSerialization.CODEC.encodeStart(
                            JsonOps.INSTANCE,
                            itemStack.get(DataComponents.LORE)!!.lines[0]
                        ).result().get().toString(),
                        icon = itemStack.toString().substring(2),
                        last_scraped = System.currentTimeMillis() / 1000L
                    )
                    logger.info("Scraped World $j")
                    try {
                        world.upload(db)
                    } catch (e: Exception) {
                        logger.info(e.message)
                    }
                }
                // finally, click on next page button
                logger.info("Scraped page #$i")
                Minecraft.getInstance().gameMode!!.handleInventoryMouseClick(
                    client.player!!.containerMenu.containerId,
                    32,
                    0,
                    ClickType.PICKUP,
                    client.player!!
                )
                waitSeconds(3) // wait three seconds to give legmos time to load
            }
            client.player!!.closeContainer()
            logger.info("Finished Scraping")
        }
    }
}
