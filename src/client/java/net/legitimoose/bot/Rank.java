package net.legitimoose.bot;

public enum Rank {
    Unknown("unknown"),
    Non(""),
    AM("ᴀᴍ"),
    FM("ꜰᴍ"),
    FM2("ꜰᴍ²"),
    XM("xᴍ");

    private final String name;

    Rank(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static Rank getEnum(String rank) {
        for (Rank v : values()) {
            if (v.getName().equalsIgnoreCase(rank)) return v;
        }
        throw new IllegalArgumentException();
    }
}
