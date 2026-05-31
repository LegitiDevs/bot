package net.legitimoose.bot;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;

public class Config {
    public static final Gson gson = new Gson();

    private transient File file;

    public String token;
    public String webhook;
    public String errorWebhook;
    public boolean scrape;
    public String bridgeWebhookId;
    public String mongoUri;
    public String secretPrefix;
    public String channelId;
    public String guildId;

    public static Config create(File file, InputStream defaultFile) throws IOException {
        if (!file.exists()) {
            Files.copy(defaultFile, file.toPath());
        }
        Config config = gson.fromJson(new FileReader(file), Config.class);
        config.file = file;
        return config;
    }

    public void reload() throws FileNotFoundException {
        Config temp = gson.fromJson(new FileReader(file), Config.class);

        this.token = temp.token;
        this.webhook = temp.webhook;
        this.errorWebhook = temp.errorWebhook;
        this.scrape = temp.scrape;
        this.bridgeWebhookId = temp.bridgeWebhookId;
        this.mongoUri = temp.mongoUri;
        this.secretPrefix = temp.secretPrefix;
        this.channelId = temp.channelId;
        this.guildId = temp.guildId;
    }
}
