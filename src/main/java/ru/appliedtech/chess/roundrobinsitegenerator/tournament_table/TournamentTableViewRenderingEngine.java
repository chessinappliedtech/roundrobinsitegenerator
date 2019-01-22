package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import java.io.IOException;
import java.io.Writer;

public interface TournamentTableViewRenderingEngine {
    void render(TournamentTableView tournamentTableView, Writer writer) throws IOException;
}
