package net.legitimoose.bot.discord.command.mute;

import net.legitimoose.bot.util.TimeUtil;

public record BotMute(String minecraft_name, String discord_id, long end_time, String reason) {

    @Override
    public String toString() {
        String prefix = "<@" + discord_id + "> | " + minecraft_name + " for " + reason;
        String time = hasExpired() ? "0s" : TimeUtil.format(end_time - System.currentTimeMillis());
        return prefix + " expires in " + time;
    }

    public boolean hasExpired() {
        return end_time < System.currentTimeMillis();
    }

}
