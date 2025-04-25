package net.legitimoose.bot.config

import io.wispforest.owo.config.annotation.Config

@Config(name = "legitimoosebot-config", wrapperName = "LegitimooseBotConfig")
class LegitimooseBotConfigModel {
    @JvmField var webhookUrl = ""

    @JvmField var errorWebhookUrl = ""

    @JvmField var secretPrefix = "::"

    @JvmField var mongoUri = ""

    @JvmField var discordToken = ""

    @JvmField var channelId = ""

    @JvmField var waitMinutesBetweenScrapes: Long = 10
}
