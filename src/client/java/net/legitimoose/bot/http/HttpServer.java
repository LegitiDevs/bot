package net.legitimoose.bot.http;

import static spark.Spark.get;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.http.endpoint.PlayerEndpoint;
import net.legitimoose.bot.http.endpoint.PlayersEndpoint;
import net.legitimoose.bot.util.McUtil;

public class HttpServer {
    private static HttpServer INSTANCE;

    public void start() {
        get("/", (req, res) -> {
            res.type("application/json");
            JsonObject response = new JsonObject();
            response.addProperty(
                    "version",
                    FabricLoader.getInstance()
                            .getModContainer("legitimoose-bot")
                            .get()
                            .getMetadata()
                            .getVersion()
                            .getFriendlyString());
            response.addProperty("online", McUtil.isOnline());

            return response;
        });

        get("/players", (req, res) -> {
            res.type("application/json");
            return new PlayersEndpoint().handleRequest();
        });

        get("/players/:uuid", (req, res) -> {
            res.type("application/json");
            return new PlayersEndpoint().handleRequest(req.params("uuid"));
        });

        get("/player", (req, res) -> {
            res.type("application/json");
            return new PlayerEndpoint().handleRequest();
        });

        get("/player/:uuid", (req, res) -> {
            res.type("application/json");
            return new PlayerEndpoint().handleRequest(req.params("uuid"));
        });
    }

    public static HttpServer getInstance() {
        if (INSTANCE == null) INSTANCE = new HttpServer();
        return INSTANCE;
    }
}
