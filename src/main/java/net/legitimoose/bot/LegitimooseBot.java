package net.legitimoose.bot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mountcode.libraries.yaml.InvalidConfigurationException;
import ru.mountcode.mods.fabricyamlconfiguration.yaml.FabricConfiguration;

import java.io.IOException;
import java.io.InputStream;

public class LegitimooseBot implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("legitimoose-bot");
    public static final FabricConfiguration CONFIG = new FabricConfiguration();

    @Override
    public void onInitialize() {
        InputStream defaultConfigFile = LegitimooseBot.class.getResourceAsStream("/assets/legitimoose-bot/config/legitimoosebot.yml");

        CONFIG.setDefault(defaultConfigFile);
        CONFIG.setFile("config/legitimoosebot.yml");

        try {
            CONFIG.initialize();
        } catch (IOException e) {
            LOGGER.error("Configuration file cannot be generated", e);
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Configuration file cannot be loaded", e);
        }
    }
}
