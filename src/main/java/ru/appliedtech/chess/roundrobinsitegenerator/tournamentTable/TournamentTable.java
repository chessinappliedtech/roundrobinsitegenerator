package ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable;

import freemarker.template.TemplateException;
import ru.appliedtech.chess.Game;
import ru.appliedtech.chess.Player;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class TournamentTable {
    private final Map<Player, String> playerPages;
    private int playersCount;
    private List<TournamentPlayer> tournamentPlayers;

    public TournamentTable(Map<Player, String> playerPages) {
        this.playerPages = playerPages;
    }

    public void calculate(List<Player> registeredPlayers, List<Game> allGames) {
        playersCount = registeredPlayers.size();
        List<Player> registeredSortedPlayers = registeredPlayers.stream()
                .sorted(comparing(Player::getLastName).thenComparing(Player::getFirstName).thenComparing(Player::getId))
                .collect(toList());
        tournamentPlayers = registeredSortedPlayers.stream()
                .map(toTournamentPlayer(registeredSortedPlayers, allGames))
                .collect(toList());
        assignRanks(tournamentPlayers);
    }

    private static void assignRanks(List<TournamentPlayer> tournamentPlayers) {
        List<TournamentPlayer> players = new ArrayList<>(tournamentPlayers);
        players.sort(comparing(TournamentPlayer::getScoreValue).reversed()
                .thenComparing(byGamesPlayed())
                .thenComparing(TournamentPlayer::getLastName)
                .thenComparing(TournamentPlayer::getFirstName)
                .thenComparing(TournamentPlayer::getId));
        int rank = 1;
        BigDecimal previousScore = null;
        int previousGamesPlayed = -1;
        for (TournamentPlayer player : players) {
            if (previousScore != null) {
                if (previousScore.compareTo(player.getScoreValue()) != 0) {
                    rank += 1;
                } else if (previousGamesPlayed != player.getGamesPlayed()) {
                    rank += 1;
                }
                previousScore = player.getScoreValue();
                previousGamesPlayed = player.getGamesPlayed();
            } else {
                previousScore = player.getScoreValue();
                previousGamesPlayed = player.getGamesPlayed();
            }
            player.setRank(rank);
        }
    }

    private static Comparator<TournamentPlayer> byGamesPlayed() {
        return (o1, o2) -> {
            if (o1.getGamesPlayed() == 0 && o2.getGamesPlayed() == 0) {
                return 0;
            } else if (o1.getGamesPlayed() == 0) {
                return 1;
            } else if (o2.getGamesPlayed() == 0) {
                return -1;
            } else {
                return Integer.compare(o1.getGamesPlayed(), o2.getGamesPlayed());
            }
        };
    }

    private Function<Player, TournamentPlayer> toTournamentPlayer(List<Player> registeredPlayers, List<Game> allGames) {
        return player -> {
            BigDecimal totalScore = allGames.stream()
                    .filter(game -> game.isPlayedBy(player.getId()))
                    .map(game -> game.getScoreOf(player.getId()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int gamesPlayed = (int) allGames.stream()
                    .filter(game -> game.isPlayedBy(player.getId()))
                    .count();
            List<BigDecimal> scores = registeredPlayers.stream()
                    .map(opponent -> allGames.stream()
                            .filter(gameOf(player, opponent))
                            .collect(toList()))
                    .map(games -> games.stream().map(g -> g.getScoreOf(player.getId())).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .collect(toList());
            return new TournamentPlayer(player, 0, totalScore, gamesPlayed, scores, playerPages.get(player));
        };
    }

    private Predicate<Game> gameOf(Player player, Player opponent) {
        return g -> !player.getId().equals(opponent.getId())
                && g.isPlayedBy(player.getId())
                && g.isPlayedBy(opponent.getId());
    }

    public int getPlayersCount() {
        return playersCount;
    }

    public List<TournamentPlayer> getTournamentPlayers() {
        return tournamentPlayers;
    }

    public void render(Writer writer, TournamentTableRenderer tournamentTableRenderer) throws IOException, TemplateException {
        tournamentTableRenderer.render(writer, this);
    }
}
