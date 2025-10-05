package net.legitimoose.bot.discord

import com.mojang.brigadier.context.CommandContextBuilder
import com.mongodb.kotlin.client.MongoCollection
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBot.logger
import net.legitimoose.bot.Scraper
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo
import org.bson.Document
import org.bson.types.ObjectId

class ListCommand(override val event: SlashCommandInteractionEvent, val lobby: Boolean?) : Command {
  override fun onCommandReceived() {
    if (lobby != null && lobby) {
      val playerList: MutableCollection<PlayerInfo>? =
          Minecraft.getInstance().connection?.onlinePlayers
      val players = StringBuilder()
      for (player in playerList!!) {
        players.append(player.tabListDisplayName!!.string).append('\n')
      }
      event.reply(players.toString()).queue()
    } else {
      val coll: MongoCollection<Document> = Scraper.db.getCollection("stats")

      event
          .deferReply()
          .queue() // It *does* send a packet to the mc server, so keeping this is safer...

      // Please ignore the nulls. Only the 'input' is actually used
      val context = CommandContextBuilder(null, null, null, 1).build("/find ")

      val pendingParse =
          Minecraft.getInstance()
              .player
              ?.connection
              ?.getSuggestionsProvider()
              ?.customSuggestion(context) ?: return

      pendingParse.thenRun {
        if (!pendingParse.isDone) {
          logger.warn("Pending parse is not done! (somehow??)")
          return@thenRun
        }
        val mcSuggestions = pendingParse.join().list
        val suggestions = StringBuilder()
        for (suggestion in mcSuggestions) {
          suggestions.append(suggestion.text + '\n')
        }
        event.hook
            .sendMessage(
                "There are ${mcSuggestions.size} player(s) online:\n```\n${suggestions.toString()}```")
            .queue()

        coll.insertOne(
            Document().append("_id", ObjectId()).append("player_count", mcSuggestions.size))
      }
    }
  }
}
