package ru.appliedtech.chess.roundrobinsitegenerator.player_status;

import java.io.IOException;
import java.io.OutputStream;

public interface PlayerStatusViewRenderingEngine {
    void render(PlayerStatusView playerStatusView, OutputStream os) throws IOException;
}
