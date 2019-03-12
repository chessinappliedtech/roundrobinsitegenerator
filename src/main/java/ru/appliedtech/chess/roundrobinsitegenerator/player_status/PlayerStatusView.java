package ru.appliedtech.chess.roundrobinsitegenerator.player_status;

import ru.appliedtech.chess.Game;
import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.elorating.EloRatingChange;
import ru.appliedtech.chess.roundrobin.RoundRobinSetup;
import ru.appliedtech.chess.roundrobin.color_allocating.ColorAllocatingSystem;
import ru.appliedtech.chess.roundrobin.player_status.PlayerStatus;
import ru.appliedtech.chess.roundrobinsitegenerator.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class PlayerStatusView {
    private final ResourceBundle resourceBundle;
    private final PlayerStatus playerStatus;
    private final RoundRobinSetup roundRobinSetup;
    private final PlayerLinks playerLinks;
    private final ColorAllocatingSystem colorAllocatingSystem;
    private final HeaderRowView headerRow;
    private final List<RowView<CellView>> rows;
    private final RowView<CellView> summaryRow;

    public PlayerStatusView(Locale locale,
                            RoundRobinSetup roundRobinSetup,
                            PlayerStatus playerStatus,
                            PlayerLinks playerLinks,
                            ColorAllocatingSystem colorAllocatingSystem) {
        this.resourceBundle = ResourceBundle.getBundle("resources", locale);
        this.playerStatus = playerStatus;
        this.roundRobinSetup = roundRobinSetup;
        this.playerLinks = playerLinks;
        this.colorAllocatingSystem = colorAllocatingSystem;
        this.headerRow = createHeaderRow();
        this.rows = createRows();
        this.summaryRow = createSummaryRow();
    }

    private RowView<CellView> createSummaryRow() {
        List<CellView> cells = new ArrayList<>();
        cells.add(new CellView(resourceBundle.getString("player.status.view.summary")));
        cells.add(new IntCellView(playerStatus.getGamesPlayed(), null));
        cells.add(new CellView(""));
        cells.add(playerStatus.getTotalScore().map(score -> (CellView) new ScoreCellView(score)).orElse(new CellView("")));
        cells.add(new CellView(""));
        cells.add(playerStatus.getRatingChange().map(change -> new CellView(String.valueOf(change.getValue()))).orElse(new CellView("")));
        return new RowView<>(cells);
    }

    private List<RowView<CellView>> createRows() {
        return playerStatus.getOpponents().stream()
                .flatMap(opponent -> {
                    Player opponentPlayer = playerStatus.resolvePlayer(opponent.getId());
                    List<CellView> mainRowCells = new ArrayList<>();
                    mainRowCells.add(new CellView(
                            getFullName(opponentPlayer),
                            playerLinks.getLink(opponentPlayer.getId()).map(PlayerLink::getLink).orElse(null),
                            1,
                            roundRobinSetup.getRoundsAmount()));
                    String playerId = playerStatus.getPlayer().getId();
                    List<ColorAllocatingSystem.Color> colors = IntStream.rangeClosed(1, roundRobinSetup.getRoundsAmount())
                            .mapToObj(round -> colorAllocatingSystem.getColor(playerId, opponentPlayer.getId(), round))
                            .collect(toList());
                    ColorAllocatingSystem.Color firstColor = colors.get(0);
                    List<Game> games = new ArrayList<>(opponent.getGames());
                    Optional<Game> firstGame = games.stream()
                            .filter(game -> firstColor == ColorAllocatingSystem.Color.white ? game.isWhite(playerId) : game.isBlack(playerId))
                            .findFirst();
                    firstGame.ifPresent(games::remove);
                    List<CellView> firstGameCells = firstGame
                            .map(game -> completedGameCells(1, game))
                            .orElse(upcomingGameCells(1, firstColor));
                    mainRowCells.addAll(firstGameCells);
                    RowView<CellView> mainRow = new RowView<>(mainRowCells);
                    List<RowView<CellView>> playerRows = new ArrayList<>();
                    List<RowView<CellView>> gameRows = IntStream.rangeClosed(2, roundRobinSetup.getRoundsAmount())
                            .mapToObj(i -> {
                                ColorAllocatingSystem.Color color = colors.get(i - 1);
                                Optional<Game> game = games.stream()
                                        .filter(g -> color == ColorAllocatingSystem.Color.white ? g.isWhite(playerId) : g.isBlack(playerId))
                                        .findFirst();
                                game.ifPresent(games::remove);
                                return game.map(g -> completedGameCells(i, g)).orElse(upcomingGameCells(i, color));
                            })
                            .map(RowView::new)
                            .collect(toList());
                    playerRows.add(mainRow);
                    playerRows.addAll(gameRows);
                    return playerRows.stream();
                })
                .collect(toList());
    }

    private List<CellView> completedGameCells(int gameIndex, Game game) {
        String link = null;
        if (game.getOuterServiceLinks() != null) {
            link = (String) game.getOuterServiceLinks().get("lichess");
        }
        IntCellView indexCell = new IntCellView(gameIndex, link);
        return asList(
                indexCell,
                new CellView(game.isWhite(getPlayerId())
                        ? resourceBundle.getString("player.status.view.white")
                        : resourceBundle.getString("player.status.view.black")),
                new ScoreCellView(game.getScoreOf(getPlayerId())),
                new CellView(game.getDate()),
                new CellView(String.valueOf(ratingChangeIn(game))));
    }

    private List<CellView> upcomingGameCells(int gameIndex, ColorAllocatingSystem.Color color) {
        return asList(
                new IntCellView(gameIndex, null),
                new CellView(color == ColorAllocatingSystem.Color.white
                        ? resourceBundle.getString("player.status.view.white")
                        : resourceBundle.getString("player.status.view.black")),
                new CellView("*"),
                new CellView("*"),
                new CellView("*"));
    }

    private String getPlayerId() {
        return playerStatus.getPlayer().getId();
    }

    private BigDecimal ratingChangeIn(Game game) {
        EloRatingChange ratingChange = playerStatus.toRatingChange(playerStatus.getPlayer()).apply(game);
        return ratingChange.getValue();
    }

    private HeaderRowView createHeaderRow() {
        return new HeaderRowView(asList(
                new HeaderCell(resourceBundle.getString("player.status.view.header.opponent")),
                new HeaderCell(resourceBundle.getString("player.status.view.header.gameN")),
                new HeaderCell(resourceBundle.getString("player.status.view.header.color")),
                new HeaderCell(resourceBundle.getString("player.status.view.header.score")),
                new HeaderCell(resourceBundle.getString("player.status.view.header.date")),
                new HeaderCell(resourceBundle.getString("player.status.view.header.ratingChange"))));
    }

    public String getPlayerFullName() {
        return getFullName(playerStatus.getPlayer());
    }

    private String getFullName(Player player) {
        return player.getFirstName() + " " + player.getLastName();
    }

    public HeaderRowView getHeaderRow() {
        return headerRow;
    }

    public List<RowView<CellView>> getRows() {
        return rows;
    }

    public RowView<CellView> getSummaryRow() {
        return summaryRow;
    }

    public String getCurrentRating() {
        return playerStatus.getCurrentRating().getValue().toString();
    }
}
