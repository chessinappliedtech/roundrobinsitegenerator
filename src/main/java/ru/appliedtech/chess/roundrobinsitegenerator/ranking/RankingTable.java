package ru.appliedtech.chess.roundrobinsitegenerator.ranking;

import freemarker.template.TemplateException;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Game;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class RankingTable {
    private final Map<Player, String> playerPages;
    private List<RankedPlayer> rankedPlayers;

    public RankingTable(Map<Player, String> playerPages) {
        this.playerPages = playerPages;
        this.rankedPlayers = new ArrayList<>();
    }

    public void calculate(List<Player> registeredPlayers, List<Game> games) {
        rankedPlayers = registeredPlayers.stream()
                .map(toRankedPlayerWithUnknownRank(games))
                .sorted(comparingInt(RankedPlayer::getScoreValue)
                        .reversed()
                        .thenComparingInt(RankedPlayer::getGamesPlayed)
                        .thenComparing(RankedPlayer::getLastname)
                        .thenComparing(RankedPlayer::getFirstname))
                .collect(toList());
        assignRanks(rankedPlayers);
    }

    private static void assignRanks(List<RankedPlayer> rankedPlayers) {
        int rank = 1;
        int previousScore = -1;
        for (RankedPlayer rankedPlayer : rankedPlayers) {
            if (previousScore != -1) {
                if (previousScore != rankedPlayer.getScoreValue()) {
                    rank += 1;
                }
                previousScore = rankedPlayer.getScoreValue();
            } else {
                previousScore = rankedPlayer.getScoreValue();
            }
            rankedPlayer.setRank(rank);
        }
    }

    private Function<Player, RankedPlayer> toRankedPlayerWithUnknownRank(List<Game> games) {
        return player -> {
            int score = games.stream()
                    .filter(game -> game.isPlayedBy(player.getId()))
                    .mapToInt(game -> game.getScoreOf(player.getId()))
                    .sum();
            int gamesPlayed = (int) games.stream()
                    .filter(game -> game.isPlayedBy(player.getId()))
                    .count();
            return new RankedPlayer(player, 0, score, gamesPlayed, playerPages.get(player));
        };
    }

    public void render(Writer writer, RankingTableRenderer renderer) throws IOException, TemplateException {
        renderer.render(writer, this);
    }

    public List<RankedPlayer> getRankedPlayers() {
        return rankedPlayers;
    }
}
