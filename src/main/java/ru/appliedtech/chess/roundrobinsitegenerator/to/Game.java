package ru.appliedtech.chess.roundrobinsitegenerator.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class Game {
    private final String whiteId;
    private final String blackId;
    private final String date;
    private final GameResult result;

    @JsonCreator
    public Game(@JsonProperty("whiteId") String whiteId,
                @JsonProperty("blackId") String blackId,
                @JsonProperty("date") String date,
                @JsonProperty("result") GameResult result) {
        this.whiteId = whiteId;
        this.blackId = blackId;
        this.date = date;
        this.result = result;
    }

    public GameResult getResult() {
        return result;
    }

    public String getBlackId() {
        return blackId;
    }

    public String getDate() {
        return date;
    }

    public String getWhiteId() {
        return whiteId;
    }

    public String getOpponentOf(String id) {
        if (getWhiteId().equals(id)) {
            return getBlackId();
        } else if (getBlackId().equals(id)) {
            return getWhiteId();
        }
        throw new IllegalArgumentException(id);
    }

    public Integer getScoreOf(String id) {
        if (result == GameResult.draw) {
            return 1;
        } else if (result == GameResult.unknown) {
            return null;
        } else {
            if (getWhiteId().equals(id)) {
                if (result == GameResult.white_won || result == GameResult.black_forfeit) {
                    return 2;
                } else if (result == GameResult.black_won || result == GameResult.white_forfeit) {
                    return 0;
                }
            } else if (getBlackId().equals(id)) {
                if (result == GameResult.black_won || result == GameResult.white_forfeit) {
                    return 2;
                } else if (result == GameResult.white_won || result == GameResult.black_forfeit) {
                    return 0;
                }
            }
        }
        throw new IllegalArgumentException(id);
    }

    public boolean isPlayedBy(String playerId) {
        return getWhiteId().equals(playerId) || getBlackId().equals(playerId);
    }

    public enum GameResult {
        white_won("white_won", "white", "ww", "белые победили"),
        black_won("black_won", "black", "bw", "чёрные победили", "черные победили"),
        draw("draw", "ничья"),
        forfeit("forfeit", "не состоялась", "отменена", "неявка"),
        white_forfeit("white forfeit", "неявка белых"),
        black_forfeit("black forfeit", "неявка чёрных", "неявка черных"),
        unknown("unknown", "неизвестен", "*", "");

        @JsonProperty
        private final String name;
        private final List<String> synonyms;

        GameResult(String... synonyms) {
            this.name = synonyms[0];
            this.synonyms = asList(synonyms);
        }

        @JsonCreator
        public static GameResult resolve(final String result) {
            return stream(values())
                    .filter(value -> value.synonyms.contains(result))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(result));
        }

        public String getName() {
            return name;
        }
    }
}
