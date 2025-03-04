package net.legitimoose.bot

import com.mojang.serialization.JsonOps
import com.mongodb.kotlin.client.MongoClient
import net.legitimoose.bot.LegitimooseBot.config
import net.legitimoose.bot.LegitimooseBot.logger
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.TextCodecs
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
            val client = MinecraftClient.getInstance()
            client.player!!.networkHandler.sendChatCommand("worlds")

            waitSeconds(1)
            val title = client.currentScreen!!.title.toString()
            logger.info(title)
            val max_pages: Int
            try {
                max_pages = title.substring(31, title.length - 2).toInt()
            } catch (e: NumberFormatException) {
                logger.error("Cannot start scraping: failed to parse integer amount of worlds!")
                logger.error(e.message)
                return
            }
            logger.info("Last page is: {}", max_pages)
            for (i in 1..max_pages) {
                val inv = client.player!!.currentScreenHandler.getSlot(0).inventory
                for (j in 0..26) {
                    if (client.player!!.currentScreenHandler.syncId == 0) return // should check if player closed the inventory not sure though
                    val itemStack = inv.getStack(j)
                    // last page & air: break, last world was already hit.
                    if (i == max_pages && itemStack.toString().substring(2) == "minecraft:air") break
                    var customData: NbtCompound
                    try {
                        customData = itemStack.get(DataComponentTypes.CUSTOM_DATA)!!.copyNbt()
                    } catch (e: NullPointerException) {
                        logger.warn(e.message)
                        continue
                    }
                    val publicBukkitValues = customData.get("PublicBukkitValues") as NbtCompound

                    val world = World(
                        creation_date = publicBukkitValues.get("datapackserverpaper:creation_date")!!.asString(),
                        creation_date_unix_seconds = publicBukkitValues.get("datapackserverpaper:creation_date_unix_seconds")!!
                            .asString().toInt(),
                        enforce_whitelist = publicBukkitValues.get("datapackserverpaper:enforce_whitelist")!!
                            .asString().toBoolean(),
                        locked = publicBukkitValues.get("datapackserverpaper:locked")!!.asString().toBoolean(),
                        owner_uuid = publicBukkitValues.get("datapackserverpaper:owner")!!.asString(),
                        player_count = publicBukkitValues.get("datapackserverpaper:player_count")!!.asString().toInt(),
                        resource_pack_url = publicBukkitValues.get("datapackserverpaper:resource_pack_url")!!
                            .asString(),
                        world_uuid = publicBukkitValues.get("datapackserverpaper:uuid")!!.asString(),
                        version = publicBukkitValues.get("datapackserverpaper:version")!!.asString(),
                        visits = publicBukkitValues.get("datapackserverpaper:visits")!!.asString().toInt(),
                        votes = publicBukkitValues.get("datapackserverpaper:votes")!!.asString().toInt(),
                        whitelist_on_version_change = publicBukkitValues.get("datapackserverpaper:whitelist_on_version_change")!!
                            .asString().toBoolean(),
                        name = itemStack.get(DataComponentTypes.CUSTOM_NAME)!!.string,
                        description = itemStack.get(DataComponentTypes.LORE)!!.lines[0].string,
                        raw_name = TextCodecs.CODEC.encodeStart(
                            JsonOps.INSTANCE,
                            itemStack.get(DataComponentTypes.CUSTOM_NAME)
                        ).result().get().toString(),
                        raw_description = TextCodecs.CODEC.encodeStart(
                            JsonOps.INSTANCE,
                            itemStack.get(DataComponentTypes.LORE)!!.lines[0]
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
                MinecraftClient.getInstance().interactionManager!!.clickSlot(
                    client.player!!.currentScreenHandler.syncId,
                    32,
                    0,
                    SlotActionType.PICKUP,
                    client.player
                )
                waitSeconds(3) // wait three seconds to give legmos time to load
            }
            client.player!!.closeHandledScreen()
            logger.info("Finished Scraping")
        }
    }
}
