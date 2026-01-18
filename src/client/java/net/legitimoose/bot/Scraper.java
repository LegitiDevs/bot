package net.legitimoose.bot;

import com.mojang.serialization.JsonOps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import net.legitimoose.bot.discord.DiscordWebhook;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class Scraper {
    private final MongoClient mongoClient = MongoClients.create(CONFIG.getOrDefault("mongoUri", ""));
    private final DiscordWebhook errorWebhook = new DiscordWebhook(CONFIG.getOrDefault("errorWebhookUrl", ""));

    public final MongoDatabase db = mongoClient.getDatabase("legitimooseapi");

    private void waitSeconds(long time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.warn("Failed to wait {} seconds:", time);
            LOGGER.warn(e.getMessage());
        }
    }

    private void error(String message, Exception exception) throws IOException, URISyntaxException {
        LOGGER.error(message);
        LOGGER.error(exception.getMessage());
        errorWebhook.setContent(String.format("%s\n%s", message, exception.getMessage()));
        errorWebhook.execute();
    }

    public void scrape() {
        Minecraft client = Minecraft.getInstance();
        client.player.connection.sendCommand("worlds");

        waitSeconds(1);
        int max_pages = Integer.parseInt(client.screen.getTitle().getSiblings().get(0).getString().substring(3));
        LOGGER.info("Last page is: {}", max_pages);
        for (int i = 0; i <= max_pages; i++) {
            Container inv = client.player.containerMenu.getSlot(0).container;
            for (int j = 0; j <= 26; j++) {
                if (client.player.containerMenu.containerId == 0)
                    return; // should check if player closed the inventory not sure though
                ItemStack itemStack = inv.getItem(j);
                // last page & air: break, last world was already hit.
                if (i == max_pages && itemStack.toString().substring(2) == "minecraft:air") break;
                CompoundTag customData = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
                CompoundTag publicBukkitValues = (CompoundTag) customData.get("PublicBukkitValues");
                Integer jam_id;
                if (!publicBukkitValues.get("datapackserverpaper:jam_id").asString().get().isEmpty()) {
                    jam_id = Integer.parseInt(publicBukkitValues.get("datapackserverpaper:jam_id").asString().get());
                } else {
                    jam_id = null;
                }
                //int jam_id = !publicBukkitValues.get("datapackserverpaper:jam_id").asString().get().isEmpty() ? Integer.parseInt(publicBukkitValues.get("datapackserverpaper:jam_id").asString().get()) : null;
                World world =
                        new World(
                                publicBukkitValues.get("datapackserverpaper:creation_date").asString().get(),
                                Integer.parseInt(publicBukkitValues
                                        .get("datapackserverpaper:creation_date_unix_seconds")
                                        .asString()
                                        .get()),
                                Boolean.parseBoolean(publicBukkitValues
                                        .get("datapackserverpaper:enforce_whitelist")
                                        .asString()
                                        .get()),
                                Boolean.parseBoolean(publicBukkitValues
                                        .get("datapackserverpaper:locked")
                                        .asString()
                                        .get()),
                                publicBukkitValues.get("datapackserverpaper:owner").asString().get(),
                                Integer.parseInt(publicBukkitValues
                                        .get("datapackserverpaper:player_count")
                                        .asString()
                                        .get()),
                                publicBukkitValues
                                        .get("datapackserverpaper:resource_pack_url")
                                        .asString()
                                        .get(),
                                publicBukkitValues.get("datapackserverpaper:uuid").asString().get(),
                                publicBukkitValues.get("datapackserverpaper:version").asString().get(),
                                Integer.parseInt(publicBukkitValues.get("datapackserverpaper:visits").asString().get()),
                                Integer.parseInt(publicBukkitValues.get("datapackserverpaper:votes").asString().get()),
                                Boolean.parseBoolean(publicBukkitValues
                                        .get("datapackserverpaper:whitelist_on_version_change")
                                        .asString()
                                        .get()),
                                itemStack.get(DataComponents.CUSTOM_NAME).getString(),
                                itemStack.get(DataComponents.LORE).lines().get(0).getString(),
                                Boolean.parseBoolean(publicBukkitValues.get("datapackserverpaper:jam_world").asString().get()),
                                jam_id,
                                ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, itemStack.get(DataComponents.CUSTOM_NAME))
                                        .result()
                                        .get()
                                        .toString(),
                                ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, itemStack.get(DataComponents.LORE).lines().get(0))
                                        .result()
                                        .get()
                                        .toString(),
                                itemStack.toString().substring(2),
                                System.currentTimeMillis() / 1000L
                        );
                LOGGER.info("Scraped World {} {}: {}", j, world.world_uuid(), world.name());
                world.upload(db);
            }
            // finally, click on next page button
            LOGGER.info("Scraped page #{}", i);
            Minecraft.getInstance()
                    .gameMode
                    .handleInventoryMouseClick(
                            client.player.containerMenu.containerId, 32, 0, ClickType.PICKUP, client.player
                    );
            waitSeconds(3); // wait five seconds to give legmos time to load
        }
        client.player.closeContainer();
        LOGGER.info("Finished Scraping");
    }
}
