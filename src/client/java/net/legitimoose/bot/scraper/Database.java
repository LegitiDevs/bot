package net.legitimoose.bot.scraper;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.legitimoose.bot.util.JsonObjectCodec;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;

public class Database {

    private static Database instance;

    private static final String DATABASE_NAME = "legitimooseapi";

    private static final MongoClient mongoClient =
            MongoClients.create(CONFIG.mongoUri);

    private final MongoDatabase database =
            mongoClient.getDatabase(DATABASE_NAME);

    private final MongoCollection<World> worlds;
    private final MongoCollection<Document> worldStats;
    private final MongoCollection<Player> players;
    private final MongoCollection<Document> stats;
    private final MongoCollection<Ban> bans;

    private Database() {
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(new JsonObjectCodec()),
                MongoClientSettings.getDefaultCodecRegistry());

        worlds = database.getCollection("worlds", World.class).withCodecRegistry(codecRegistry);
        worldStats = database.getCollection("world_stats");
        players = database.getCollection("players", Player.class);
        stats = database.getCollection("stats");
        bans = database.getCollection("bans", Ban.class);
    }

    private static Database getInstance() {
        return instance == null ? (instance = new Database()) : instance;
    }

    public static MongoCollection<Player> getPlayers() {
        return getInstance().players;
    }

    public static MongoCollection<World> getWorlds() {
        return getInstance().worlds;
    }

    public static MongoCollection<Document> getStats() {
        return getInstance().stats;
    }

    public static MongoCollection<Document> getWorldStats() {
        return getInstance().worldStats;
    }

    public static MongoCollection<Ban> getBans() {
        return getInstance().bans;
    }

}