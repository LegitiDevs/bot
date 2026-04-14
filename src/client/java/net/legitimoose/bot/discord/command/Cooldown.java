package net.legitimoose.bot.discord.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown {

    private final Map<Long, Long> cooldowns;

    private final long COOLDOWN_MS;

    private static final int MAX_CAPACITY = 8;

    private Cooldown(long millis) {
        // ConcurrentHashMap so that you can delete elements whilst iterating
        cooldowns = new ConcurrentHashMap<>();
        COOLDOWN_MS = millis;
    }

    public static Cooldown ofSeconds(int seconds) {
        return new Cooldown(seconds * 1000L);
    }

    public static Cooldown ofMillis(long millis) {
        return new Cooldown(millis);
    }

    public void add(long userId) {
        garbageCollect();
        cooldowns.put(userId, System.currentTimeMillis());
    }

    /**
     * Returns the number of milliseconds until or since the users cooldown expired or 0.
     * <p>
     * If the cooldown has expired, it will reset it to COOLDOWN_MS
     */
    public long getRemainingAndSet(long userId) {
        long remaining = getRemaining(userId);
        if (remaining <= 0)
            add(userId);
        return remaining;
    }

    public long getRemaining(long userId) {
        Long lastUse = cooldowns.get(userId);
        if (lastUse == null)
            return 0L;

        long expireTime = lastUse + COOLDOWN_MS;
        return expireTime - System.currentTimeMillis();
    }

    /**
     * Probably overkill due to restarts each 24h, removes entries that are no longer
     * required when the size is greater than MAX_CAPACITY
     */
    public void garbageCollect() {
        if (cooldowns.size() < MAX_CAPACITY)
            return;

        long time = System.currentTimeMillis();

        for (Map.Entry<Long, Long> entry : cooldowns.entrySet()) {
            if ((entry.getValue() + COOLDOWN_MS) < time) {
                cooldowns.remove(entry.getKey());
            }
        }
    }

    public static boolean isOK(long timeLeft) {
        return timeLeft <= 0;
    }

    public String formatSeconds(long millis) {
        return String.valueOf((int)(float)millis / 1000);
    }

}
