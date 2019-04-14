package ru.appliedtech.chess.roundrobinsitegenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.elorating.EloRating;
import ru.appliedtech.chess.elorating.KValueSet;
import ru.appliedtech.chess.elorating.PlayerEloRating;
import ru.appliedtech.chess.elorating.PlayerKValueSet;
import ru.appliedtech.chess.elorating.io.PlayerEloRatingDeserializer;
import ru.appliedtech.chess.elorating.io.PlayerEloRatingSerializer;
import ru.appliedtech.chess.elorating.io.PlayerKValueSetDeserializer;
import ru.appliedtech.chess.elorating.io.PlayerKValueSetSerializer;
import ru.appliedtech.chess.roundrobin.RoundRobinSetup;
import ru.appliedtech.chess.roundrobin.TournamentTable;
import ru.appliedtech.chess.roundrobin.color_allocating.ColorAllocatingSystem;
import ru.appliedtech.chess.roundrobin.color_allocating.ColorAllocatingSystemFactory;
import ru.appliedtech.chess.roundrobin.io.RoundRobinSetupObjectNodeReader;
import ru.appliedtech.chess.roundrobin.player_status.PlayerStatus;
import ru.appliedtech.chess.roundrobinsitegenerator.model.PlayerLink;
import ru.appliedtech.chess.roundrobinsitegenerator.model.PlayerLinks;
import ru.appliedtech.chess.roundrobinsitegenerator.player_status.PlayerStatusView;
import ru.appliedtech.chess.roundrobinsitegenerator.player_status.PlayerStatusViewHtmlRenderingEngine;
import ru.appliedtech.chess.roundrobinsitegenerator.tournament_table.TournamentTableView;
import ru.appliedtech.chess.roundrobinsitegenerator.tournament_table.TournamentTableViewHtmlRenderingEngine;
import ru.appliedtech.chess.storage.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

public class RoundRobinSiteGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        new RoundRobinSiteGenerator().run(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6]);
    }

    public void run(String localeDef,
                    String tournamentDescriptionFilePath,
                    String playersFilePath,
                    String gamesFilePath,
                    String eloRatingStorageFilePath,
                    String kValueStorageFilePath,
                    String outputDir) throws IOException, TemplateException {
        Configuration configuration = createTemplatesConfiguration();

        TournamentDescription tournamentDescription = readTournamentDescription(tournamentDescriptionFilePath);
        RoundRobinSetup roundRobinSetup = (RoundRobinSetup) tournamentDescription.getTournamentSetup();
        PlayerStorage playerStorage = readPlayers(playersFilePath, tournamentDescription);
        GameStorage gameStorage = readGames(gamesFilePath, tournamentDescription);
        EloRatingReadOnlyStorage eloRatingStorage = readEloRatings(eloRatingStorageFilePath, tournamentDescription);
        KValueReadOnlyStorage kValueStorage = readKValues(kValueStorageFilePath);

        //noinspection ResultOfMethodCallIgnored
        new File(outputDir).mkdirs();

        Locale locale = resolveLocale(localeDef);
        PlayerLinks playerLinks = new PlayerLinks(id -> new PlayerLink(id, "status-" + id + ".html"), emptyMap());
        List<String> players = playerStorage.getPlayers().stream().map(Player::getId).collect(toList());
        List<String> joinedPlayers = players.stream().filter(p -> tournamentDescription.getJoinedPlayers().contains(p)).collect(toList());
        List<String> startingPlayers = new ArrayList<>(players);
        startingPlayers.removeAll(joinedPlayers);
        List<String> quitPlayers = players.stream().filter(p -> tournamentDescription.getQuitPlayers().contains(p)).collect(toList());
        ColorAllocatingSystem colorAllocatingSystem = new ColorAllocatingSystemFactory(roundRobinSetup)
                .create(startingPlayers, joinedPlayers);
        Link mainPageLink = null;
        if (tournamentDescription.getLinks() != null) {
            mainPageLink = tournamentDescription.getLinks().stream()
                    .filter(l -> l.getPurpose().equals("self"))
                    .findFirst()
                    .orElse(null);
        }
        for (Player player : playerStorage.getPlayers()) {
            PlayerStatus playerStatus = new PlayerStatus(player, playerStorage, gameStorage,
                    eloRatingStorage, kValueStorage, tournamentDescription, roundRobinSetup);
            PlayerStatusView playerStatusView = new PlayerStatusView(locale, roundRobinSetup,
                    playerStatus, playerLinks, colorAllocatingSystem, mainPageLink);
            String playerStatusFileName = playerLinks.getLink(player.getId())
                    .map(PlayerLink::getLink)
                    .orElseThrow(IllegalStateException::new);
            try (OutputStream os = new FileOutputStream(new File(outputDir, playerStatusFileName))) {
                new PlayerStatusViewHtmlRenderingEngine(configuration).render(playerStatusView, os);
            }
        }

        TournamentTable table = new TournamentTable(playerStorage, gameStorage,
                eloRatingStorage, kValueStorage, roundRobinSetup, players, quitPlayers);
        TournamentTableView tournamentTableView = new TournamentTableView(
                locale,
                table,
                tournamentDescription,
                playerLinks);
        StringWriter sw = new StringWriter();
        new TournamentTableViewHtmlRenderingEngine(configuration).render(tournamentTableView, sw);

        Map<String, Object> model = new HashMap<>();
        model.put("view", sw.toString());
        model.put("tournamentDescription", resolve(tournamentDescription, playerStorage.getPlayers()));
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(new File(outputDir, "index.html")),
                StandardCharsets.UTF_8)) {
            Template template = configuration.getTemplate("index.ftl");
            template.process(model, writer);
        }
    }

    private Locale resolveLocale(String localeDef) {
        String[] strings = localeDef.split("_");
        String language = strings.length > 0 ? strings[0] : "";
        String country = strings.length > 1 ? strings[1] : "";
        String variant = strings.length > 2 ? strings[2] : "";
        return language.isEmpty() ? Locale.US : new Locale(language, country, variant);
    }

    private KValueReadOnlyStorage readKValues(String kValueStorageFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("base");
        module.addSerializer(new PlayerKValueSetSerializer());
        module.addDeserializer(PlayerKValueSet.class, new PlayerKValueSetDeserializer());
        mapper.registerModule(module);
        List<PlayerKValueSet> records;
        try (FileInputStream fis = new FileInputStream(kValueStorageFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<PlayerKValueSet>>() {});
        }
        Map<String, KValueSet> kValues = records.stream().collect(toMap(
                PlayerKValueSet::getPlayerId,
                PlayerKValueSet::getKValueSet));
        return new KValueReadOnlyStorage(kValues);
    }

    private EloRatingReadOnlyStorage readEloRatings(String eloRatingStorageFilePath, TournamentDescription tournamentDescription) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("base");
        module.addSerializer(new PlayerEloRatingSerializer());
        module.addDeserializer(PlayerEloRating.class, new PlayerEloRatingDeserializer());
        mapper.registerModule(module);

        List<PlayerEloRating> records;
        try (FileInputStream fis = new FileInputStream(eloRatingStorageFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<PlayerEloRating>>() {});
        }
        RoundRobinSetup roundRobinSetup = (RoundRobinSetup) tournamentDescription.getTournamentSetup();
        TimeControlType timeControlType = roundRobinSetup.getTimeControlType();

        Map<EloRatingKey, EloRating> ratings = records.stream().collect(toMap(
                r -> new EloRatingKey(tournamentDescription.getStartDay().toInstant(), r.getPlayerId()),
                extractRating(timeControlType)));
        return new EloRatingReadOnlyStorage(ratings);
    }

    private static Function<PlayerEloRating, EloRating> extractRating(TimeControlType timeControlType) {
        EloRating defaultRating = new EloRating(new BigDecimal(1500.00).setScale(2, BigDecimal.ROUND_HALF_UP));
        switch (timeControlType) {
            case CLASSIC:
                return r -> r.getClassicRating() != null
                        ? r.getClassicRating()
                        : defaultRating;
            case RAPID:
                return r -> r.getRapidRating() != null
                        ? r.getRapidRating()
                        : defaultRating;
            case BLITZ:
                return r -> r.getBlitzRating() != null
                        ? r.getBlitzRating()
                        : defaultRating;
            default:
                return r -> defaultRating;
        }
    }

    private GameStorage readGames(String gamesFilePath, TournamentDescription tournamentDescription) throws IOException {
        ObjectMapper gameObjectMapper = new GameObjectMapper(tournamentDescription.getTournamentSetup());
        List<Game> games;
        try (FileInputStream fis = new FileInputStream(gamesFilePath)) {
            games = gameObjectMapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
        }
        return new GameReadOnlyStorage(games);
    }

    private PlayerStorage readPlayers(String playersFilePath, TournamentDescription tournamentDescription) throws IOException {
        ObjectMapper baseMapper = new ChessBaseObjectMapper(emptyMap());
        List<Player> players;
        try (FileInputStream fis = new FileInputStream(playersFilePath)) {
            players = baseMapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        List<String> registeredPlayerIds = tournamentDescription.getPlayers();
        List<Player> registeredPlayers = players.stream()
                .filter(player -> registeredPlayerIds.contains(player.getId()))
                .sorted(comparingInt(p -> registeredPlayerIds.indexOf(p.getId())))
                .collect(toList());
        List<String> joinedPlayers = tournamentDescription.getJoinedPlayers();
        registeredPlayers.addAll(players.stream()
                .filter(player -> joinedPlayers.contains(player.getId()))
                .sorted(comparingInt(p -> joinedPlayers.indexOf(p.getId())))
                .collect(toList()));
        return new PlayerReadOnlyStorage(registeredPlayers);
    }

    private TournamentDescription readTournamentDescription(String tournamentDescriptionFilePath) throws IOException {
        Map<String, TournamentSetupObjectNodeReader> tournamentSetupReaders = new HashMap<>();
        tournamentSetupReaders.put("round-robin", new RoundRobinSetupObjectNodeReader());
        ObjectMapper baseMapper = new ChessBaseObjectMapper(tournamentSetupReaders);
        TournamentDescription tournamentDescription;
        try (FileInputStream fis = new FileInputStream(tournamentDescriptionFilePath)) {
            tournamentDescription = baseMapper.readValue(fis, TournamentDescription.class);
        }
        return tournamentDescription;
    }

    private Configuration createTemplatesConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setClassForTemplateLoading(RoundRobinSiteGenerator.class, "/");
        return configuration;
    }

    private Map<String, String> resolve(TournamentDescription tournamentDescription, List<Player> registeredPlayers) {
        Map<String, String> result = new HashMap<>();
        result.put("tournamentTitle", tournamentDescription.getTournamentTitle());
        result.put("tournamentId", tournamentDescription.getTournamentId());
        result.put("roundsAmount", Integer.toString(((RoundRobinSetup)tournamentDescription.getTournamentSetup()).getRoundsAmount()));
        result.put("arbiter", idToName(registeredPlayers).apply(tournamentDescription.getArbiter()));
        String deputyArbiters = tournamentDescription.getDeputyArbiters().stream()
                .map(idToName(registeredPlayers))
                .collect(joining(", "));
        result.put("deputyArbiters", deputyArbiters);
        String gameWriters = tournamentDescription.getGameWriters().stream()
                .map(idToName(registeredPlayers))
                .collect(joining(", "));
        result.put("gameWriters", gameWriters);
        result.put("regulations", tournamentDescription.getRegulations());
        if (tournamentDescription.getLinks() != null) {
            tournamentDescription.getLinks().stream()
                    .filter(l -> l.getPurpose().equals("playoff"))
                    .findFirst()
                    .ifPresent(l -> {
                        result.put("linkvalue", l.getValue());
                        result.put("linkname", l.getName());
                    });
        }
        return result;
    }

    private Function<String, String> idToName(List<Player> registeredPlayers) {
        return id -> registeredPlayers.stream().filter(p -> p.getId().equals(id)).map(p -> p.getFirstName() + " " + p.getLastName())
                .findFirst().orElse(id);
    }
}
