package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.elorating.EloRating;
import ru.appliedtech.chess.elorating.KValueSet;
import ru.appliedtech.chess.roundrobin.RoundRobinSetup;
import ru.appliedtech.chess.roundrobin.TournamentTable;
import ru.appliedtech.chess.roundrobin.io.RoundRobinSetupObjectNodeReader;
import ru.appliedtech.chess.storage.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class TournamentTableViewCsvRenderingEngine implements TournamentTableViewRenderingEngine {
    private final Charset charset;

    public TournamentTableViewCsvRenderingEngine(Charset charset) {
        this.charset = charset != null ? charset : Charset.forName("UTF-8");
    }

    @Override
    public void render(TournamentTableView tournamentTableView, OutputStream os) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, charset))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.withAutoFlush(true);
            CSVPrinter csvPrinter = csvFormat.print(bw);
            TournamentTableView.HeaderRowView headerRowView = tournamentTableView.getHeaderRowView();
            csvPrinter.printRecord((Object[]) headerRowView.getCells().stream()
                    .map(TournamentTableView.CellView::getValue)
                    .toArray(Object[]::new));
            for (int i = 0; i < tournamentTableView.getPlayerRowViews().size(); i++) {
                TournamentTableView.PlayerRowView playerRowView = tournamentTableView.getPlayerRowViews().get(i);
                csvPrinter.printRecord(
                        playerRowView.getCells().stream()
                                .map(TournamentTableView.CellView::getValue)
                                .collect(toList()));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        RoundRobinSetup setup = new RoundRobinSetup(
                2,
                GameResultSystem.STANDARD,
                asList("direct-encounter", "number-of-wins", "neustadtl", "koya"),
                TimeControlType.BLITZ);

        Map<String, TournamentSetupObjectNodeReader> tournamentSetupReaders = new HashMap<>();
        tournamentSetupReaders.put("round-robin", new RoundRobinSetupObjectNodeReader());
        ObjectMapper baseMapper = new ChessBaseObjectMapper(tournamentSetupReaders);
        List<Player> registeredPlayers;
        try (FileInputStream fis = new FileInputStream("C:\\Chess\\projects\\Blitz1Dec2018\\data\\players.json")) {
            registeredPlayers = baseMapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        ObjectMapper gameObjectMapper = new GameObjectMapper(setup);
        List<Game> games;
        try (FileInputStream fis = new FileInputStream("C:\\Chess\\projects\\Blitz1Dec2018\\data\\games.json")) {
            games = gameObjectMapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
        }

        PlayerStorage playerStorage = new PlayerReadOnlyStorage(registeredPlayers);
        GameStorage gameStorage = new GameReadOnlyStorage(games);
        Map<EloRatingKey, EloRating> ratings = emptyMap();
        EloRatingReadOnlyStorage eloRatingStorage = new EloRatingReadOnlyStorage(ratings);
        Map<String, KValueSet> kValues = emptyMap();
        KValueReadOnlyStorage kValueStorage = new KValueReadOnlyStorage(kValues);
        TournamentTable table = new TournamentTable(playerStorage, gameStorage, eloRatingStorage, kValueStorage, setup);
        TournamentTableView tournamentTableView = new TournamentTableView(
                new Locale("ru", "RU"),
                table,
                new TournamentDescription("Title", "Arbiter",
                        emptyList(), emptyList(), "", setup));
        try (OutputStream os = new FileOutputStream("C:\\Temp\\tournamentTableView.csv")) {
            new TournamentTableViewCsvRenderingEngine(Charset.forName("UTF-8")).render(tournamentTableView, os);
        }
    }
}
