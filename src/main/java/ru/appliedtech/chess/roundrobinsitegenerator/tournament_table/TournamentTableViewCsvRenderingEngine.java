package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import ru.appliedtech.chess.roundrobinsitegenerator.model.CellView;
import ru.appliedtech.chess.roundrobinsitegenerator.model.HeaderRowView;
import ru.appliedtech.chess.roundrobinsitegenerator.model.PlayerRowView;

import java.io.IOException;
import java.io.Writer;

import static java.util.stream.Collectors.toList;

public class TournamentTableViewCsvRenderingEngine implements TournamentTableViewRenderingEngine {
    @Override
    public void render(TournamentTableView tournamentTableView, Writer writer) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withAutoFlush(true);
        CSVPrinter csvPrinter = csvFormat.print(writer);
        HeaderRowView headerRowView = tournamentTableView.getHeaderRowView();
        csvPrinter.printRecord((Object[]) headerRowView.getCells().stream()
                .map(CellView::getValue)
                .toArray(Object[]::new));
        for (int i = 0; i < tournamentTableView.getPlayerRowViews().size(); i++) {
            PlayerRowView playerRowView = tournamentTableView.getPlayerRowViews().get(i);
            csvPrinter.printRecord(
                    playerRowView.getCells().stream()
                            .map(CellView::getValue)
                            .collect(toList()));
        }
    }
}
