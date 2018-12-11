package ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable;

import freemarker.template.TemplateException;
import ru.appliedtech.chess.roundrobinsitegenerator.ranking.RankedPlayer;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Game;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.*;
import static java.util.Comparator.comparing;
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
                .sorted(comparing(Player::getLastname).thenComparing(Player::getFirstname).thenComparing(Player::getId))
                .collect(toList());
        tournamentPlayers = registeredSortedPlayers.stream()
                .map(toTournamentPlayer(registeredSortedPlayers, allGames))
                .collect(toList());
        assignRanks(tournamentPlayers);
    }

    private static void assignRanks(List<TournamentPlayer> tournamentPlayers) {
        List<TournamentPlayer> players = new ArrayList<>(tournamentPlayers);
        players.sort(comparingInt(TournamentPlayer::getScoreValue).reversed()
                .thenComparing(TournamentPlayer::getLastname)
                .thenComparing(TournamentPlayer::getFirstname)
                .thenComparing(TournamentPlayer::getId));
        int rank = 1;
        int previousScore = -1;
        for (TournamentPlayer player : players) {
            if (previousScore != -1) {
                if (previousScore != player.getScoreValue()) {
                    rank += 1;
                }
                previousScore = player.getScoreValue();
            } else {
                previousScore = player.getScoreValue();
            }
            player.setRank(rank);
        }
    }

    private Function<Player, TournamentPlayer> toTournamentPlayer(List<Player> registeredPlayers, List<Game> allGames) {
        return player -> {
            int totalScore = allGames.stream()
                    .filter(game -> game.isPlayedBy(player.getId()))
                    .mapToInt(game -> game.getScoreOf(player.getId()))
                    .sum();
            List<Integer> scores = registeredPlayers.stream()
                    .map(opponent -> allGames.stream()
                            .filter(gameOf(player, opponent))
                            .collect(toList()))
                    .map(games -> games.stream().mapToInt(g -> g.getScoreOf(player.getId())).sum())
                    .collect(toList());
            return new TournamentPlayer(player, 0, totalScore, scores, playerPages.get(player));
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
