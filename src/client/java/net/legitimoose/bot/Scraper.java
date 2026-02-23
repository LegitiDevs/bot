package net.legitimoose.bot;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.serialization.JsonOps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class Scraper {
    private static Scraper INSTANCE;

    private final MongoClient mongoClient = MongoClients.create(CONFIG.getString("mongoUri"));
    private final DiscordWebhook errorWebhook = new DiscordWebhook(CONFIG.getString("errorWebhook"));

    private final Pattern jamScorePattern = Pattern.compile("^CategoryScore\\(rank=(.*), score=(.*)\\)");

    public final MongoDatabase db = mongoClient.getDatabase("legitimooseapi");

    private void waitSeconds(long time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.warn("Failed to wait {} seconds:", time);
            LOGGER.warn(e.getMessage());
        }
    }

    public void startScraping() {
        while (true) {
            try {
                scrape();
            } catch (Exception e) {
                try {
                    error("Scraper error", e);
                } catch (Exception ignored) {
                }
            }
            waitSeconds(5);
        }
    }

    private void error(String message, Exception exception) throws IOException, URISyntaxException {
        LOGGER.error(message);
        LOGGER.error(exception.getMessage());
        errorWebhook.setContent(String.format("%s\n%s", message, exception.getMessage()));
        errorWebhook.execute();
    }

    public void scrape() throws IOException, URISyntaxException {
        Minecraft client = Minecraft.getInstance();
        MongoCollection<Document> stats = db.getCollection("stats");

        // Please ignore the nulls. Only the 'input' is actually used
        CommandContext context = new CommandContextBuilder(null, null, null, 1).build("/find ");

        CompletableFuture<Suggestions> pendingParse;
        pendingParse =
                Minecraft.getInstance()
                        .player
                        .connection
                        .getSuggestionsProvider()
                        .customSuggestion(context);

        pendingParse.thenAccept((suggestions) -> stats.insertOne(new Document().append("player_count", suggestions.getList().size())));

        client.player.closeContainer();

        client.player.connection.sendCommand("worlds");

        waitSeconds(1);
        int max_pages;

        max_pages = Integer.parseInt(client.screen.getTitle().getSiblings().getFirst().getString().substring(3));

        LOGGER.info("Last page is: {}", max_pages);
        for (int i = 1; i <= max_pages; i++) {
            List<World> worlds = new ArrayList<>();

            Container inv = client.player.containerMenu.getSlot(0).container;
            for (int j = 0; j <= 26; j++) {
                if (client.player.containerMenu.containerId == 0)
                    return; // should check if player closed the inventory not sure though
                ItemStack itemStack = inv.getItem(j);
                // last page & air: break, last world was already hit.
                if (i == max_pages && itemStack.toString().substring(2).equals("minecraft:air")) break;
                CompoundTag customData;

                customData = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();
                CompoundTag publicBukkitValues = (CompoundTag) customData.get("PublicBukkitValues");

                int descriptionLines = 0;
                while (!itemStack.get(DataComponents.LORE).lines().get(descriptionLines).getString().isEmpty()) {
                    descriptionLines++;
                }

                StringBuilder description = new StringBuilder();
                for (int k = 0; k < descriptionLines; k++) {
                    description.append(itemStack.get(DataComponents.LORE).lines().get(k).getString());
                    if (k != descriptionLines - 1) description.append("\n");
                }

                StringBuilder raw_description = new StringBuilder("[");
                for (int k = 0; k < descriptionLines; k++) {
                    raw_description.append(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, itemStack.get(DataComponents.LORE).lines().get(k))
                            .result()
                            .get());
                    if (k != descriptionLines - 1) raw_description.append(",");
                }
                raw_description.append("]");

                int jam_id = getNbtInt(publicBukkitValues, "jam_id");
                int featured_instant = getNbtInt(publicBukkitValues, "featured_instant");
                boolean jam_world = getNbtBoolean(publicBukkitValues, "jam_world");
                JsonObject jam = new JsonObject();
                if (jam_id != -1) {
                    jam.addProperty("id", jam_id);
                    jam.addProperty("upgraded", !jam_world);

                    if (jam_id > 1 && getNbtField(publicBukkitValues, "jam_rating_count") != null) {
                        JsonObject scores = new JsonObject();
                        String[] categories = {"overall", "originality", "aesthetics", "fun", "theme"};
                        for (String category : categories) {
                            LOGGER.info(category);

                            JsonObject score = new JsonObject();

                            Matcher scoreMatcher = jamScorePattern.matcher(getNbtString(publicBukkitValues, "jam_score_" + category));
                            if (scoreMatcher.find()) {
                                score.addProperty("rank", Integer.parseInt(scoreMatcher.group(1)));
                                score.addProperty("score", Double.parseDouble(scoreMatcher.group(2)));
                            }

                            scores.add(category, score);
                        }

                        jam.addProperty("rating_count", getNbtInt(publicBukkitValues, "jam_rating_count"));
                        jam.add("scores", scores);
                    }
                }

                World world = new World(
                        getNbtString(publicBukkitValues, "creation_date"),
                        getNbtInt(publicBukkitValues, "creation_date_unix_seconds"),

                        getNbtBoolean(publicBukkitValues, "enforce_whitelist"),
                        getNbtBoolean(publicBukkitValues, "locked"),

                        getNbtString(publicBukkitValues, "owner"),

                        getNbtInt(publicBukkitValues, "player_count"),
                        getNbtInt(publicBukkitValues, "max_players"),
                        getNbtInt(publicBukkitValues, "max_datapack_size"),

                        getNbtString(publicBukkitValues, "resource_pack_url"),
                        getNbtString(publicBukkitValues, "uuid"),
                        getNbtString(publicBukkitValues, "version"),

                        getNbtInt(publicBukkitValues, "visits"),
                        getNbtInt(publicBukkitValues, "votes"),

                        getNbtBoolean(publicBukkitValues, "whitelist_on_version_change"),

                        itemStack.get(DataComponents.CUSTOM_NAME).getString(),
                        description.toString(),

                        ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, itemStack.get(DataComponents.CUSTOM_NAME))
                                .result()
                                .get()
                                .toString(),
                        raw_description.toString(),

                        featured_instant,

                        jam_world,
                        jam_id,

                        jam,

                        itemStack.toString().substring(2),
                        System.currentTimeMillis() / 1000L
                );
                worlds.add(world);
                LOGGER.info("Scraped World {} {}: {}", j, world.world_uuid(), world.name());
            }
            bulkUpsert(db, worlds);
            // finally, click on next page button
            LOGGER.info("Scraped page #{}", i);
            Minecraft.getInstance()
                    .gameMode
                    .handleInventoryMouseClick(
                            client.player.containerMenu.containerId, 32, 0, ClickType.PICKUP, client.player
                    );
            waitSeconds(1); // wait a sec to give legmos time to load
        }
        client.player.closeContainer();
        LOGGER.info("Finished Scraping");
    }

    private void bulkUpsert(MongoDatabase db, List<World> worlds) {
        MongoCollection<World> coll = db.getCollection("worlds", World.class);

        List<WriteModel<World>> operations = new ArrayList<>();

        coll.deleteMany(lt("last_scraped", System.currentTimeMillis() / 1000L - 86400));

        LOGGER.info("writing world");
        for (World world : worlds) {
            Bson updates =
                    Updates.combine(
                            Updates.set("creation_date", world.creation_date()),
                            Updates.set("creation_date_unix_seconds", world.creation_date_unix_seconds()),
                            Updates.set("enforce_whitelist", world.enforce_whitelist()),
                            Updates.set("locked", world.locked()),
                            Updates.set("owner_uuid", world.owner_uuid()),
                            Updates.set("player_count", world.player_count()),
                            Updates.set("max_players", world.max_players()),
                            Updates.set("max_datapack_size", world.max_datapack_size()),
                            Updates.set("resource_pack_url", world.resource_pack_url()),
                            Updates.set("version", world.version()),
                            Updates.set("visits", world.visits()),
                            Updates.set("votes", world.votes()),
                            Updates.set("whitelist_on_version_change", world.whitelist_on_version_change()),
                            Updates.set("name", world.name()),
                            Updates.set("description", world.description()),
                            Updates.set("raw_name", Document.parse(world.raw_name())),
                            Updates.set("raw_description", BsonArray.parse(world.raw_description())),
                            Updates.set("featured_instant", world.featured_instant()),
                            Updates.set("jam_world", world.jam_world()),
                            Updates.set("jam_id", world.jam_id()),
                            Updates.set("jam", Document.parse(world.jam().toString())),
                            Updates.set("icon", world.icon()),
                            Updates.set("last_scraped", world.last_scraped()));
            operations.add(new UpdateOneModel<>(
                    eq("world_uuid", world.world_uuid()),
                    updates,
                    new UpdateOptions().upsert(true)
            ));
        }

        if (!operations.isEmpty()) {
            coll.bulkWrite(operations);
        }
        LOGGER.info("Bulk wrote {} worlds", operations.size());
    }

    private String getNbtString(CompoundTag tag, String field) {
        return getNbtField(tag, field).asString().get();
    }

    private boolean getNbtBoolean(CompoundTag tag, String field) {
        return Boolean.parseBoolean(getNbtField(tag, field).asString().get());
    }

    private int getNbtInt(CompoundTag tag, String field) {
        if (!getNbtString(tag, field).isEmpty() && !getNbtString(tag,field).equals("null")) {
            return Integer.parseInt(getNbtString(tag, field));
        } else {
            return -1;
        }
    }

    private Tag getNbtField(CompoundTag tag, String field) {
        return tag.get("datapackserverpaper:" + field);
    }

    public static Scraper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Scraper();
        }
        return INSTANCE;
    }
}
