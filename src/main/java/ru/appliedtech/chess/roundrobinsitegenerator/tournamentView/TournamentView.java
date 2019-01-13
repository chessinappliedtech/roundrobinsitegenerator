package ru.appliedtech.chess.roundrobinsitegenerator.tournamentView;

import freemarker.template.TemplateException;
import ru.appliedtech.chess.Game;
import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.tiebreaksystems.*;

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
import static java.util.stream.Collectors.toList;

public class TournamentView {
    private final Map<Player, String> playerPages;
    private final List<TieBreakSystem> tieBreakSystems;
    private int playersCount;
    private List<TournamentPlayer> tournamentPlayers;

    public TournamentView(Map<Player, String> playerPages, List<TieBreakSystem> tieBreakSystems) {
        this.playerPages = playerPages;
        this.tieBreakSystems = tieBreakSystems;
    }

    public void calculate(List<Player> registeredPlayers, List<Game> allGames) {
        playersCount = registeredPlayers.size();
        List<Player> registeredSortedPlayers = registeredPlayers.stream()
                .sorted(comparing(Player::getLastName).thenComparing(Player::getFirstName).thenComparing(Player::getId))
                .collect(toList());
        tournamentPlayers = registeredSortedPlayers.stream()
                .map(toTournamentPlayer(registeredSortedPlayers, allGames))
                .collect(toList());
        assignRanks(tournamentPlayers, tieBreakSystems, allGames);
    }

    private void assignRanks(List<TournamentPlayer> tournamentPlayers, List<TieBreakSystem> tieBreakSystems, List<Game> allGames) {
        List<TournamentPlayer> players = new ArrayList<>(tournamentPlayers);
        Comparator<Player> playerComparator = tieBreakSystems.stream()
                .map(t -> (Comparator<Player>)t)
                .reduce(Comparator::thenComparing)
                .orElse(new DirectEncounterSystem(allGames));
        Comparator<TournamentPlayer> comparator = (o1, o2) -> playerComparator.compare(o1.getPlayer(), o2.getPlayer());
        players.sort(comparator
                .thenComparing(TournamentPlayer::getLastName)
                .thenComparing(TournamentPlayer::getFirstName)
                .thenComparing(TournamentPlayer::getId));
        int rank = 1;
        TournamentPlayer previousPlayer = null;
        for (TournamentPlayer player : players) {
            if (previousPlayer != null) {
                int compareResult = comparator.compare(previousPlayer, player);
                if (compareResult < 0) {
                    rank += 1;
                }
            }
            previousPlayer = player;
            player.setRank(rank);
        }
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
            int wins = tieBreakSystems.stream()
                    .filter(GreaterNumberOfWinsSystem.class::isInstance)
                    .map(GreaterNumberOfWinsSystem.class::cast)
                    .map(tbs -> tbs.scoreOf(player).intValue()).findFirst()
                    .orElse(0);
            BigDecimal neustadtl = tieBreakSystems.stream()
                    .filter(NeustadtlSystem.class::isInstance)
                    .map(NeustadtlSystem.class::cast)
                    .map(tbs -> tbs.scoreOf(player)).findFirst()
                    .orElse(BigDecimal.ZERO);
            BigDecimal koya = tieBreakSystems.stream()
                    .filter(KoyaSystem.class::isInstance)
                    .map(KoyaSystem.class::cast)
                    .map(tbs -> tbs.scoreOf(player)).findFirst()
                    .orElse(BigDecimal.ZERO);
            return new TournamentPlayer(player, 0, totalScore, gamesPlayed, scores, playerPages.get(player), wins, neustadtl, koya);
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

    public void render(Writer writer, TournamentViewRenderer tournamentViewRenderer) throws IOException, TemplateException {
        tournamentViewRenderer.render(writer, this);
    }
}
