package net.legitimoose.bot.config;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "legitimoosebot-config", wrapperName = "LegitimooseBotConfig")
public class LegitimooseBotConfigModel {
    public String webhookUrl = "";
    public String secretPrefix = "::";
    public String mongoUri = "";
    public String discordToken = "";
    public String channelId = "";
    public int waitMinutesBetweenScrapes = 10;
    public boolean verboseLogging = false;
}
