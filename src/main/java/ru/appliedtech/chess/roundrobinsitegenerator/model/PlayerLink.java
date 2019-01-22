package ru.appliedtech.chess.roundrobinsitegenerator.model;

public class PlayerLink {
    private final String playerId;
    private final String link;

    public PlayerLink(String playerId, String link) {
        this.playerId = playerId;
        this.link = link;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getLink() {
        return link;
    }
}
