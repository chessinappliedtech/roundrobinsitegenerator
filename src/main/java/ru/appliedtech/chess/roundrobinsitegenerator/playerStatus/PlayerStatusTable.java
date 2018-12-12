package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

import freemarker.template.TemplateException;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Game;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable.TournamentPlayer.scoreToString;

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
        int totalScore = allGames.stream()
                .filter(game -> game.isPlayedBy(player.getId()))
                .mapToInt(game -> game.getScoreOf(player.getId()))
                .sum();
        total = new Total(totalGamesPlayed, totalScore);
        List<Player> opponentPlayers = registeredPlayers.stream()
                .filter(registeredPlayer -> !registeredPlayer.getId().equals(player.getId()))
                .sorted(Comparator.comparing(Player::getLastname).thenComparing(Player::getFirstname).thenComparing(Player::getId))
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
                        int score = game.getScoreOf(player.getId());
                        gameWithOpponents.add(new GameWithOpponent(index + 1, color, scoreToString(score), game.getDate()));
                    }
                    if (gameWithOpponents.size() < maxGames) {
                        for (int index = gameWithOpponents.size(); index < maxGames; index++) {
                            gameWithOpponents.add(new GameWithOpponent(index + 1, "*", "*", "*"));
                        }
                    }
                    return new Opponent(opponentPlayer, gameWithOpponents, playerPages.get(opponentPlayer));
                })
                .collect(toList());
    }

    private String getColor(Player player, Game game) {
        String color;
        if (game.getWhiteId().equals(player.getId())) {
            color = "&#1073;&#1077;&#1083;&#1099;&#1081;"; // white
        } else {
            color = "&#1095;&#1105;&#1088;&#1085;&#1099;&#1081;"; // black
        }
        return color;
    }

    private String getOpponentId(Player player, Game game) {
        String opponentId;
        if (game.getWhiteId().equals(player.getId())) {
            opponentId = game.getBlackId();
        } else {
            opponentId = game.getWhiteId();
        }
        return opponentId;
    }

    public static class Total {
        private final int gamesPlayed;
        private final int score;

        public Total(int gamesPlayed, int score) {
            this.gamesPlayed = gamesPlayed;
            this.score = score;
        }

        public String getScore() {
            return scoreToString(score);
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }
    }
}
