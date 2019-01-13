package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import ru.appliedtech.chess.TournamentDescription;
import ru.appliedtech.chess.roundrobin.TournamentTable;
import ru.appliedtech.chess.tiebreaksystems.TieBreakSystem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class TournamentTableView {
    private final HeaderRowView headerRowView;
    private final ResourceBundle resourceBundle;
    private final List<PlayerRowView> playerRowViews;
    private final TournamentDescription tournamentDescription;

    public TournamentTableView(Locale locale, TournamentTable tournamentTable, TournamentDescription tournamentDescription) {
        this.resourceBundle = ResourceBundle.getBundle("resources", locale);
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
            cells.add(new CellView(playerRow.getPlayer().getFirstName() + " " + playerRow.getPlayer().getLastName()));
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
                            .orElse(new NoScoreCellView());
                    cells.add(scoreCellView);
                } else {
                    cells.add(new DiagonalCellView());
                }
            }

            cells.add(new IntCellView(playerRow.getGamesPlayed()));
            cells.addAll(playerRow.getTieBreakValues().stream()
                    .map(tieBreakValue -> new ScoreCellView(tieBreakValue.getValue()))
                    .collect(toList()));
            cells.add(new IntCellView(tournamentTable.getRanking().get(playerRow.getPlayer().getId())));
            cells.add(new RatingCellView(playerRow.getCurrentRating().getValue()));

            rowViews.add(new PlayerRowView(cells));
        }
        return rowViews;
    }

    public boolean isDiagonalCell(CellView cellView) {
        return cellView instanceof DiagonalCellView;
    }

    public static class RowView<T extends CellView> {
        private final List<T> cells;

        private RowView(List<T> cells) {
            this.cells = cells != null ? new ArrayList<>(cells) : emptyList();
        }

        public List<T> getCells() {
            return unmodifiableList(cells);
        }
    }

    public static class CellView {
        private final String value;
        private final int colspan;
        private final int rowspan;

        private CellView(String value) {
            this(value, 1, 1);
        }

        private CellView(String value, int colspan, int rowspan) {
            this.value = value;
            this.colspan = colspan;
            this.rowspan = rowspan;
        }

        public String getValue() {
            return value;
        }

        public int getColspan() {
            return colspan;
        }

        public int getRowspan() {
            return rowspan;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class IntCellView extends CellView {
        private IntCellView(int value) {
            this(value, 1, 1);
        }

        private IntCellView(int value, int colspan, int rowspan) {
            super(String.valueOf(value), colspan, rowspan);
        }
    }

    public static class ScoreCellView extends CellView {
        private ScoreCellView(BigDecimal value) {
            this(value, 1, 1);
        }

        private ScoreCellView(BigDecimal value, int colspan, int rowspan) {
            super(scoreToString(value), colspan, rowspan);
        }
    }

    public static class RatingCellView extends CellView {
        private RatingCellView(BigDecimal value) {
            this(value, 1, 1);
        }

        private RatingCellView(BigDecimal value, int colspan, int rowspan) {
            super(String.valueOf(value), colspan, rowspan);
        }
    }

    public static String scoreToString(BigDecimal value) {
        String result;
        BigDecimal wholePart = new BigDecimal(value.intValue());
        String wholePartString = wholePart.toBigInteger().toString();
        if (isWhole(value)) {
            result = wholePartString;
        } else if (hasHalf(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#189;";
        } else if (hasFourth(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#188;";
        } else if (hasThreeQuarters(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#190;";
        } else {
            result = value.toString();
        }
        return result;
    }

    private static boolean isWhole(BigDecimal value) {
        return value.remainder(ONE).compareTo(ZERO) == 0;
    }

    private static boolean hasHalf(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.5)) == 0;
    }

    private static boolean hasFourth(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.25)) == 0;
    }

    private static boolean hasThreeQuarters(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.75)) == 0;
    }

    public static class DiagonalCellView extends CellView {
        private DiagonalCellView() {
            this(1, 1);
        }

        private DiagonalCellView(int colspan, int rowspan) {
            super("", colspan, rowspan);
        }
    }

    public static class NoScoreCellView extends CellView {
        private NoScoreCellView() {
            this(1, 1);
        }

        private NoScoreCellView(int colspan, int rowspan) {
            super("", colspan, rowspan);
        }
    }

    public static class OpponentScoreCellView extends ScoreCellView {
        private OpponentScoreCellView(BigDecimal value) {
            this(value, 1, 1);
        }

        private OpponentScoreCellView(BigDecimal value, int colspan, int rowspan) {
            super(value, colspan, rowspan);
        }
    }

    public static class HeaderCell extends CellView {
        private HeaderCell(String value, int colspan, int rowspan) {
            super(value, colspan, rowspan);
        }

        private HeaderCell(String value) {
            this(value, 1, 1);
        }
    }

    public static class HeaderRowView extends RowView<HeaderCell> {
        private HeaderRowView(List<HeaderCell> headers) {
            super(headers);
        }
    }

    public static class PlayerRowView extends RowView<CellView>  {
        private PlayerRowView(List<CellView> cells) {
            super(cells);
        }
    }
}
