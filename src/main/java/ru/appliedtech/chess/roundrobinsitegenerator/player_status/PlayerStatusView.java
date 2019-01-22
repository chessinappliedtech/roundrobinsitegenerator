package ru.appliedtech.chess.roundrobinsitegenerator.player_status;

import ru.appliedtech.chess.Game;
import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.elorating.EloRatingChange;
import ru.appliedtech.chess.roundrobin.RoundRobinSetup;
import ru.appliedtech.chess.roundrobin.player_status.PlayerStatus;
import ru.appliedtech.chess.roundrobinsitegenerator.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class PlayerStatusView {
    private final ResourceBundle resourceBundle;
    private final PlayerStatus playerStatus;
    private final RoundRobinSetup roundRobinSetup;
    private final PlayerLinks playerLinks;
    private final HeaderRowView headerRow;
    private final List<RowView<CellView>> rows;
    private final RowView<CellView> summaryRow;

    public PlayerStatusView(Locale locale, RoundRobinSetup roundRobinSetup, PlayerStatus playerStatus, PlayerLinks playerLinks) {
        this.resourceBundle = ResourceBundle.getBundle("resources", locale);
        this.playerStatus = playerStatus;
        this.roundRobinSetup = roundRobinSetup;
        this.playerLinks = playerLinks;
        this.headerRow = createHeaderRow();
        this.rows = createRows();
        this.summaryRow = createSummaryRow();
    }

    private RowView<CellView> createSummaryRow() {
        List<CellView> cells = new ArrayList<>();
        cells.add(new CellView(resourceBundle.getString("player.status.view.summary")));
        cells.add(new IntCellView(playerStatus.getGamesPlayed()));
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
                    mainRowCells.addAll(opponent.getGames().stream().findFirst()
                            .map(game -> completedGameCells(1, game))
                            .orElse(upcomingGameCells(1)));
                    RowView<CellView> mainRow = new RowView<>(mainRowCells);
                    List<RowView<CellView>> playerRows = new ArrayList<>();
                    List<RowView<CellView>> gameRows = IntStream.range(1, roundRobinSetup.getRoundsAmount())
                            .mapToObj(i -> {
                                if (i < opponent.getGames().size()) {
                                    return completedGameCells(i + 1, opponent.getGames().get(i));
                                } else {
                                    return upcomingGameCells(i + 1);
                                }
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
        return asList(
                new IntCellView(gameIndex),
                new CellView(game.isWhite(getPlayerId())
                        ? resourceBundle.getString("player.status.view.white")
                        : resourceBundle.getString("player.status.view.black")),
                new ScoreCellView(game.getScoreOf(getPlayerId())),
                new CellView(game.getDate()),
                new CellView(String.valueOf(ratingChangeIn(game))));
    }

    private List<CellView> upcomingGameCells(int gameIndex) {
        return asList(
                new IntCellView(gameIndex),
                new CellView("*"),
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
