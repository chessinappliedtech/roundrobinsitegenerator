package ru.appliedtech.chess.roundrobinsitegenerator.player_status;

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
import ru.appliedtech.chess.roundrobin.color_allocating.ColorAllocatingSystemFactory;
import ru.appliedtech.chess.roundrobin.io.RoundRobinSetupObjectNodeReader;
import ru.appliedtech.chess.roundrobin.player_status.PlayerStatus;
import ru.appliedtech.chess.roundrobinsitegenerator.RoundRobinSiteGenerator;
import ru.appliedtech.chess.roundrobinsitegenerator.model.PlayerLinks;
import ru.appliedtech.chess.storage.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static ru.appliedtech.chess.roundrobin.RoundRobinSetup.ColorAllocatingSystemDescription;

public class PlayerStatusViewHtmlRenderingEngine implements PlayerStatusViewRenderingEngine {
    private final Configuration templatesConfiguration;

    public PlayerStatusViewHtmlRenderingEngine(Configuration templatesConfiguration) {
        this.templatesConfiguration = templatesConfiguration;
    }

    @Override
    public void render(PlayerStatusView playerStatusView, OutputStream os) throws IOException {
        Template template = templatesConfiguration.getTemplate("playerStatus.ftl");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        try {
            template.process(playerStatusView, bw);
            bw.flush();
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        ColorAllocatingSystemDescription colorAllocatingSystemDescription =
                new ColorAllocatingSystemDescription("fixed-alternation-color-allocating-system", 123456);
        RoundRobinSetup setup = new RoundRobinSetup(
                2,
                GameResultSystem.STANDARD,
                asList("direct-encounter", "number-of-wins", "neustadtl", "koya"),
                TimeControlType.BLITZ,
                colorAllocatingSystemDescription);

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
        Player player = playerStorage.getPlayer("alexey.biryukov").orElse(null);
        TournamentDescription tournamentDescription = new TournamentDescription(
                "Title",
                "blitz1.dec2018",
                "Arbiter",
                identifiers(registeredPlayers),
                emptyList(),
                emptyList(),
                "",
                new Date(),
                setup,
                emptyList(),
                emptyList(),
                null);
        PlayerStatus playerStatus = new PlayerStatus(player, playerStorage, gameStorage,
                eloRatingStorage, kValueStorage, tournamentDescription, setup);
        PlayerLinks playerLinks = new PlayerLinks(id -> null, emptyMap());
        PlayerStatusView tournamentTableView = new PlayerStatusView(
                new Locale("ru", "RU"),
                setup,
                playerStatus,
                playerLinks,
                new ColorAllocatingSystemFactory(setup).create(identifiers(registeredPlayers), emptyList()),
                null);
        try (OutputStream os = new FileOutputStream("C:\\Temp\\playerStatus.html")) {
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            configuration.setLogTemplateExceptions(true);
            configuration.setWrapUncheckedExceptions(true);
            configuration.setClassForTemplateLoading(RoundRobinSiteGenerator.class, "/");
            new PlayerStatusViewHtmlRenderingEngine(configuration).render(tournamentTableView, os);
        }
    }

    private static List<String> identifiers(List<Player> registeredPlayers) {
        return registeredPlayers.stream().map(Player::getId).collect(toList());
    }
}
