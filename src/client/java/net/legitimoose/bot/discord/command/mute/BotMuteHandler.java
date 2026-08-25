package net.legitimoose.bot.discord.command.mute;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.scraper.Database;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.bson.conversions.Bson;

import java.util.HashSet;
import java.util.Set;

public class BotMuteHandler {

    private static final String MUTED_MESSAGE = "You are not able to use the bot";

    private static BotMuteHandler instance;

    /**
     * Runtime sets for faster lookup
     */
    private final Set<String> discordIds, minecraftNames;

    private BotMuteHandler() {
        discordIds = new HashSet<>();
        minecraftNames = new HashSet<>();

        // Store all mutes in the sets
        for (BotMute mute : Database.getBotMutes().find()) {
            discordIds.add(mute.discord_id());
            minecraftNames.add(mute.minecraft_name());
        }
    }

    public static BotMuteHandler getInstance() {
        return instance == null ? (instance = new BotMuteHandler()) : instance;
    }

    public void add(BotMute mute) {
        Database.getBotMutes().insertOne(mute);
        minecraftNames.add(mute.minecraft_name());
        discordIds.add(mute.discord_id());
    }

    /**
     * @return if the deletion was successful
     */
    public boolean delete(String discordId, String minecraftName) {
        minecraftNames.remove(minecraftName);
        discordIds.remove(discordId);
        Bson filter = filter(discordId, minecraftName);
        return Database.getBotMutes().deleteOne(filter).getDeletedCount() > 0;
    }

    /**
     * @return whether either the discord ID or minecraft name are marked as muted
     */
    public boolean contains(String discordId, String minecraftName) {
        return discordIds.contains(discordId) || minecraftNames.contains(minecraftName);
    }

    /**
     * Removes all expired mutes
     */
    public void refresh() {
        for (BotMute mute : Database.getBotMutes().find()) {
            if (mute.hasExpired()) {
                delete(mute.discord_id(), mute.minecraft_name());
            }
        }
    }

    private boolean isMutedIngame(String name) {
        return minecraftNames.contains(name);
    }

    private boolean isMutedDiscord(String id) {
        return discordIds.contains(id);
    }

    /**
     * @return whether the player is muted and interaction should be cancelled
     */
    public boolean shouldCancelPlayer(String player, boolean privateMessage) {
        if (isMutedIngame(player)) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (privateMessage)
                connection.sendCommand("msg " + player + " " + MUTED_MESSAGE);
            else
                connection.sendChat(MUTED_MESSAGE);
            return true;
        }
        return false;
    }

    /**
     * @return whether the user is muted and event should be cancelled
     */
    public boolean shouldCancelCommand(SlashCommandInteractionEvent event) {
        if (isMutedDiscord(event.getUser().getId())) {
            event.reply(MUTED_MESSAGE).setEphemeral(true).queue();
            return true;
        }
        return false;
    }

    private static Bson filter(String discordId, String minecraftName) {
        return Filters.and(
                Filters.eq("discord_id", discordId),
                Filters.eq("minecraft_name", minecraftName)
        );
    }

}
