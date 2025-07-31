package net.legitimoose.bot.discord

import Command
import kotlin.system.exitProcess
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBot
import net.legitimoose.bot.LegitimooseBotClient

class StaffCommands(override val event: SlashCommandInteractionEvent, val command: String) :
    Command {
  override fun onCommandReceived() {
    if (event.guild?.id !=
        LegitimooseBot.config.getOrDefault("discordGuildId", "1311574348989071440")) {
      event.reply("Invalid guild (somehow).")
      return
    }

    val command = this.command.split(" ")[0]
    val args = this.command.split(" ").drop(1).toList()
    event.deferReply(true).queue()

    when (command) {
      "rejoin" -> {
        event.hook.sendMessage("Rejoining server...").queue()
        LegitimooseBotClient.rejoin(force = true)
      }
      "restart" -> {
        event.hook.sendMessage("Restarting bot...").complete()
        exitProcess(0)
      }
      "send-command" -> {
        if (args.isEmpty()) {
          event.hook.sendMessage("Please provide a command to send.").queue()
          return
        }
        var commandToSend = args.joinToString(" ")
        commandToSend = commandToSend.removePrefix("/")
        LegitimooseBotClient.mc.schedule {
          LegitimooseBotClient.mc.connection?.sendCommand(commandToSend)
          event.hook.sendMessage("Command sent: `/$commandToSend`").queue()
        }
      }
      "send-message" -> {
        if (args.isEmpty()) {
          event.hook.sendMessage("Please provide a message to send.").queue()
          return
        }
        val messageToSend = args.joinToString(" ")
        LegitimooseBotClient.mc.schedule {
          LegitimooseBotClient.mc.connection?.sendChat(messageToSend)
          event.hook.sendMessage("Message sent: `$messageToSend`").queue()
        }
      }
      "help" -> {
        event.hook
            .sendMessage(
                "Available commands: `rejoin`, `restart`, `send-command`, `send-message`, `help`")
            .queue()
      }
      else -> {
        event.hook
            .sendMessage("Unknown command: `$command`. Use `/help` for a list of commands.")
            .queue()
      }
    // TODO: Figure out how to login (maybe with headlessmc api???? or recreate headlessmc
    // behavior+replace the first value in .accounts file) OR make a headlessmc plugin to handle
    // this
    // A remote update command would also be possible (download mod from somewhere and restart bot)
    }
  }
}
