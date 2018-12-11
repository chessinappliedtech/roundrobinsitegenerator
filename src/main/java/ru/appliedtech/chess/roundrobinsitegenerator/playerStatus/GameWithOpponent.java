package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

public class GameWithOpponent {
    private final int index;
    private final String color;
    private final String score;
    private final String date;

    public GameWithOpponent(int index, String color, String score, String date) {
        this.index = index;
        this.color = color;
        this.score = score;
        this.date = date;
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
