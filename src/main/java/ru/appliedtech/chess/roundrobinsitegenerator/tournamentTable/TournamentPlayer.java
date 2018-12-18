package ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable;

import ru.appliedtech.chess.Player;

import java.text.MessageFormat;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TournamentPlayer {
    private final Player player;
    private int rank;
    private final int score;
    private final int gamesPlayed;
    private final List<Integer> scores;
    private final String page;

    public TournamentPlayer(Player player, int rank, int score, int gamesPlayed, List<Integer> scores, String page) {
        this.player = player;
        this.rank = rank;
        this.score = score;
        this.gamesPlayed = gamesPlayed;
        this.scores = scores;
        this.page = page;
    }

    public int getScoreValue() {
        return score;
    }

    public String getScore() {
        return scoreToString(score);
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public static String scoreToString(int value) {
        String result;
        if (value % 2 == 1) {
            int integerPart = value - 1;
            result = (integerPart != 0 ? Integer.toString(integerPart / 2) : "") + "&#189;";
        }
        else {
            result = Integer.toString(value / 2);
        }
        return result;
    }

    public int getRank() {
        return rank;
    }

    public List<String> getScores() {
        return scores.stream().map(TournamentPlayer::scoreToString).collect(toList());
    }

    public String getFirstName() {
        return player.getFirstName();
    }

    public String getLastName() {
        return player.getLastName();
    }

    public String getPage() {
        return page;
    }

    @Override
    public String toString() {
        return MessageFormat.format("TournamentPlayer'{'player={0}, rank={1}, score={2}, scores={3}'}'", player, rank, score, scores);
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getId() {
        return player.getId();
    }
}
