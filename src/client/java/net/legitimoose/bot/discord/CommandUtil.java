package net.legitimoose.bot.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.util.DiscordUtil;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;

public class CommandUtil {

    public static boolean isManagerOfGuild(Member member) {
        return member.hasPermission(Permission.MANAGE_SERVER);
    }

    public static boolean isCorrectGuild(Guild guild) {
        return guild.getId().equals(CONFIG.getString("guildId"));
    }

    /**
     * Returns whether the user who caused the event is a manager of the guild
     * that this bot is being used in.
     */
    public static boolean isInstigatorManagerOfTargetGuild(GenericInteractionCreateEvent event) {
        return event.getMember() != null &&
                isManagerOfGuild(event.getMember()) &&
                isCorrectGuild(event.getGuild());
    }

    /**
     * Returns the name of the person who caused the event to occur.
     * Prioritises guild specific names over their username.
     */
    public static String getInstigatorsName(GenericInteractionCreateEvent event) {
        return event.getMember() == null ? event.getUser().getEffectiveName() : event.getMember().getEffectiveName();
    }

    public static void reply(String message, boolean showToAll, SlashCommandInteractionEvent event) {
        event.reply(message).setEphemeral(!showToAll).queue();
    }

    /**
     * Same as {@link #reply} but sanitises the String first
     */
    public static void replySanitized(String message, boolean showToAll, SlashCommandInteractionEvent event) {
        reply(DiscordUtil.sanitizeString(message), showToAll, event);
    }

}
