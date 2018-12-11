package ru.appliedtech.chess.roundrobinsitegenerator.ranking;

import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;

import static ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable.TournamentPlayer.scoreToString;

public class RankedPlayer {
    private final Player player;
    private int rank;
    private final int score;
    private final int gamesPlayed;
    private final String page;

    public RankedPlayer(Player player, int rank, int score, int gamesPlayed, String page) {
        this.player = player;
        this.rank = rank;
        this.score = score;
        this.gamesPlayed = gamesPlayed;
        this.page = page;
    }

    public String getScore() {
        return scoreToString(score);
    }

    public int getScoreValue() {
        return score;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getFirstname() {
        return player.getFirstname();
    }

    public String getLastname() {
        return player.getLastname();
    }

    public String getPage() {
        return page;
    }
}
