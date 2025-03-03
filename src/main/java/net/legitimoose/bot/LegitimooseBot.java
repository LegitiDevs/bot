package net.legitimoose.bot;

import net.fabricmc.api.ModInitializer;
import net.legitimoose.bot.config.LegitimooseBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegitimooseBot implements ModInitializer {
    public static final String MOD_ID = "legitimoose-bot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final LegitimooseBotConfig CONFIG = LegitimooseBotConfig.createAndLoad();

    @Override
    public void onInitialize() {
    }
}
