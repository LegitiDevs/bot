package net.legitimoose.bot.discord.command;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;
import net.legitimoose.bot.util.DiscordUtil;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.regex;

public class MsgCommand extends ListenerAdapter {
    private static final MongoCollection<Player> coll = Scraper.getInstance().db.getCollection("players", Player.class);

    public static final Map<String, Long> lastSent = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("msg")) return;
        String message = event.getOption("message").getAsString();
        String player = event.getOption("player").getAsString();
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
                .sendCommand(McUtil.sanitizeString("msg " + newMessage));

        event.reply(DiscordUtil.sanitizeString("Sent `" + message.trim() + "` to " + player)).setEphemeral(true).queue();
        lastSent.put(player, event.getUser().getIdLong());
    }
}
