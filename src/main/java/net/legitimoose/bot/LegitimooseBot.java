package net.legitimoose.bot;

import net.fabricmc.api.ModInitializer;
import net.legitimoose.bot.config.SimpleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegitimooseBot implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("legitimoose-bot");
    public static final SimpleConfig CONFIG = SimpleConfig.of("legitimoosebot-config").request();

    @Override
    public void onInitialize() {

    }
}
