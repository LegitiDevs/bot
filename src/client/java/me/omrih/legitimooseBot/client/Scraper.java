package me.omrih.legitimooseBot.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;
import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;
import static me.omrih.legitimooseBot.client.LegitimooseBotClient.LOGGER;

public class Scraper {
    public static boolean invIsOpen = false;

    public static void scrapeAll() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        client.player.networkHandler.sendChatCommand("worlds");

        new Thread(() -> {
            waitSeconds(1);
            assert MinecraftClient.getInstance().interactionManager != null;
            Inventory inv = client.player.currentScreenHandler.getSlot(0).inventory;
            String title = client.currentScreen.getTitle().toString();
            int max_pages = 0;
            try {
                max_pages = Integer.parseInt(title.substring(31, title.length() - 2));
            } catch (NumberFormatException e) {
                LOGGER.warning(e.getMessage());
                client.player.sendMessage(Text.literal("Cannot start scraping: failed to parse integer amount of worlds!").formatted(Formatting.RED));
                return;
            }
            LOGGER.info("Last page is: " + max_pages);
            for (int i = 1; i <= max_pages; i++) {
                for (int j = 0; j <= 26; j++) {
                    if (client.player.currentScreenHandler.syncId == 0)
                        return; // should check if player closed the inventory not sure though
                    ItemStack itemStack = inv.getStack(j);
                    // last page & air: break, last world was already hit.
                    if (i == max_pages && itemStack.toString().substring(2).equals("minecraft:air")) break;
                    NbtCompound customData = itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                    NbtElement publicBukkitValues = customData.get("PublicBukkitValues");
                    assert publicBukkitValues != null;

                    ScrapedWorld world = new ScrapedWorld();
                    world.creation_date = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:creation_date")).asString();
                    world.enforce_whitelist = Boolean.parseBoolean(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:enforce_whitelist")).asString());
                    world.locked = Boolean.parseBoolean(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:locked")).asString());
                    world.owner_uuid = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:owner")).asString();
                    world.player_count = Integer.parseInt(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:player_count")).asString());
                    world.resource_pack_url = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:resource_pack_url")).asString();
                    world.world_uuid = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:uuid")).asString();
                    world.version = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:version")).asString();
                    world.visits = Integer.parseInt(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:visits")).asString());
                    world.votes = Integer.parseInt(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:votes")).asString());
                    world.whitelist_on_version_change = Boolean.parseBoolean(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:whitelist_on_version_change")).asString());
                    world.name = Objects.requireNonNull(itemStack.get(DataComponentTypes.CUSTOM_NAME)).getString();
                    world.description = Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).lines().getFirst().getString();
                    world.icon = Objects.requireNonNull(itemStack.toString().substring(2));

                    LOGGER.info("Scraped World " + j);
                    LOGGER.info("World data: " + world.getString());
                    try {
                        world.uploadToDB();
                    } catch (Exception e) {
                        LOGGER.info(e.getMessage());
                    }
                }
                // finally, click on next page button
                LOGGER.info("Scraped page #" + i);
                MinecraftClient.getInstance().interactionManager.clickSlot(client.player.currentScreenHandler.syncId, 32, 0, SlotActionType.PICKUP, client.player);
                waitSeconds(3); // wait three seconds to give legmos time to load
            }
            client.player.closeHandledScreen();
            client.player.sendMessage(Text.literal("Finished Scraping!").formatted(Formatting.GREEN));
        }).start();
    }

    // inner class
    public static class ScrapedWorld {
        String creation_date;
        boolean enforce_whitelist;
        boolean locked;
        String owner_uuid;
        int player_count;
        String resource_pack_url;
        String world_uuid;
        String version;
        int visits;
        int votes;
        boolean whitelist_on_version_change;
        String name;
        String description;
        String icon;

        public String getString() {
            return toJsonObject().toString();
        }

        public JsonObject toJsonObject() {
            JsonObject obj = new JsonObject();
            obj.add("creation_date", new JsonPrimitive(creation_date));
            obj.add("enforce_whitelist", new JsonPrimitive(enforce_whitelist));
            obj.add("locked", new JsonPrimitive(locked));
            obj.add("owner_uuid", new JsonPrimitive(owner_uuid));
            obj.add("player_count", new JsonPrimitive(player_count));
            obj.add("resource_pack_url", new JsonPrimitive(resource_pack_url));
            obj.add("world_uuid", new JsonPrimitive(world_uuid));
            obj.add("version", new JsonPrimitive(version));
            obj.add("visits", new JsonPrimitive(visits));
            obj.add("votes", new JsonPrimitive(votes));
            obj.add("whitelist_on_version_change", new JsonPrimitive(whitelist_on_version_change));
            obj.add("name", new JsonPrimitive(name));
            obj.add("description", new JsonPrimitive(description));
            obj.add("icon", new JsonPrimitive(icon));
            return obj;
        }

        public void uploadToDB() {
            MongoClient mongoClient = MongoClients.create(CONFIG.mongoUri());
            MongoDatabase database = mongoClient.getDatabase("legitimooseapi");
            database.createCollection("worlds");
            MongoCollection<Document> collection = database.getCollection("worlds");
            Document doc = collection.find(eq("world_uuid", this.world_uuid)).first();
            if (doc != null) {
                Bson updates = Updates.combine(
                        Updates.set("creation_date", this.creation_date),
                        Updates.set("enforce_whitelist", this.enforce_whitelist),
                        Updates.set("locked", this.locked),
                        Updates.set("owner_uuid", this.owner_uuid),
                        Updates.set("player_count", this.player_count),
                        Updates.set("resource_pack_url", this.resource_pack_url),
                        Updates.set("world_uuid", this.world_uuid),
                        Updates.set("version", this.version),
                        Updates.set("visits", this.visits),
                        Updates.set("votes", this.votes),
                        Updates.set("whitelist_on_version_change", this.whitelist_on_version_change),
                        Updates.set("name", this.name),
                        Updates.set("description", this.description),
                        Updates.set("icon", this.icon)
                );
                collection.updateOne(doc, updates, new UpdateOptions());
                LOGGER.info("Updated world");
                return;
            }
            collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("creation_date", this.creation_date)
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
                    .append("icon", this.icon)
            );
            LOGGER.info("Created World");
        }
    }

    private static NbtCompound encodeStack(ItemStack stack, DynamicOps<NbtElement> ops) {
        DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(ops, stack.getComponentChanges());
        result.ifError(e -> {

        });
        NbtElement nbtElement = result.getOrThrow();
        // cast here, as soon as this breaks, the mod will need to update anyway
        return (NbtCompound) nbtElement;
    }

    private static void waitSeconds(int time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.warning("Failed to wait " + time + " seconds:");
            LOGGER.warning(e.getMessage());
        }
    }
}
