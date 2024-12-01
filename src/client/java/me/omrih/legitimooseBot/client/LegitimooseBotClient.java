package me.omrih.legitimooseBot.client;

import me.omrih.legitimooseBot.client.config.LegitimooseBotConfig;
import net.fabricmc.api.ClientModInitializer;

public class LegitimooseBotClient implements ClientModInitializer {
    public static final LegitimooseBotConfig CONFIG = LegitimooseBotConfig.createAndLoad();

    @Override
    public void onInitializeClient() {
    }
}
