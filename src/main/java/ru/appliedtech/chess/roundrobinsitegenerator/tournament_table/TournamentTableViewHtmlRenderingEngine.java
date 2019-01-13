package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.elorating.EloRating;
import ru.appliedtech.chess.elorating.KValueSet;
import ru.appliedtech.chess.roundrobin.RoundRobinSetup;
import ru.appliedtech.chess.roundrobin.TournamentTable;
import ru.appliedtech.chess.roundrobin.io.RoundRobinSetupObjectNodeReader;
import ru.appliedtech.chess.roundrobinsitegenerator.RoundRobinSiteGenerator;
import ru.appliedtech.chess.storage.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class TournamentTableViewHtmlRenderingEngine implements TournamentTableViewRenderingEngine {
    private final Configuration templatesConfiguration;

    public TournamentTableViewHtmlRenderingEngine(Configuration templatesConfiguration) {
        this.templatesConfiguration = templatesConfiguration;
    }

    @Override
    public void render(TournamentTableView tournamentTableView, OutputStream os) throws IOException {
        Template template = templatesConfiguration.getTemplate("tournamentTable.ftl");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
        try {
            template.process(tournamentTableView, bw);
        } catch (TemplateException e) {
            throw new IOException(e);
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
        try (OutputStream os = new FileOutputStream("C:\\Temp\\tournamentTableView.html")) {
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            configuration.setLogTemplateExceptions(true);
            configuration.setWrapUncheckedExceptions(true);
            configuration.setClassForTemplateLoading(RoundRobinSiteGenerator.class, "/");
            new TournamentTableViewHtmlRenderingEngine(configuration).render(tournamentTableView, os);
        }
    }
}
