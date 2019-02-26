package ru.appliedtech.chess.roundrobinsitegenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.elorating.EloRating;
import ru.appliedtech.chess.elorating.KValue;
import ru.appliedtech.chess.elorating.KValueSet;
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
        ColorAllocatingSystem colorAllocatingSystem = new ColorAllocatingSystemFactory(roundRobinSetup)
                .create(playerStorage.getPlayers().stream().map(Player::getId).collect(toList()));
        for (Player player : playerStorage.getPlayers()) {
            PlayerStatus playerStatus = new PlayerStatus(player, playerStorage, gameStorage,
                    eloRatingStorage, kValueStorage, tournamentDescription, roundRobinSetup);
            PlayerStatusView playerStatusView = new PlayerStatusView(locale, roundRobinSetup,
                    playerStatus, playerLinks, colorAllocatingSystem);
            String playerStatusFileName = playerLinks.getLink(player.getId())
                    .map(PlayerLink::getLink)
                    .orElseThrow(IllegalStateException::new);
            try (OutputStream os = new FileOutputStream(new File(outputDir, playerStatusFileName))) {
                new PlayerStatusViewHtmlRenderingEngine(configuration).render(playerStatusView, os);
            }
        }

        TournamentTable table = new TournamentTable(playerStorage, gameStorage,
                eloRatingStorage, kValueStorage, roundRobinSetup);
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
        List<KValuesJsonRecord> records;
        try (FileInputStream fis = new FileInputStream(kValueStorageFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<KValuesJsonRecord>>() {});
        }
        Map<String, KValueSet> kValues = records.stream().collect(toMap(
                KValuesJsonRecord::getPlayerId,
                r -> new KValueSet(
                        new KValue(r.getkValueSetRecord().getClassic()),
                        new KValue(r.getkValueSetRecord().getRapid()),
                        new KValue(r.getkValueSetRecord().getBlitz()))));
        return new KValueReadOnlyStorage(kValues);
    }

    public static final class KValuesJsonRecord {
        @JsonProperty
        private String playerId;
        @JsonProperty("kvalues")
        private KValueSetRecord kValueSetRecord;

        public String getPlayerId() {
            return playerId;
        }

        public KValueSetRecord getkValueSetRecord() {
            return kValueSetRecord;
        }
    }

    public static final class KValueSetRecord {
        @JsonProperty
        private int rapid;
        @JsonProperty
        private int classic;
        @JsonProperty
        private int blitz;

        public int getBlitz() {
            return blitz;
        }

        public int getClassic() {
            return classic;
        }

        public int getRapid() {
            return rapid;
        }
    }

    private EloRatingReadOnlyStorage readEloRatings(String eloRatingStorageFilePath, TournamentDescription tournamentDescription) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<RatingRecord> records;
        try (FileInputStream fis = new FileInputStream(eloRatingStorageFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<RatingRecord>>() {});
        }
        Map<EloRatingKey, EloRating> ratings = records.stream().collect(toMap(
                r -> new EloRatingKey(tournamentDescription.getStartDay().toInstant(), r.getPlayerId()),
                r -> r.getRating() != null ? new EloRating(r.getRating()) : new EloRating(new BigDecimal(1500.00).setScale(2, BigDecimal.ROUND_HALF_UP))));
        return new EloRatingReadOnlyStorage(ratings);
    }

    public static final class RatingRecord {
        @JsonProperty
        private String playerId;
        @JsonProperty
        private BigDecimal rating;

        public String getPlayerId() {
            return playerId;
        }

        public BigDecimal getRating() {
            return rating;
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
        return result;
    }

    private Function<String, String> idToName(List<Player> registeredPlayers) {
        return id -> registeredPlayers.stream().filter(p -> p.getId().equals(id)).map(p -> p.getFirstName() + " " + p.getLastName())
                .findFirst().orElse(id);
    }
}
