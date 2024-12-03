package me.omrih.legitimooseBot.client.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "legitimoose-bot")
@Config(name = "legitimoosebot-config", wrapperName = "LegitimooseBotConfig")
public class LegitimooseBotConfigModel {
    public String webhookUrl = "";
    public String mongoUri = "";
}
