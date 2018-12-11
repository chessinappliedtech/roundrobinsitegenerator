package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;

import java.util.List;

public class Opponent {
    private final Player player;
    private final List<GameWithOpponent> games;
    private final String page;

    Opponent(Player player, List<GameWithOpponent> games, String page) {
        this.player = player;
        this.games = games;
        this.page = page;
    }

    public String getFirstname() {
        return player.getFirstname();
    }

    public String getLastname() {
        return player.getLastname();
    }

    public List<GameWithOpponent> getGames() {
        return games;
    }

    public String getPage() {
        return page;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Opponent{");
        sb.append("player=").append(player);
        sb.append(", games=").append(games);
        sb.append('}');
        return sb.toString();
    }
}
