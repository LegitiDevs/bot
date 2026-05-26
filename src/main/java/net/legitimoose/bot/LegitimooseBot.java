package net.legitimoose.bot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LegitimooseBot implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("legitimoose-bot");
    public static Config CONFIG;

    @Override
    public void onInitialize() {
        InputStream defaultConfigFile = LegitimooseBot.class.getResourceAsStream("/assets/legitimoose-bot/config/legitimoosebot.yml");
        try {
            CONFIG = Config.create(new File("config/legitimoosebot.json"), defaultConfigFile);
        } catch (IOException e) {
            LOGGER.error("failed to initialize config", e);
        }
    }
}
