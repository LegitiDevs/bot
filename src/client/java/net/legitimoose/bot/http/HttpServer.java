package net.legitimoose.bot.http;

import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.http.endpoint.PlayersEndpoint;

import static spark.Spark.get;

public class HttpServer {
    private static HttpServer INSTANCE;

    public void start() {
        get("/", (req, res) -> {
            res.type("application/json");
            return String.format("{\"version\":\"%s\"}",
                    FabricLoader.getInstance().getModContainer("legitimoose-bot").get().getMetadata().getVersion());
        });

        get("/players", (req, res) -> {
            res.type("application/json");
            return new PlayersEndpoint().handleRequest();
        });

        get("/players/:uuid", (req, res) -> {
            res.type("application/json");
            return new PlayersEndpoint().handleRequest(req.params("uuid"));
        });
    }

    public static HttpServer getInstance() {
        if (INSTANCE == null) INSTANCE = new HttpServer();
        return INSTANCE;
    }
}
