package ru.appliedtech.chess.roundrobinsitegenerator.tournamentView;

import ru.appliedtech.chess.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

import static java.math.BigDecimal.*;
import static java.util.stream.Collectors.toList;

public class TournamentPlayer {
    private final Player player;
    private Integer rank;
    private final BigDecimal score;
    private final int gamesPlayed;
    private final List<BigDecimal> scores;
    private final String page;
    private final int wins;
    private final BigDecimal neustadtl;
    private final BigDecimal koya;

    public TournamentPlayer(Player player, int rank, BigDecimal score, int gamesPlayed, List<BigDecimal> scores,
                            String page, int wins, BigDecimal neustadtl, BigDecimal koya) {
        this.player = player;
        this.rank = rank;
        this.score = score;
        this.gamesPlayed = gamesPlayed;
        this.scores = scores;
        this.page = page;
        this.wins = wins;
        this.neustadtl = neustadtl;
        this.koya = koya;
    }

    Player getPlayer() {
        return player;
    }

    public BigDecimal getScoreValue() {
        return score;
    }

    public String getScore() {
        return scoreToString(score);
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public static String scoreToString(BigDecimal value) {
        String result;
        BigDecimal wholePart = new BigDecimal(value.intValue());
        String wholePartString = wholePart.toBigInteger().toString();
        if (isWhole(value)) {
            result = wholePartString;
        } else if (hasHalf(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#189;";
        } else if (hasFourth(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#188;";
        } else if (hasThreeQuarters(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#190;";
        } else {
            result = value.toString();
        }
        return result;
    }

    public int getWins() {
        return wins;
    }

    public String getNeustadtl() {
        return scoreToString(neustadtl);
    }

    public String getKoya() {
        return scoreToString(koya);
    }

    private static boolean isWhole(BigDecimal value) {
        return value.remainder(ONE).compareTo(ZERO) == 0;
    }

    private static boolean hasHalf(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.5)) == 0;
    }

    private static boolean hasFourth(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.25)) == 0;
    }

    private static boolean hasThreeQuarters(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.75)) == 0;
    }

    public String getRank() {
        return rank != null ? Integer.toString(rank) : "-";
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

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getId() {
        return player.getId();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TournamentPlayer.class.getSimpleName() + "[", "]")
                .add("player=" + player)
                .add("rank=" + rank)
                .add("score=" + score)
                .add("gamesPlayed=" + gamesPlayed)
                .add("scores=" + scores)
                .add("page='" + page + "'")
                .toString();
    }
}
