package net.legitimoose.bot.scraper;

public enum Rank {
    Unknown("unknown"),
    Non(""),
    AM("ᴀᴍ"),
    FM("ꜰᴍ"),
    FM2("ꜰᴍ²"),
    XM("xᴍ"),
    MM("ᴍᴍ\uD83D\uDCB0"),
    Bot("ʙᴏᴛ"),
    Mod("ᴍᴏᴅ"),
    Admin("ᴀᴅᴍɪɴ", "xᴍ³"),
    Moose("ᴍᴏᴏꜱᴇ");

    private final String[] name;

    Rank(String... name) {
        this.name = name;
    }

    public String[] getNames() {
        return this.name;
    }

    public static Rank getEnum(String rank) {
        for (Rank v : values()) {
            for (String name : v.getNames()) {
                if (name.equalsIgnoreCase(rank)) return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
