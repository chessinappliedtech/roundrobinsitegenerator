package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import java.io.IOException;
import java.io.OutputStream;

public interface TournamentTableViewRenderingEngine {
    public void render(TournamentTableView tournamentTableView, OutputStream os) throws IOException;
}
