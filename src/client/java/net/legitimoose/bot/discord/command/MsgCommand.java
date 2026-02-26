package net.legitimoose.bot.discord.command;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class MsgCommand implements Command {
    private static final MongoCollection<Player> coll = Scraper.getInstance().db.getCollection("players", Player.class);

    final SlashCommandInteractionEvent event;
    final String message;
    final String player;
    public static final Map<String, Long> lastSent = new HashMap<>();

    public MsgCommand(SlashCommandInteractionEvent event, String message, String player) {
        this.event = event;
        this.message = message;
        this.player = player;
    }

    @Override
    public void onCommandReceived() {
        Player playerObj = coll.find(regex("name", player, "i")).first();
        if (playerObj == null || playerObj.blocked().contains(event.getUser().getName())) {
            event.reply("Failed to send, player has blocked you or does not exist.").setEphemeral(true).queue();
            return;
        }

        String newMessage = player.replace("§", "?") + " [ᴅɪsᴄᴏʀᴅ] @" + event.getUser().getName() + ": " + message.replace("\n", "<br>").replace("§", "?");
        if (newMessage.length() >= 200) {
            event.reply("Failed to send, message and/or player name too long!").setEphemeral(true).queue();
            return;
        }

        Minecraft.getInstance()
                .player
                .connection
                .sendCommand("msg " + newMessage);

        event.reply("Sent `" + message.trim() + "` to " + player).setEphemeral(true).queue();
        lastSent.put(player, event.getUser().getIdLong());
    }
}
