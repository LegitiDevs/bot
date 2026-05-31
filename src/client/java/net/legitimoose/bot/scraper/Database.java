package net.legitimoose.bot.scraper;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Database {

    private static Database instance;

    private static final String DATABASE_NAME = "legitimooseapi";

    private static final MongoClient mongoClient =
            MongoClients.create(CONFIG.mongoUri);

    private final MongoDatabase database =
            mongoClient.getDatabase(DATABASE_NAME);

    private MongoCollection<World> worlds;
    private MongoCollection<Player> players;
    private MongoCollection<Document> stats;
    private MongoCollection<Ban> bans;

    private Database() {
        worlds = database.getCollection("worlds", World.class);
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

    public static MongoCollection<Ban> getBans() {
        return getInstance().bans;
    }

}