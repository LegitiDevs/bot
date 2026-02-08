package net.legitimoose.bot;

import net.dv8tion.jda.api.entities.Member;
import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.discord.DiscordWebhook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class EventHandler {
    private static EventHandler INSTANCE;

    public List<String> lastMessages = new ArrayList<>();
    public boolean joinLeaveMessages = true;

    private final Pattern joinPattern = Pattern.compile("^\\[\\+]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern switchPattern = Pattern.compile("^\\[→]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern leavePattern = Pattern.compile("^\\[-]\\s*(?:[^|]+\\|\\s*)?(\\S+)");

    private final Pattern chatPattern = Pattern.compile("^(?:\\[SHOUT]\\s*)?(?:[^|]+\\|\\s*)?([^:]+): (.*)");
    private final Pattern msgPattern = Pattern.compile("\\[(.*) -> me] @(.*) (.*)");

    public void onRecieveMessage(Component message, boolean overlay) {
        lastMessages.add(message.getString());

        String msg = message.getString();
        String username = "";
        String cleanMessage = msg;

        Matcher joinMatcher = joinPattern.matcher(msg);
        Matcher switchMatcher = switchPattern.matcher(msg);
        Matcher leaveMatcher = leavePattern.matcher(msg);
        Matcher chatMatcher = chatPattern.matcher(msg);
        Matcher msgMatcher = msgPattern.matcher(msg);

        DiscordWebhook webhook = new DiscordWebhook(CONFIG.getOrDefault("webhookUrl", ""));
        if (joinMatcher.find() && joinLeaveMessages) {
            username = joinMatcher.group(1);
            cleanMessage = String.format("**%s** joined the server.", username);
            webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
            webhook.setContent(cleanMessage.replace("@", ""));
            try {
                webhook.execute(0x57F287);
            } catch (IOException | URISyntaxException e) {
                LOGGER.warn(e.getMessage());
            }
            return;
        } else if (switchMatcher.find() && joinLeaveMessages) {
            username = switchMatcher.group(1);
            cleanMessage = String.format("**%s** switched servers.", username);
            webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
            webhook.setContent(cleanMessage.replace("@", ""));
            try {
                webhook.execute(0xF2F257);
            } catch (IOException | URISyntaxException e) {
                LOGGER.warn(e.getMessage());
            }
            return;
        } else if (leaveMatcher.find() && joinLeaveMessages) {
            username = leaveMatcher.group(1);
            cleanMessage = String.format("**%s** left the server.", username);
            webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
            webhook.setContent(cleanMessage.replace("@", ""));
            try {
                webhook.execute(0xF25757);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return;
        } else if (chatMatcher.find()) {
            username = chatMatcher.group(1);
            cleanMessage = chatMatcher.group(2);
            if (msg.startsWith("[SHOUT]")) {
                webhook.setUsername(String.format("[SHOUT] %s", username));
            } else {
                webhook.setUsername(username);
            }
            webhook.setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", username));
        } else if (msgMatcher.find()) {
            String username1 = msgMatcher.group(1);
            String username2 = msgMatcher.group(2);
            String msg1 = msgMatcher.group(3);
            Member member =
                    DiscordBot.jda
                            .getGuildById(CONFIG.getOrDefault("discordGuildId", "1311574348989071440"))
                            .findMembers(s -> s.getUser().getName().equals(username2))
                            .get().getFirst();
            member.getUser()
                    .openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(String.format("%s: %s", username1, msg1)))
                    .queue();
            return;
        }

        if (username.equals(Minecraft.getInstance().player.getName().getString())) return;

        if (!username.isEmpty() &&
                !cleanMessage.startsWith(CONFIG.getOrDefault("secretPrefix", "::"))
        ) {
            webhook.setContent(cleanMessage.replace("@", ""));
            try {
                webhook.execute();
            } catch (IOException | URISyntaxException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    public static EventHandler getInstance() {
        if (INSTANCE == null) INSTANCE = new EventHandler();
        return INSTANCE;
    }
}
