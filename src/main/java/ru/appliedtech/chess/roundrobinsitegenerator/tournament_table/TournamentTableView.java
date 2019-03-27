package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import ru.appliedtech.chess.TournamentDescription;
import ru.appliedtech.chess.roundrobin.TournamentTable;
import ru.appliedtech.chess.roundrobinsitegenerator.model.*;
import ru.appliedtech.chess.tiebreaksystems.TieBreakSystem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toList;

public class TournamentTableView {
    private static final String QUIT_PLAYER = "&ndash;";
    private static final String QUIT_OPPONENT = "+";
    private final HeaderRowView headerRowView;
    private final ResourceBundle resourceBundle;
    private final List<PlayerRowView> playerRowViews;
    private final TournamentDescription tournamentDescription;
    private final PlayerLinks playerLinks;

    public TournamentTableView(Locale locale, TournamentTable tournamentTable,
                               TournamentDescription tournamentDescription, PlayerLinks playerLinks) {
        this.resourceBundle = ResourceBundle.getBundle("resources", locale);
        this.playerLinks = playerLinks;
        this.headerRowView = createHeaderRowView(tournamentTable);
        this.playerRowViews = createPlayerRowViews(tournamentTable);
        this.tournamentDescription = tournamentDescription;
    }

    public String getTournamentTitle() {
        return tournamentDescription.getTournamentTitle();
    }

    public HeaderRowView getHeaderRowView() {
        return headerRowView;
    }

    public List<PlayerRowView> getPlayerRowViews() {
        return playerRowViews;
    }

    private HeaderRowView createHeaderRowView(TournamentTable tournamentTable) {
        List<HeaderCell> headerCells = new ArrayList<>();
        headerCells.add(new HeaderCell(resourceBundle.getString("tournament.table.view.header.index")));
        headerCells.add(new HeaderCell(resourceBundle.getString("tournament.table.view.header.player")));
        headerCells.add(new HeaderCell(resourceBundle.getString("tournament.table.view.header.rating")));
        for (int i = 0; i < tournamentTable.getPlayersCount(); i++) {
            headerCells.add(new HeaderCell(
                    resourceBundle.getString("tournament.table.view.header.opponent") + (i + 1)));
        }
        headerCells.add(new HeaderCell(resourceBundle.getString("tournament.table.view.header.gamesPlayed")));
        for (int i = 0; i < tournamentTable.getTieBreakSystems().size(); i++) {
            TieBreakSystem tieBreakSystem = tournamentTable.getTieBreakSystems().get(i);
            headerCells.add(new HeaderCell(
                    resourceBundle.getString("tournament.table.view.header.tieBreakSystem." + tieBreakSystem.getName())));
        }
        headerCells.add(new HeaderCell(resourceBundle.getString("tournament.table.view.header.rank")));
        headerCells.add(new HeaderCell(resourceBundle.getString("tournament.table.view.header.newRating")));
        return new HeaderRowView(headerCells);
    }

    private List<PlayerRowView> createPlayerRowViews(TournamentTable tournamentTable) {
        List<PlayerRowView> rowViews = new ArrayList<>();
        List<TournamentTable.PlayerRow> playerRows = tournamentTable.getPlayerRows();
        for (int i = 0; i < playerRows.size(); i++) {
            TournamentTable.PlayerRow playerRow = playerRows.get(i);
            List<CellView> cells = new ArrayList<>();
            cells.add(new IntCellView(i + 1));
            cells.add(new CellView(
                    playerRow.getPlayer().getFirstName() + " " + playerRow.getPlayer().getLastName(),
                    playerLinks.getLink(playerRow.getPlayer().getId()).map(PlayerLink::getLink).orElse(null),
                    1,
                    1));
            cells.add(new RatingCellView(playerRow.getInitialRating().getValue()));

            for (int j = 0; j < playerRows.size(); j++) {
                if (j != i) {
                    String opponentId = playerRows.get(j).getPlayer().getId();
                    TournamentTable.OpponentCell opponentCell = playerRow.getOpponents().stream()
                            .filter(o -> o.getOpponentId().equals(opponentId))
                            .findFirst()
                            .orElseThrow(IllegalStateException::new);
                    CellView scoreCellView = opponentCell.getScores().stream()
                            .reduce(BigDecimal::add)
                            .map(score -> (CellView)new OpponentScoreCellView(score))
                            .orElse(toEmptyScoreCellView(playerRow, opponentCell));
                    cells.add(scoreCellView);
                } else {
                    cells.add(new DiagonalCellView());
                }
            }

            cells.add(new IntCellView(playerRow.getGamesPlayed()));
            cells.addAll(playerRow.getTieBreakValues().stream()
                    .map(tieBreakValue -> new ScoreCellView(tieBreakValue.getValue()))
                    .collect(toList()));
            cells.add(playerRow.isQuit() ? new CellView(QUIT_PLAYER) : new IntCellView(tournamentTable.getRanking().get(playerRow.getPlayer().getId())));
            cells.add(new RatingCellView(playerRow.getCurrentRating().getValue()));

            rowViews.add(new PlayerRowView(cells));
        }
        return rowViews;
    }

    private CellView toEmptyScoreCellView(TournamentTable.PlayerRow playerRow, TournamentTable.OpponentCell opponentCell) {
        if (opponentCell.isQuit() && playerRow.isQuit()) {
            return new CellView(QUIT_PLAYER);
        }
        else if (opponentCell.isQuit()) {
            return new CellView(QUIT_OPPONENT);
        }
        else if (playerRow.isQuit()) {
            return new CellView(QUIT_PLAYER);
        }
        return new NoScoreCellView();
    }

    public boolean isDiagonalCell(CellView cellView) {
        return cellView instanceof DiagonalCellView;
    }
}
