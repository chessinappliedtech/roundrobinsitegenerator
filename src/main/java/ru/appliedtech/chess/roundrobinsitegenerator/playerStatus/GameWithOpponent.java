package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

public class GameWithOpponent {
    private final int index;
    private final String color;
    private final String score;
    private final String date;
    private final String pgn;
    private final String lichessId;

    public GameWithOpponent(int index, String color, String score, String date, String pgn, String lichessId) {
        this.index = index;
        this.color = color;
        this.score = score;
        this.date = date;
        this.pgn = pgn;
        this.lichessId = lichessId;
    }

    public String getScore() {
        return score;
    }

    public int getIndex() {
        return index;
    }

    public String getColor() {
        return color;
    }

    public String getDate() {
        return date;
    }

    public String getPgn() {
        return pgn != null ? pgn : "";
    }

    public String getLichess() {
        return lichessId != null ? "https://lichess.org/" + lichessId : null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameWithOpponent{");
        sb.append("index=").append(index);
        sb.append(", color='").append(color).append('\'');
        sb.append(", score='").append(score).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
