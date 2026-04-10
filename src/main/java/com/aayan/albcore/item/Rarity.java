package com.aayan.albcore.item;

public enum Rarity {

    COMMON("<white>✦ COMMON"),
    UNCOMMON("<green>✦ UNCOMMON"),
    RARE("<aqua>✦ RARE"),
    EPIC("<light_purple>✦ EPIC"),
    LEGENDARY("<gold>✦ LEGENDARY"),
    MYTHIC("<gradient:#ff00cc:#aa00ff>✦ MYTHIC</gradient>");

    private final String tag;

    Rarity(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}