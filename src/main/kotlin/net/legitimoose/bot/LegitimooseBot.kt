package net.legitimoose.bot

import net.legitimoose.bot.config.LegitimooseBotConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LegitimooseBot {
    private const val MOD_ID = "legitimoose-bot"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)
    val config: LegitimooseBotConfig = LegitimooseBotConfig.createAndLoad()

    fun init() {
    }
}
