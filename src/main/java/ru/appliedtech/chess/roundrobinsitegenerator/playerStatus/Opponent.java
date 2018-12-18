package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

import ru.appliedtech.chess.Player;

import java.util.List;
import java.util.StringJoiner;

public class Opponent {
    private final Player player;
    private final List<GameWithOpponent> games;
    private final String page;

    Opponent(Player player, List<GameWithOpponent> games, String page) {
        this.player = player;
        this.games = games;
        this.page = page;
    }

    public String getFirstName() {
        return player.getFirstName();
    }

    public String getLastName() {
        return player.getLastName();
    }

    public List<GameWithOpponent> getGames() {
        return games;
    }

    public String getPage() {
        return page;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Opponent.class.getSimpleName() + "[", "]")
                .add("player=" + player)
                .add("games=" + games)
                .add("page='" + page + "'")
                .toString();
    }
}
