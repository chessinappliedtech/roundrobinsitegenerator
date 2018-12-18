package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

import freemarker.template.TemplateException;
import ru.appliedtech.chess.Game;
import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable.TournamentPlayer;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class PlayerStatusTable {

    private final int maxGames;
    private final Map<Player, String> playerPages;
    private Total total;
    private Player player;
    private List<Opponent> opponents;

    public PlayerStatusTable(int maxGames, Map<Player, String> playerPages) {
        this.maxGames = maxGames;
        this.playerPages = playerPages;
    }

    public int getMaxGames() {
        return maxGames;
    }

    public Player getPlayer() {
        return player;
    }

    public Total getTotal() {
        return total;
    }

    public List<Opponent> getOpponents() {
        return opponents;
    }

    public void render(Writer writer, PlayerStatusTableRenderer renderer) throws IOException, TemplateException {
        renderer.render(writer, this);
    }

    public void calculate(Player player, List<Player> registeredPlayers, List<Game> allGames) {
        this.player = player;
        int totalGamesPlayed = (int) allGames.stream()
                .filter(game -> game.isPlayedBy(player.getId()))
                .count();
        BigDecimal totalScore = allGames.stream()
                .filter(game -> game.isPlayedBy(player.getId()))
                .map(game -> game.getScoreOf(player.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        total = new Total(totalGamesPlayed, totalScore);
        List<Player> opponentPlayers = registeredPlayers.stream()
                .filter(registeredPlayer -> !registeredPlayer.getId().equals(player.getId()))
                .sorted(comparing(Player::getLastName).thenComparing(Player::getFirstName).thenComparing(Player::getId))
                .collect(toList());
        opponents = opponentPlayers.stream()
                .map(opponentPlayer -> {
                    List<Game> games = allGames.stream()
                            .filter(game -> game.isPlayedBy(player.getId()))
                            .filter(game -> game.isPlayedBy(opponentPlayer.getId()))
                            .collect(toList());
                    List<GameWithOpponent> gameWithOpponents = new ArrayList<>();
                    for (int index = 0; index < games.size(); index++) {
                        Game game = games.get(index);
                        String color = getColor(player, game);
                        BigDecimal score = game.getScoreOf(player.getId());
                        String lichess = game.getOuterServiceLinks() != null ? (String) game.getOuterServiceLinks().get("lichess") : null;
                        gameWithOpponents.add(new GameWithOpponent(index + 1, color,
                                TournamentPlayer.scoreToString(score), game.getDate(),
                                game.getPgnLocation(), lichess));
                    }
                    if (gameWithOpponents.size() < maxGames) {
                        for (int index = gameWithOpponents.size(); index < maxGames; index++) {
                            gameWithOpponents.add(new GameWithOpponent(index + 1, "*",
                                    "*", "*", null, null));
                        }
                    }
                    return new Opponent(opponentPlayer, gameWithOpponents, playerPages.get(opponentPlayer));
                })
                .collect(toList());
    }

    private String getColor(Player player, Game game) {
        String color;
        if (game.isWhite(player.getId())) {
            color = "&#1073;&#1077;&#1083;&#1099;&#1081;"; // white
        } else {
            color = "&#1095;&#1105;&#1088;&#1085;&#1099;&#1081;"; // black
        }
        return color;
    }

    public static class Total {
        private final int gamesPlayed;
        private final BigDecimal score;

        public Total(int gamesPlayed, BigDecimal score) {
            this.gamesPlayed = gamesPlayed;
            this.score = score;
        }

        public String getScore() {
            return TournamentPlayer.scoreToString(score);
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }
    }
}
